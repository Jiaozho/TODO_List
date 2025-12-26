package com.iftech.todo.service;

import com.iftech.todo.api.dto.UpdateTodoRequest;
import com.iftech.todo.domain.TodoItem;
import com.iftech.todo.storage.TodoRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private static final DateTimeFormatter DUE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public enum Sort {
        CREATED_AT_DESC,
        PRIORITY_DESC,
        DUE_DATE_ASC,
        DUE_DATE_DESC
    }

    /**
     * 构造方法，通过依赖注入获取存储层实现。
     *
     * @param todoRepository TODO 存储接口
     */
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * 查询待办列表。
     *
     * @return 待办列表
     */
    public List<TodoItem> list() {
        return todoRepository.list();
    }

    public List<TodoItem> list(String category) {
        return list(category, Sort.CREATED_AT_DESC);
    }

    public List<TodoItem> list(String category, Sort sort) {
        String normalized = normalizeCategory(category);
        if (normalized == null) {
            return sortList(todoRepository.list(), sort);
        }
        List<TodoItem> list = todoRepository.list();
        List<TodoItem> filtered = list.stream().filter(item -> normalized.equals(item.getCategory())).collect(Collectors.toList());
        return sortList(filtered, sort);
    }

    public List<String> listCategories() {
        List<TodoItem> list = todoRepository.list();
        Set<String> categories = list.stream()
                .map(TodoItem::getCategory)
                .filter(v -> v != null && !v.trim().isEmpty())
                .collect(Collectors.toSet());
        return categories.stream().sorted().collect(Collectors.toList());
    }

    /**
     * 创建新的待办事项。
     *
     * <p>会生成随机 id，并初始化 completed=false，同时设置创建/更新时间。
     *
     * @param title       标题（由上层校验必填，这里会 trim）
     * @param description 描述（可为空）
     * @return 创建后的待办
     */
    public TodoItem create(String title, String description, String category) {
        return create(title, description, category, null, null);
    }

    public TodoItem create(String title, String description, String category, Integer priority, String dueDate) {
        Instant now = Instant.now();
        TodoItem item = new TodoItem(UUID.randomUUID().toString(), title.trim(), normalizeDescription(description),
                normalizeCategory(category), normalizePriority(priority), normalizeDueDate(dueDate), false, now, now);
        return todoRepository.create(item);
    }

    /**
     * 根据 id 更新待办事项的部分字段。
     *
     * <p>若待办不存在则抛出 404；若传入 title 但为空白则抛出 400。
     *
     * @param id      待办 id
     * @param request 更新请求（字段可选）
     * @return 更新后的待办
     */
    public TodoItem update(String id, UpdateTodoRequest request) {
        TodoItem existing = todoRepository.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "todo not found");
        }

        boolean changed = false;
        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
            }
            existing.setTitle(title);
            changed = true;
        }
        if (request.getDescription() != null) {
            existing.setDescription(normalizeDescription(request.getDescription()));
            changed = true;
        }
        if (request.getCategory() != null) {
            existing.setCategory(normalizeCategory(request.getCategory()));
            changed = true;
        }
        if (request.getPriority() != null) {
            existing.setPriority(normalizePriority(request.getPriority()));
            changed = true;
        }
        if (request.getDueDate() != null) {
            existing.setDueDate(normalizeDueDate(request.getDueDate()));
            changed = true;
        }
        if (request.getCompleted() != null) {
            existing.setCompleted(request.getCompleted());
            changed = true;
        }

        if (changed) {
            existing.setUpdatedAt(Instant.now());
        }
        return todoRepository.update(existing);
    }

    /**
     * 切换待办事项完成状态。
     *
     * <p>若待办不存在则抛出 404。
     *
     * @param id 待办 id
     * @return 切换后的待办
     */
    public TodoItem toggle(String id) {
        TodoItem existing = todoRepository.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "todo not found");
        }
        existing.setCompleted(!existing.isCompleted());
        existing.setUpdatedAt(Instant.now());
        return todoRepository.update(existing);
    }

    /**
     * 删除待办事项。
     *
     * <p>若待办不存在则抛出 404。
     *
     * @param id 待办 id
     */
    public void delete(String id) {
        boolean deleted = todoRepository.delete(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "todo not found");
        }
    }

    /**
     * 统一处理描述字段：去除首尾空格；空字符串归一化为 null。
     *
     * @param description 原始描述
     * @return 归一化后的描述
     */
    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeCategory(String category) {
        if (category == null) {
            return null;
        }
        String trimmed = category.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int normalizePriority(Integer priority) {
        if (priority == null) {
            return TodoItem.DEFAULT_PRIORITY;
        }
        if (priority < 1 || priority > 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "priority must be 1..3");
        }
        return priority;
    }

    private String normalizeDueDate(String dueDate) {
        if (dueDate == null) {
            return null;
        }
        String trimmed = dueDate.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            LocalDateTime dt;
            if (trimmed.length() == 10) {
                dt = LocalDate.parse(trimmed).atStartOfDay();
            } else {
                dt = LocalDateTime.parse(trimmed);
            }
            return dt.format(DUE_DATE_FORMATTER);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dueDate must be yyyy-MM-ddTHH:mm");
        }
    }

    private List<TodoItem> sortList(List<TodoItem> list, Sort sort) {
        if (sort == null) {
            sort = Sort.CREATED_AT_DESC;
        }
        java.util.Comparator<TodoItem> createdDesc = java.util.Comparator
                .comparing(TodoItem::getCreatedAt, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                .reversed();
        java.util.Comparator<TodoItem> comparator;
        switch (sort) {
            case PRIORITY_DESC:
                comparator = java.util.Comparator.comparingInt(TodoItem::getPriority).reversed().thenComparing(createdDesc);
                break;
            case DUE_DATE_ASC:
                comparator = java.util.Comparator
                        .<TodoItem, LocalDateTime>comparing(
                                item -> parseDueDateTime(item.getDueDate()),
                                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                        .thenComparing(createdDesc);
                break;
            case DUE_DATE_DESC:
                comparator = java.util.Comparator
                        .<TodoItem, LocalDateTime>comparing(
                                item -> parseDueDateTime(item.getDueDate()),
                                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                        .reversed()
                        .thenComparing(createdDesc);
                break;
            case CREATED_AT_DESC:
            default:
                comparator = createdDesc;
                break;
        }
        list.sort(comparator);
        return list;
    }

    private LocalDateTime parseDueDateTime(String dueDate) {
        if (dueDate == null) {
            return null;
        }
        String trimmed = dueDate.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            if (trimmed.length() == 10) {
                return LocalDate.parse(trimmed).atStartOfDay();
            }
            return LocalDateTime.parse(trimmed);
        } catch (Exception e) {
            return null;
        }
    }

    public Sort parseSort(String raw) {
        if (raw == null) {
            return Sort.CREATED_AT_DESC;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return Sort.CREATED_AT_DESC;
        }
        if ("priority".equals(normalized) || "prioritydesc".equals(normalized) || "priority_desc".equals(normalized)) {
            return Sort.PRIORITY_DESC;
        }
        if ("duedate".equals(normalized) || "due".equals(normalized) || "due_date".equals(normalized)
                || "duedateasc".equals(normalized) || "due_date_asc".equals(normalized) || "dueasc".equals(normalized)) {
            return Sort.DUE_DATE_ASC;
        }
        if ("duedatedesc".equals(normalized) || "due_date_desc".equals(normalized) || "duedesc".equals(normalized)) {
            return Sort.DUE_DATE_DESC;
        }
        if ("created".equals(normalized) || "createdat".equals(normalized) || "created_at".equals(normalized)
                || "createddesc".equals(normalized) || "created_at_desc".equals(normalized)) {
            return Sort.CREATED_AT_DESC;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid sort");
    }
}
