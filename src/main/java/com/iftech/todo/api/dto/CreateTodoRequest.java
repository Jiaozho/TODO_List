package com.iftech.todo.api.dto;

import javax.validation.constraints.NotBlank;

public class CreateTodoRequest {
    @NotBlank
    private String title;

    private String description;
    private String category;
    private Integer priority;
    private String dueDate;

    /**
     * 获取待办标题。
     *
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置待办标题。
     *
     * @param title 标题（必填）
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取待办描述。
     *
     * @return 描述（可为空）
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置待办描述。
     *
     * @param description 描述（可为空）
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
