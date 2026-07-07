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
 * A Project.
 */
@Entity
@Table(name = "project")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Project implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Size(min = 2, max = 10)
    @Column(name = "project_key", length = 10, nullable = false, unique = true)
    private String key;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "project" }, allowSetters = true)
    private Set<ProjectMember> projectMembers = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "project" }, allowSetters = true)
    private Set<Sprint> sprintses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "project" }, allowSetters = true)
    private Set<Epic> epicses = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "commentses", "attachmentses", "histories", "sprint", "epic", "project" }, allowSetters = true)
    private Set<Issue> issueses = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Project id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Project name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public Project description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return this.key;
    }

    public Project key(String key) {
        this.setKey(key);
        return this;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Project createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public User getOwner() {
        return this.owner;
    }

    public void setOwner(User user) {
        this.owner = user;
    }

    public Project owner(User user) {
        this.setOwner(user);
        return this;
    }

    public Set<ProjectMember> getProjectMembers() {
        return this.projectMembers;
    }

    public void setProjectMembers(Set<ProjectMember> projectMembers) {
        if (this.projectMembers != null) {
            this.projectMembers.forEach(i -> i.setProject(null));
        }
        if (projectMembers != null) {
            projectMembers.forEach(i -> i.setProject(this));
        }
        this.projectMembers = projectMembers;
    }

    public Project projectMembers(Set<ProjectMember> projectMembers) {
        this.setProjectMembers(projectMembers);
        return this;
    }

    public Project addProjectMember(ProjectMember projectMember) {
        this.projectMembers.add(projectMember);
        projectMember.setProject(this);
        return this;
    }

    public Project removeProjectMember(ProjectMember projectMember) {
        this.projectMembers.remove(projectMember);
        projectMember.setProject(null);
        return this;
    }

    public Set<Sprint> getSprintses() {
        return this.sprintses;
    }

    public void setSprintses(Set<Sprint> sprints) {
        if (this.sprintses != null) {
            this.sprintses.forEach(i -> i.setProject(null));
        }
        if (sprints != null) {
            sprints.forEach(i -> i.setProject(this));
        }
        this.sprintses = sprints;
    }

    public Project sprintses(Set<Sprint> sprints) {
        this.setSprintses(sprints);
        return this;
    }

    public Project addSprints(Sprint sprint) {
        this.sprintses.add(sprint);
        sprint.setProject(this);
        return this;
    }

    public Project removeSprints(Sprint sprint) {
        this.sprintses.remove(sprint);
        sprint.setProject(null);
        return this;
    }

    public Set<Epic> getEpicses() {
        return this.epicses;
    }

    public void setEpicses(Set<Epic> epics) {
        if (this.epicses != null) {
            this.epicses.forEach(i -> i.setProject(null));
        }
        if (epics != null) {
            epics.forEach(i -> i.setProject(this));
        }
        this.epicses = epics;
    }

    public Project epicses(Set<Epic> epics) {
        this.setEpicses(epics);
        return this;
    }

    public Project addEpics(Epic epic) {
        this.epicses.add(epic);
        epic.setProject(this);
        return this;
    }

    public Project removeEpics(Epic epic) {
        this.epicses.remove(epic);
        epic.setProject(null);
        return this;
    }

    public Set<Issue> getIssueses() {
        return this.issueses;
    }

    public void setIssueses(Set<Issue> issues) {
        if (this.issueses != null) {
            this.issueses.forEach(i -> i.setProject(null));
        }
        if (issues != null) {
            issues.forEach(i -> i.setProject(this));
        }
        this.issueses = issues;
    }

    public Project issueses(Set<Issue> issues) {
        this.setIssueses(issues);
        return this;
    }

    public Project addIssues(Issue issue) {
        this.issueses.add(issue);
        issue.setProject(this);
        return this;
    }

    public Project removeIssues(Issue issue) {
        this.issueses.remove(issue);
        issue.setProject(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Project)) {
            return false;
        }
        return getId() != null && getId().equals(((Project) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Project{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", key='" + getKey() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            "}";
    }
}
