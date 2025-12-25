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
import org.springframework.stereotype.Repository;

@Repository
public class FileTodoRepository implements TodoRepository {
    private static final TypeReference<List<TodoItem>> LIST_TYPE = new TypeReference<List<TodoItem>>() {
    };

    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Path storagePath;

    private List<TodoItem> cached;

    public FileTodoRepository(ObjectMapper objectMapper, @Value("${todo.storage.path:data/todos.json}") String storagePath) {
        this.objectMapper = objectMapper;
        this.storagePath = Paths.get(storagePath);
    }

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

    private void ensureLoaded() {
        if (cached != null) {
            return;
        }
        cached = readFromDisk();
    }

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

    private TodoItem cloneItem(TodoItem item) {
        return new TodoItem(item.getId(), item.getTitle(), item.getDescription(), item.isCompleted(), item.getCreatedAt(),
                item.getUpdatedAt());
    }
}
