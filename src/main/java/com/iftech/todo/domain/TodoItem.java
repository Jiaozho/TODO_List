package com.iftech.todo.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public class TodoItem {
    private String id;
    private String title;
    private String description;
    private String category;
    private boolean completed;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant updatedAt;

    /**
     * 供序列化/反序列化使用的无参构造方法。
     */
    public TodoItem() {
    }

    /**
     * 领域对象完整构造方法。
     *
     * @param id          唯一标识
     * @param title       标题
     * @param description 描述（可为空）
     * @param completed   是否完成
     * @param createdAt   创建时间
     * @param updatedAt   更新时间
     */
    public TodoItem(String id, String title, String description, String category, boolean completed, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.completed = completed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 获取待办 id。
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * 设置待办 id。
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取标题。
     *
     * @return 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置标题。
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取描述。
     *
     * @return 描述（可为空）
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置描述。
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

    /**
     * 获取完成状态。
     *
     * @return true 表示已完成；false 表示未完成
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * 设置完成状态。
     *
     * @param completed 完成状态
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * 获取创建时间。
     *
     * @return 创建时间
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取更新时间。
     *
     * @return 更新时间
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间。
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
