package com.gestiontaches.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class NotificationDTO implements Serializable {

    private Long id;

    @NotNull
    private String message;

    private TaskDTO task;

    private String taskTitle;

    private UserDTO user;

    @NotNull
    private Boolean isRead;

    @NotNull
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TaskDTO getTask() {
        return task;
    }

    public void setTask(TaskDTO task) {
        this.task = task;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    // Keep backward-compatible setters for raw IDs
    public void setTaskId(Long taskId) {
        if (taskId != null) {
            if (this.task == null) {
                this.task = new TaskDTO();
            }
            this.task.setId(taskId);
        }
    }

    public Long getTaskId() {
        return this.task != null ? this.task.getId() : null;
    }

    public void setUserId(Long userId) {
        if (userId != null) {
            if (this.user == null) {
                this.user = new UserDTO();
            }
            this.user.setId(userId);
        }
    }

    public Long getUserId() {
        return this.user != null ? this.user.getId() : null;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationDTO)) return false;
        NotificationDTO that = (NotificationDTO) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return (
            "NotificationDTO{" +
            "id=" +
            getId() +
            ", message='" +
            getMessage() +
            "'" +
            ", taskTitle='" +
            getTaskTitle() +
            "'" +
            ", isRead=" +
            getIsRead() +
            ", createdAt='" +
            getCreatedAt() +
            "'" +
            "}"
        );
    }
}
