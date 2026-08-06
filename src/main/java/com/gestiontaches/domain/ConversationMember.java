package com.gestiontaches.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A ConversationMember.
 *
 * <p>Tracks which users belong to a {@link Conversation} together with their read state.
 * For DIRECT conversations this also answers "who is the other participant?". The
 * {@code lastReadAt} timestamp drives unread-count computation and acts as the read
 * receipt for a user on a conversation.</p>
 */
@Entity
@Table(name = "chat_conversation_member", uniqueConstraints = { @UniqueConstraint(columnNames = { "conversation_id", "user_id" }) })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ConversationMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "members", "project", "createdBy" }, allowSetters = true)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User user;

    @NotNull
    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    public Long getId() {
        return this.id;
    }

    public ConversationMember id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return this.conversation;
    }

    public ConversationMember conversation(Conversation conversation) {
        this.setConversation(conversation);
        return this;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public User getUser() {
        return this.user;
    }

    public ConversationMember user(User user) {
        this.setUser(user);
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getJoinedAt() {
        return this.joinedAt;
    }

    public ConversationMember joinedAt(Instant joinedAt) {
        this.setJoinedAt(joinedAt);
        return this;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Instant getLastReadAt() {
        return this.lastReadAt;
    }

    public ConversationMember lastReadAt(Instant lastReadAt) {
        this.setLastReadAt(lastReadAt);
        return this;
    }

    public void setLastReadAt(Instant lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConversationMember)) {
            return false;
        }
        return getId() != null && getId().equals(((ConversationMember) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ConversationMember{" + "id=" + getId() + ", joinedAt='" + getJoinedAt() + "'" + "}";
    }
}
