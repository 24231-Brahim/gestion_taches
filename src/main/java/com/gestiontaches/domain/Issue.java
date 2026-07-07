package com.gestiontaches.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gestiontaches.domain.enumeration.IssueStatus;
import com.gestiontaches.domain.enumeration.IssueType;
import com.gestiontaches.domain.enumeration.Priority;
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
 * A Issue.
 */
@Entity
@Table(name = "issue")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Issue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Size(max = 5000)
    @Column(name = "description", length = 5000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private IssueType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IssueStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "issue")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "issue" }, allowSetters = true)
    private Set<Comment> commentses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "issue")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "issue" }, allowSetters = true)
    private Set<Attachment> attachmentses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "issue")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "issue" }, allowSetters = true)
    private Set<ActionHistory> histories = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "project" }, allowSetters = true)
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "project" }, allowSetters = true)
    private Epic epic;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "sprintses", "epicses", "issueses" }, allowSetters = true)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Issue id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public Issue title(String title) {
        this.setTitle(title);
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public Issue description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IssueType getType() {
        return this.type;
    }

    public Issue type(IssueType type) {
        this.setType(type);
        return this;
    }

    public void setType(IssueType type) {
        this.type = type;
    }

    public IssueStatus getStatus() {
        return this.status;
    }

    public Issue status(IssueStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return this.priority;
    }

    public Issue priority(Priority priority) {
        this.setPriority(priority);
        return this;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Issue createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public Issue updatedAt(Instant updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Comment> getCommentses() {
        return this.commentses;
    }

    public void setCommentses(Set<Comment> comments) {
        if (this.commentses != null) {
            this.commentses.forEach(i -> i.setIssue(null));
        }
        if (comments != null) {
            comments.forEach(i -> i.setIssue(this));
        }
        this.commentses = comments;
    }

    public Issue commentses(Set<Comment> comments) {
        this.setCommentses(comments);
        return this;
    }

    public Issue addComments(Comment comment) {
        this.commentses.add(comment);
        comment.setIssue(this);
        return this;
    }

    public Issue removeComments(Comment comment) {
        this.commentses.remove(comment);
        comment.setIssue(null);
        return this;
    }

    public Set<Attachment> getAttachmentses() {
        return this.attachmentses;
    }

    public void setAttachmentses(Set<Attachment> attachments) {
        if (this.attachmentses != null) {
            this.attachmentses.forEach(i -> i.setIssue(null));
        }
        if (attachments != null) {
            attachments.forEach(i -> i.setIssue(this));
        }
        this.attachmentses = attachments;
    }

    public Issue attachmentses(Set<Attachment> attachments) {
        this.setAttachmentses(attachments);
        return this;
    }

    public Issue addAttachments(Attachment attachment) {
        this.attachmentses.add(attachment);
        attachment.setIssue(this);
        return this;
    }

    public Issue removeAttachments(Attachment attachment) {
        this.attachmentses.remove(attachment);
        attachment.setIssue(null);
        return this;
    }

    public Set<ActionHistory> getHistories() {
        return this.histories;
    }

    public void setHistories(Set<ActionHistory> actionHistories) {
        if (this.histories != null) {
            this.histories.forEach(i -> i.setIssue(null));
        }
        if (actionHistories != null) {
            actionHistories.forEach(i -> i.setIssue(this));
        }
        this.histories = actionHistories;
    }

    public Issue histories(Set<ActionHistory> actionHistories) {
        this.setHistories(actionHistories);
        return this;
    }

    public Issue addHistory(ActionHistory actionHistory) {
        this.histories.add(actionHistory);
        actionHistory.setIssue(this);
        return this;
    }

    public Issue removeHistory(ActionHistory actionHistory) {
        this.histories.remove(actionHistory);
        actionHistory.setIssue(null);
        return this;
    }

    public Sprint getSprint() {
        return this.sprint;
    }

    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }

    public Issue sprint(Sprint sprint) {
        this.setSprint(sprint);
        return this;
    }

    public Epic getEpic() {
        return this.epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    public Issue epic(Epic epic) {
        this.setEpic(epic);
        return this;
    }

    public Project getProject() {
        return this.project;
    }

    public User getAssignee() {
        return this.assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public Issue assignee(User assignee) {
        this.setAssignee(assignee);
        return this;
    }

    public User getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(User user) {
        this.createdBy = user;
    }

    public Issue createdBy(User user) {
        this.setCreatedBy(user);
        return this;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Issue project(Project project) {
        this.setProject(project);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Issue)) {
            return false;
        }
        return getId() != null && getId().equals(((Issue) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Issue{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", description='" + getDescription() + "'" +
            ", type='" + getType() + "'" +
            ", status='" + getStatus() + "'" +
            ", priority='" + getPriority() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", assignee='" + (getAssignee() != null ? getAssignee().getLogin() : "null") + "'" +
            ", createdBy='" + (getCreatedBy() != null ? getCreatedBy().getLogin() : "null") + "'" +
            "}";
    }
}
