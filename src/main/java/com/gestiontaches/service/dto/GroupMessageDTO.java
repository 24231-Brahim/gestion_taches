package com.gestiontaches.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.gestiontaches.domain.GroupMessage} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class GroupMessageDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 1, max = 5000)
    private String content;

    private Instant createdAt;

    private UserDTO sender;

    private UserDTO recipient;

    private ProjectDTO project;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public UserDTO getSender() {
        return sender;
    }

    public void setSender(UserDTO sender) {
        this.sender = sender;
    }

    public UserDTO getRecipient() {
        return recipient;
    }

    public void setRecipient(UserDTO recipient) {
        this.recipient = recipient;
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
        if (!(o instanceof GroupMessageDTO)) {
            return false;
        }

        GroupMessageDTO groupMessageDTO = (GroupMessageDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, groupMessageDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return (
            "GroupMessageDTO{" +
            "id=" +
            getId() +
            ", content='" +
            getContent() +
            "'" +
            ", createdAt='" +
            getCreatedAt() +
            "'" +
            ", sender=" +
            getSender() +
            ", recipient=" +
            getRecipient() +
            ", project=" +
            getProject() +
            "}"
        );
    }
}
