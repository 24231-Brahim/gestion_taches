package com.gestiontaches.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gestiontaches.domain.enumeration.ProjectRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "project_member", uniqueConstraints = { @UniqueConstraint(columnNames = { "project_id", "user_id" }) })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProjectMember implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "owner", "projectMembers", "sprintses", "epicses", "tasks" }, allowSetters = true)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private ProjectRole role;

    @NotNull
    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    public Long getId() {
        return this.id;
    }

    public ProjectMember id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ProjectMember project(Project project) {
        this.setProject(project);
        return this;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProjectMember user(User user) {
        this.setUser(user);
        return this;
    }

    public ProjectRole getRole() {
        return this.role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public ProjectMember role(ProjectRole role) {
        this.setRole(role);
        return this;
    }

    public Instant getJoinedAt() {
        return this.joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public ProjectMember joinedAt(Instant joinedAt) {
        this.setJoinedAt(joinedAt);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectMember)) {
            return false;
        }
        return getId() != null && getId().equals(((ProjectMember) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProjectMember{" + "id=" + getId() + ", role='" + getRole() + "'" + ", joinedAt='" + getJoinedAt() + "'" + "}";
    }
}
