package com.iftech.todo.api.dto;

public class UpdateTodoRequest {
    private String title;
    private String description;
    private String category;
    private Boolean completed;

    /**
     * 获取待办标题（用于更新）。
     *
     * @return 标题；为 null 表示不更新该字段
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置待办标题（用于更新）。
     *
     * @param title 标题；为 null 表示不更新该字段
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取待办描述（用于更新）。
     *
     * @return 描述；为 null 表示不更新该字段
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置待办描述（用于更新）。
     *
     * @param description 描述；为 null 表示不更新该字段
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

    /**
     * 获取完成状态（用于更新）。
     *
     * @return 完成状态；为 null 表示不更新该字段
     */
    public Boolean getCompleted() {
        return completed;
    }

    /**
     * 设置完成状态（用于更新）。
     *
     * @param completed 完成状态；为 null 表示不更新该字段
     */
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
