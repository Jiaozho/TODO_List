package com.iftech.todo.storage;

import com.iftech.todo.domain.TodoItem;
import java.util.List;

public interface TodoRepository {
    /**
     * 查询全部待办事项。
     *
     * @return 待办列表
     */
    List<TodoItem> list();

    /**
     * 按 id 查询单个待办事项。
     *
     * @param id 待办 id
     * @return 找到则返回对象；找不到返回 null
     */
    TodoItem findById(String id);

    /**
     * 新增待办事项。
     *
     * @param item 待办对象
     * @return 持久化后的对象（通常等于入参的拷贝）
     */
    TodoItem create(TodoItem item);

    /**
     * 更新待办事项。
     *
     * @param item 待办对象
     * @return 更新后的对象
     */
    TodoItem update(TodoItem item);

    /**
     * 删除待办事项。
     *
     * @param id 待办 id
     * @return true 表示删除成功；false 表示目标不存在
     */
    boolean delete(String id);
}
