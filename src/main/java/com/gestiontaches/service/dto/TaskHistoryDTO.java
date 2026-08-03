package com.gestiontaches.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.gestiontaches.domain.TaskHistory} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TaskHistoryDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 100)
    private String action;

    @Size(max = 500)
    private String oldValue;

    @Size(max = 500)
    private String newValue;

    @NotNull
    private Instant createdAt;

    @NotNull
    private TaskDTO task;

    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public TaskDTO getTask() {
        return task;
    }

    public void setTask(TaskDTO task) {
        this.task = task;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskHistoryDTO)) {
            return false;
        }

        TaskHistoryDTO taskHistoryDTO = (TaskHistoryDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, taskHistoryDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TaskHistoryDTO{" +
            "id=" + getId() +
            ", action='" + getAction() + "'" +
            ", oldValue='" + getOldValue() + "'" +
            ", newValue='" + getNewValue() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", task=" + getTask() +
            "}";
    }
}
