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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
public class TodoController {
    private final TodoService todoService;

    /**
     * 构造方法，通过依赖注入获取业务服务。
     *
     * @param todoService TODO 业务服务
     */
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * 查询待办事项列表。
     *
     * <p>返回值为 JSON 数组，默认按创建时间倒序（由存储层/服务层控制）。
     *
     * @return 待办列表
     */
    @GetMapping
    public List<TodoItem> list(@RequestParam(value = "category", required = false) String category) {
        return todoService.list(category);
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return todoService.listCategories();
    }

    /**
     * 创建新的待办事项。
     *
     * <p>标题必填；描述可选。创建成功返回 201，并返回创建后的对象（包含 id、时间戳等）。
     *
     * @param request 创建请求（含 title/description）
     * @return 创建后的待办对象
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TodoItem create(@Valid @RequestBody CreateTodoRequest request) {
        return todoService.create(request.getTitle(), request.getDescription(), request.getCategory());
    }

    /**
     * 按 id 更新待办事项的部分字段。
     *
     * <p>支持按需更新 title/description/completed，未传字段保持不变。
     *
     * @param id      待办 id
     * @param request 更新请求（字段可选）
     * @return 更新后的待办对象
     */
    @PatchMapping("/{id}")
    public TodoItem update(@PathVariable("id") String id, @Valid @RequestBody UpdateTodoRequest request) {
        return todoService.update(id, request);
    }

    /**
     * 切换待办事项的完成状态（completed true/false）。
     *
     * @param id 待办 id
     * @return 切换后的待办对象
     */
    @PatchMapping("/{id}/toggle")
    public TodoItem toggle(@PathVariable("id") String id) {
        return todoService.toggle(id);
    }

    /**
     * 删除指定 id 的待办事项。
     *
     * <p>删除成功返回 204；不存在返回 404。
     *
     * @param id 待办 id
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        todoService.delete(id);
    }
}
