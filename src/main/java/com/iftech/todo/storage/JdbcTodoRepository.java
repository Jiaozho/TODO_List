package com.iftech.todo.storage;

import com.iftech.todo.domain.TodoItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTodoRepository implements TodoRepository {
    private static final DateTimeFormatter DUE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final RowMapper<TodoItem> ROW_MAPPER = new RowMapper<TodoItem>() {
        @Override
        public TodoItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            TodoItem item = new TodoItem();
            item.setId(rs.getString("id"));
            item.setTitle(rs.getString("title"));
            item.setDescription(rs.getString("description"));
            item.setCategory(rs.getString("category"));
            item.setPriority(rs.getInt("priority"));
            Timestamp dueDate = rs.getTimestamp("due_date");
            item.setDueDate(dueDate == null ? null : dueDate.toLocalDateTime().format(DUE_DATE_FORMATTER));
            item.setCompleted(rs.getBoolean("completed"));

            Timestamp createdAt = rs.getTimestamp("created_at");
            Timestamp updatedAt = rs.getTimestamp("updated_at");
            item.setCreatedAt(createdAt == null ? null : createdAt.toInstant());
            item.setUpdatedAt(updatedAt == null ? null : updatedAt.toInstant());
            return item;
        }
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcTodoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TodoItem> list() {
        return jdbcTemplate.query(
                "SELECT id, title, description, category, priority, due_date, completed, created_at, updated_at FROM todo_item ORDER BY created_at DESC",
                ROW_MAPPER);
    }

    @Override
    public TodoItem findById(String id) {
        List<TodoItem> list = jdbcTemplate.query(
                "SELECT id, title, description, category, priority, due_date, completed, created_at, updated_at FROM todo_item WHERE id = ?",
                ROW_MAPPER,
                id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public TodoItem create(TodoItem item) {
        jdbcTemplate.update(
                "INSERT INTO todo_item (id, title, description, category, priority, due_date, completed, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getPriority(),
                toDueTimestamp(item.getDueDate()),
                item.isCompleted(),
                toTimestamp(item.getCreatedAt()),
                toTimestamp(item.getUpdatedAt()));
        return findById(item.getId());
    }

    @Override
    public TodoItem update(TodoItem item) {
        jdbcTemplate.update(
                "UPDATE todo_item SET title = ?, description = ?, category = ?, priority = ?, due_date = ?, completed = ?, created_at = ?, updated_at = ? WHERE id = ?",
                item.getTitle(),
                item.getDescription(),
                item.getCategory(),
                item.getPriority(),
                toDueTimestamp(item.getDueDate()),
                item.isCompleted(),
                toTimestamp(item.getCreatedAt()),
                toTimestamp(item.getUpdatedAt()),
                item.getId());
        return findById(item.getId());
    }

    @Override
    public boolean delete(String id) {
        int affected = jdbcTemplate.update("DELETE FROM todo_item WHERE id = ?", id);
        return affected > 0;
    }

    private Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    private Timestamp toDueTimestamp(String dueDate) {
        if (dueDate == null) {
            return null;
        }
        String trimmed = dueDate.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        LocalDateTime dt;
        if (trimmed.length() == 10) {
            dt = LocalDate.parse(trimmed).atStartOfDay();
        } else {
            dt = LocalDateTime.parse(trimmed);
        }
        return Timestamp.valueOf(dt);
    }
}
