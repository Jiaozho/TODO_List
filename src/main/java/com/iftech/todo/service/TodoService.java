package com.iftech.todo.service;

import com.iftech.todo.api.dto.UpdateTodoRequest;
import com.iftech.todo.domain.TodoItem;
import com.iftech.todo.storage.TodoRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TodoService {
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<TodoItem> list() {
        return todoRepository.list();
    }

    public TodoItem create(String title, String description) {
        Instant now = Instant.now();
        TodoItem item = new TodoItem(UUID.randomUUID().toString(), title.trim(), normalizeDescription(description), false, now, now);
        return todoRepository.create(item);
    }

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
        if (request.getCompleted() != null) {
            existing.setCompleted(request.getCompleted());
            changed = true;
        }

        if (changed) {
            existing.setUpdatedAt(Instant.now());
        }
        return todoRepository.update(existing);
    }

    public TodoItem toggle(String id) {
        TodoItem existing = todoRepository.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "todo not found");
        }
        existing.setCompleted(!existing.isCompleted());
        existing.setUpdatedAt(Instant.now());
        return todoRepository.update(existing);
    }

    public void delete(String id) {
        boolean deleted = todoRepository.delete(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "todo not found");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

