package com.gestiontaches.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A ChatMessage.
 *
 * <p>A message inside a {@link Conversation}. Replies/threads are supported through
 * {@code parentMessage} (architecture), mentions are persisted as user ids, and deletion
 * is a soft delete ({@code deleted} flag) so the surrounding conversation stays intact.</p>
 */
@Entity
@Table(name = "chat_message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 1, max = 5000)
    @Column(name = "content", length = 5000, nullable = false)
    private String content;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnoreProperties(value = { "members", "project", "createdBy" }, allowSetters = true)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User sender;

    /**
     * Message this one replies to (threads). Architecture only — not wired to the UI yet.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "conversation", "sender", "parentMessage", "reactions", "attachments" }, allowSetters = true)
    private ChatMessage parentMessage;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "edited_at")
    private Instant editedAt;

    @NotNull
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * User ids mentioned in the message (from @login syntax). Architecture for mention
     * notifications.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chat_message_mentions", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "user_id")
    private Set<Long> mentions = new HashSet<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "message" }, allowSetters = true)
    private Set<MessageReaction> reactions = new HashSet<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "message" }, allowSetters = true)
    private Set<ChatAttachment> attachments = new HashSet<>();

    public Long getId() {
        return this.id;
    }

    public ChatMessage id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }

    public ChatMessage content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Conversation getConversation() {
        return this.conversation;
    }

    public ChatMessage conversation(Conversation conversation) {
        this.setConversation(conversation);
        return this;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public User getSender() {
        return this.sender;
    }

    public ChatMessage sender(User user) {
        this.setSender(user);
        return this;
    }

    public void setSender(User user) {
        this.sender = user;
    }

    public ChatMessage getParentMessage() {
        return this.parentMessage;
    }

    public ChatMessage parentMessage(ChatMessage chatMessage) {
        this.setParentMessage(chatMessage);
        return this;
    }

    public void setParentMessage(ChatMessage chatMessage) {
        this.parentMessage = chatMessage;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public ChatMessage createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getEditedAt() {
        return this.editedAt;
    }

    public ChatMessage editedAt(Instant editedAt) {
        this.setEditedAt(editedAt);
        return this;
    }

    public void setEditedAt(Instant editedAt) {
        this.editedAt = editedAt;
    }

    public Boolean getDeleted() {
        return this.deleted;
    }

    public ChatMessage deleted(Boolean deleted) {
        this.setDeleted(deleted);
        return this;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Set<Long> getMentions() {
        return this.mentions;
    }

    public void setMentions(Set<Long> mentions) {
        this.mentions = mentions;
    }

    public ChatMessage mentions(Set<Long> mentions) {
        this.setMentions(mentions);
        return this;
    }

    public Set<MessageReaction> getReactions() {
        return this.reactions;
    }

    public void setReactions(Set<MessageReaction> messageReactions) {
        if (this.reactions != null) {
            this.reactions.forEach(i -> i.setMessage(null));
        }
        if (messageReactions != null) {
            messageReactions.forEach(i -> i.setMessage(this));
        }
        this.reactions = messageReactions;
    }

    public Set<ChatAttachment> getAttachments() {
        return this.attachments;
    }

    public void setAttachments(Set<ChatAttachment> chatAttachments) {
        if (this.attachments != null) {
            this.attachments.forEach(i -> i.setMessage(null));
        }
        if (chatAttachments != null) {
            chatAttachments.forEach(i -> i.setMessage(this));
        }
        this.attachments = chatAttachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatMessage)) {
            return false;
        }
        return getId() != null && getId().equals(((ChatMessage) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ChatMessage{" + "id=" + getId() + ", content='" + getContent() + "'" + ", createdAt='" + getCreatedAt() + "'" + "}";
    }
}
