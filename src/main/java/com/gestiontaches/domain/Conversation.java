package com.gestiontaches.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gestiontaches.domain.enumeration.ConversationType;
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
 * A Conversation.
 *
 * <p>A chat thread scoped to a {@link Project}. A project owns exactly one GENERAL
 * conversation (the "# General" channel, lazily created on first access) and any number
 * of DIRECT conversations between two members.</p>
 */
@Entity
@Table(name = "chat_conversation")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Conversation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private ConversationType type;

    /**
     * Optional display name (used for future named channels, ignored for GENERAL/DIRECT).
     */
    @Size(max = 100)
    @Column(name = "name", length = 100)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnoreProperties(value = { "owner", "projectMembers", "sprintses", "epicses", "tasks" }, allowSetters = true)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "conversation" }, allowSetters = true)
    private Set<ConversationMember> members = new HashSet<>();

    public Long getId() {
        return this.id;
    }

    public Conversation id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConversationType getType() {
        return this.type;
    }

    public Conversation type(ConversationType type) {
        this.setType(type);
        return this;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public Conversation name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Project getProject() {
        return this.project;
    }

    public Conversation project(Project project) {
        this.setProject(project);
        return this;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getCreatedBy() {
        return this.createdBy;
    }

    public Conversation createdBy(User user) {
        this.setCreatedBy(user);
        return this;
    }

    public void setCreatedBy(User user) {
        this.createdBy = user;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Conversation createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Set<ConversationMember> getMembers() {
        return this.members;
    }

    public void setMembers(Set<ConversationMember> conversationMembers) {
        if (this.members != null) {
            this.members.forEach(i -> i.setConversation(null));
        }
        if (conversationMembers != null) {
            conversationMembers.forEach(i -> i.setConversation(this));
        }
        this.members = conversationMembers;
    }

    public Conversation members(Set<ConversationMember> conversationMembers) {
        this.setMembers(conversationMembers);
        return this;
    }

    public Conversation addMember(ConversationMember conversationMember) {
        this.members.add(conversationMember);
        conversationMember.setConversation(this);
        return this;
    }

    public Conversation removeMember(ConversationMember conversationMember) {
        this.members.remove(conversationMember);
        conversationMember.setConversation(null);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Conversation)) {
            return false;
        }
        return getId() != null && getId().equals(((Conversation) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "Conversation{" +
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
