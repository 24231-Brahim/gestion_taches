package com.gestiontaches.service.dto;

import com.gestiontaches.domain.enumeration.ConversationType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A DTO for the {@link com.gestiontaches.domain.Conversation} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConversationDTO implements Serializable {

    private Long id;

    @NotNull
    private ConversationType type;

    private String name;

    private Long projectId;

    @NotNull
    private Instant createdAt;

    /** Timestamp of the most recent message, used by the sidebar to sort conversations. */
    private Instant lastMessageAt;

    /** Truncated content of the most recent message. */
    private String lastMessagePreview;

    /** Number of unread messages for the requesting user. */
    private long unreadCount;

    /** Members of the conversation, empty for the listing endpoint. */
    private List<ChatMemberDTO> participants = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConversationType getType() {
        return type;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<ChatMemberDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ChatMemberDTO> participants) {
        this.participants = participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConversationDTO)) {
            return false;
        }

        ConversationDTO that = (ConversationDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return (
            "ConversationDTO{" +
            "id=" +
            getId() +
            ", type='" +
            getType() +
            "'" +
            ", name='" +
            getName() +
            "'" +
            ", createdAt='" +
            getCreatedAt() +
            "'" +
            "}"
        );
    }
}
