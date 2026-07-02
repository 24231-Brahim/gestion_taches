package com.gestiontaches.service.dto;

import com.gestiontaches.domain.enumeration.EpicStatus;
import com.gestiontaches.domain.enumeration.Priority;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A DTO for the {@link com.gestiontaches.domain.Epic} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EpicDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 1, max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotNull
    private EpicStatus status;

    @NotNull
    private Priority priority;

    @NotNull
    private Instant createdAt;

    private Instant updatedAt;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull
    private ProjectDTO project;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EpicStatus getStatus() {
        return status;
    }

    public void setStatus(EpicStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ProjectDTO getProject() {
        return project;
    }

    public void setProject(ProjectDTO project) {
        this.project = project;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EpicDTO)) {
            return false;
        }

        EpicDTO epicDTO = (EpicDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, epicDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EpicDTO{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", description='" + getDescription() + "'" +
            ", status='" + getStatus() + "'" +
            ", priority='" + getPriority() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", startDate='" + getStartDate() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", project=" + getProject() +
            "}";
    }
}
