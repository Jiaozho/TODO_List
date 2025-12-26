package com.iftech.todo.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iftech.todo.domain.TodoItem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("file")
@Repository
public class FileTodoRepository implements TodoRepository {
    private static final TypeReference<List<TodoItem>> LIST_TYPE = new TypeReference<List<TodoItem>>() {
    };

    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Path storagePath;

    private List<TodoItem> cached;

    /**
     * 构造方法。
     *
     * <p>存储路径来自配置项 {@code todo.storage.path}，默认 {@code data/todos.json}。
     *
     * @param objectMapper JSON 序列化工具
     * @param storagePath  存储文件路径（相对/绝对均可）
     */
    public FileTodoRepository(ObjectMapper objectMapper, @Value("${todo.storage.path:data/todos.json}") String storagePath) {
        this.objectMapper = objectMapper;
        this.storagePath = Paths.get(storagePath);
    }

    /**
     * 查询待办列表。
     *
     * <p>使用读锁保证并发安全；返回的是缓存数据的副本，并按创建时间倒序排序。
     *
     * @return 待办列表（副本）
     */
    @Override
    public List<TodoItem> list() {
        lock.readLock().lock();
        try {
            ensureLoaded();
            List<TodoItem> copy = new ArrayList<TodoItem>(cached);
            copy.sort(Comparator.comparing(TodoItem::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            return copy;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 按 id 查询待办。
     *
     * <p>使用读锁；返回对象副本，避免外部修改影响缓存。
     *
     * @param id 待办 id
     * @return 找到返回副本；不存在返回 null
     */
    @Override
    public TodoItem findById(String id) {
        lock.readLock().lock();
        try {
            ensureLoaded();
            for (TodoItem item : cached) {
                if (item.getId() != null && item.getId().equals(id)) {
                    return cloneItem(item);
                }
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 新增待办事项并持久化到文件。
     *
     * <p>使用写锁；会写入内存缓存，并落盘到 JSON 文件。
     *
     * @param item 待办对象
     * @return 新增后的对象副本
     */
    @Override
    public TodoItem create(TodoItem item) {
        lock.writeLock().lock();
        try {
            ensureLoaded();
            cached.add(cloneItem(item));
            persist();
            return cloneItem(item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 更新待办事项并持久化到文件。
     *
     * <p>使用写锁；若缓存中存在同 id 则替换，否则按“补写”方式插入。
     *
     * @param item 待办对象
     * @return 更新后的对象副本
     */
    @Override
    public TodoItem update(TodoItem item) {
        lock.writeLock().lock();
        try {
            ensureLoaded();
            for (int i = 0; i < cached.size(); i++) {
                TodoItem existing = cached.get(i);
                if (existing.getId() != null && existing.getId().equals(item.getId())) {
                    cached.set(i, cloneItem(item));
                    persist();
                    return cloneItem(item);
                }
            }
            TodoItem toInsert = cloneItem(item);
            if (toInsert.getCreatedAt() == null) {
                Instant now = Instant.now();
                toInsert.setCreatedAt(now);
                toInsert.setUpdatedAt(now);
            }
            cached.add(toInsert);
            persist();
            return cloneItem(toInsert);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 删除待办事项并持久化到文件。
     *
     * @param id 待办 id
     * @return true 表示删除成功；false 表示目标不存在
     */
    @Override
    public boolean delete(String id) {
        lock.writeLock().lock();
        try {
            ensureLoaded();
            boolean removed = cached.removeIf(item -> item.getId() != null && item.getId().equals(id));
            if (removed) {
                persist();
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 确保缓存已经从磁盘加载。
     *
     * <p>首次访问时加载一次，后续复用内存缓存。
     */
    private void ensureLoaded() {
        if (cached != null) {
            return;
        }
        cached = readFromDisk();
    }

    /**
     * 从磁盘读取 JSON 文件并反序列化为列表。
     *
     * <p>读取失败时返回空列表，避免因单次损坏导致整个服务不可用。
     *
     * @return 待办列表
     */
    private List<TodoItem> readFromDisk() {
        if (!Files.exists(storagePath)) {
            return new ArrayList<TodoItem>();
        }
        try {
            byte[] bytes = Files.readAllBytes(storagePath);
            String json = new String(bytes, StandardCharsets.UTF_8);
            if (json.trim().isEmpty()) {
                return new ArrayList<TodoItem>();
            }
            List<TodoItem> list = objectMapper.readValue(json, LIST_TYPE);
            return list == null ? new ArrayList<TodoItem>() : new ArrayList<TodoItem>(list);
        } catch (IOException e) {
            return new ArrayList<TodoItem>();
        }
    }

    /**
     * 将内存缓存写回到磁盘（JSON 文件）。
     *
     * <p>采用“写临时文件 + 原子替换”的方式，尽量避免进程中断导致文件半写入。
     */
    private void persist() {
        try {
            Path parent = storagePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = storagePath.resolveSibling(storagePath.getFileName().toString() + ".tmp");
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cached);
            Files.write(tmp, json.getBytes(StandardCharsets.UTF_8));
            Files.move(tmp, storagePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new IllegalStateException("failed to persist todos", e);
        }
    }

    /**
     * 克隆一个待办对象，用于隔离内部缓存与外部引用。
     *
     * @param item 原始对象
     * @return 深拷贝后的对象（字段级复制）
     */
    private TodoItem cloneItem(TodoItem item) {
        return new TodoItem(item.getId(), item.getTitle(), item.getDescription(), item.getCategory(), item.getPriority(), item.getDueDate(),
                item.isCompleted(), item.getCreatedAt(), item.getUpdatedAt());
    }
}
