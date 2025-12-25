package com.iftech.todo.api;

import com.iftech.todo.api.dto.CreateTodoRequest;
import com.iftech.todo.api.dto.UpdateTodoRequest;
import com.iftech.todo.domain.TodoItem;
import com.iftech.todo.service.TodoService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
public class TodoController {
    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public List<TodoItem> list() {
        return todoService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoItem create(@Valid @RequestBody CreateTodoRequest request) {
        return todoService.create(request.getTitle(), request.getDescription());
    }

    @PatchMapping("/{id}")
    public TodoItem update(@PathVariable("id") String id, @Valid @RequestBody UpdateTodoRequest request) {
        return todoService.update(id, request);
    }

    @PatchMapping("/{id}/toggle")
    public TodoItem toggle(@PathVariable("id") String id) {
        return todoService.toggle(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        todoService.delete(id);
    }
}

