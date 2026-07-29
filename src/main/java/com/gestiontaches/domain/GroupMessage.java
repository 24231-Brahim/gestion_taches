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
 * A GroupMessage.
 */
@Entity
@Table(name = "group_message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class GroupMessage implements Serializable {

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

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User recipient;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "groupMessages" }, allowSetters = true)
    private Project project;

    public Long getId() {
        return this.id;
    }

    public GroupMessage id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }

    public GroupMessage content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public GroupMessage createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public User getSender() {
        return this.sender;
    }

    public void setSender(User user) {
        this.sender = user;
    }

    public GroupMessage sender(User user) {
        this.setSender(user);
        return this;
    }

    public User getRecipient() {
        return this.recipient;
    }

    public void setRecipient(User user) {
        this.recipient = user;
    }

    public GroupMessage recipient(User user) {
        this.setRecipient(user);
        return this;
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public GroupMessage project(Project project) {
        this.setProject(project);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroupMessage)) {
            return false;
        }
        return getId() != null && getId().equals(((GroupMessage) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return ("GroupMessage{" + "id=" + getId() + ", content='" + getContent() + "'" + ", createdAt='" + getCreatedAt() + "'" + "}");
    }
}
