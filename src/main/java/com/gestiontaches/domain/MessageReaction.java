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
 * A MessageReaction.
 *
 * <p>An emoji reaction (👍 ❤️ 🎉 👀) a user adds to a {@link ChatMessage}.
 * Persisted for the future reactions feature; not wired to a REST endpoint yet.</p>
 */
@Entity
@Table(name = "chat_message_reaction", uniqueConstraints = { @UniqueConstraint(columnNames = { "message_id", "user_id", "emoji" }) })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MessageReaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "conversation", "sender", "parentMessage", "reactions", "attachments" }, allowSetters = true)
    private ChatMessage message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User user;

    @NotNull
    @Size(min = 1, max = 16)
    @Column(name = "emoji", length = 16, nullable = false)
    private String emoji;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return this.id;
    }

    public MessageReaction id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatMessage getMessage() {
        return this.message;
    }

    public MessageReaction message(ChatMessage chatMessage) {
        this.setMessage(chatMessage);
        return this;
    }

    public void setMessage(ChatMessage chatMessage) {
        this.message = chatMessage;
    }

    public User getUser() {
        return this.user;
    }

    public MessageReaction user(User user) {
        this.setUser(user);
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEmoji() {
        return this.emoji;
    }

    public MessageReaction emoji(String emoji) {
        this.setEmoji(emoji);
        return this;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public MessageReaction createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageReaction)) {
            return false;
        }
        return getId() != null && getId().equals(((MessageReaction) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "MessageReaction{" + "id=" + getId() + ", emoji='" + getEmoji() + "'" + ", createdAt='" + getCreatedAt() + "'" + "}";
    }
}
