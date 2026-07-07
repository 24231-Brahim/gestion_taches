package com.gestiontaches.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class NotificationDTO implements Serializable {

    private Long id;

    @NotNull
    private String message;

    private Long issueId;

    private String issueTitle;

    @NotNull
    private Long userId;

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

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
            ", issueId=" +
            getIssueId() +
            ", issueTitle='" +
            getIssueTitle() +
            "'" +
            ", userId=" +
            getUserId() +
            ", isRead=" +
            getIsRead() +
            ", createdAt='" +
            getCreatedAt() +
            "'" +
            "}"
        );
    }
}
