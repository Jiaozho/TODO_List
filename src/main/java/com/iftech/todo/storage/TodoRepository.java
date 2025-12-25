package com.iftech.todo.storage;

import com.iftech.todo.domain.TodoItem;
import java.util.List;

public interface TodoRepository {
    List<TodoItem> list();

    TodoItem findById(String id);

    TodoItem create(TodoItem item);

    TodoItem update(TodoItem item);

    boolean delete(String id);
}

