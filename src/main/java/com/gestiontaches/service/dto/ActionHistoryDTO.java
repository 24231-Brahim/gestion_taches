package com.gestiontaches.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.gestiontaches.domain.ActionHistory} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ActionHistoryDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 1, max = 100)
    private String action;

    @Size(max = 100)
    private String fieldChanged;

    @Size(max = 500)
    private String oldValue;

    @Size(max = 500)
    private String newValue;

    @NotNull
    private Instant createdAt;

    @NotNull
    private IssueDTO issue;

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

    public String getFieldChanged() {
        return fieldChanged;
    }

    public void setFieldChanged(String fieldChanged) {
        this.fieldChanged = fieldChanged;
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

    public IssueDTO getIssue() {
        return issue;
    }

    public void setIssue(IssueDTO issue) {
        this.issue = issue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ActionHistoryDTO)) {
            return false;
        }

        ActionHistoryDTO actionHistoryDTO = (ActionHistoryDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, actionHistoryDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ActionHistoryDTO{" +
            "id=" + getId() +
            ", action='" + getAction() + "'" +
            ", fieldChanged='" + getFieldChanged() + "'" +
            ", oldValue='" + getOldValue() + "'" +
            ", newValue='" + getNewValue() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", issue=" + getIssue() +
            "}";
    }
}
