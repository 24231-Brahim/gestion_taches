package com.gestiontaches.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.gestiontaches.domain.TaskTransition} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TaskTransitionDTO implements Serializable {

    private Long id;

    @Size(max = 50)
    private String fromStatus;

    @NotNull
    @Size(max = 50)
    private String toStatus;

    private Long timeSpentInSeconds;

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

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public Long getTimeSpentInSeconds() {
        return timeSpentInSeconds;
    }

    public void setTimeSpentInSeconds(Long timeSpentInSeconds) {
        this.timeSpentInSeconds = timeSpentInSeconds;
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
        if (!(o instanceof TaskTransitionDTO)) {
            return false;
        }

        TaskTransitionDTO taskTransitionDTO = (TaskTransitionDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, taskTransitionDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TaskTransitionDTO{" +
            "id=" + getId() +
            ", fromStatus='" + getFromStatus() + "'" +
            ", toStatus='" + getToStatus() + "'" +
            ", timeSpentInSeconds=" + getTimeSpentInSeconds() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", task=" + getTask() +
            "}";
    }
}
