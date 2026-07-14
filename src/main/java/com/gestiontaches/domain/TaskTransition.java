package com.gestiontaches.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "task_transition")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TaskTransition implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Size(max = 50)
    @Column(name = "from_status", length = 50)
    private String fromStatus;

    @NotNull
    @Size(max = 50)
    @Column(name = "to_status", length = 50, nullable = false)
    private String toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "time_spent_in_seconds")
    private Long timeSpentInSeconds;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return this.task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getFromStatus() {
        return this.fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return this.toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getTimeSpentInSeconds() {
        return this.timeSpentInSeconds;
    }

    public void setTimeSpentInSeconds(Long timeSpentInSeconds) {
        this.timeSpentInSeconds = timeSpentInSeconds;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskTransition)) return false;
        return getId() != null && getId().equals(((TaskTransition) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "TaskTransition{" +
            "id=" +
            getId() +
            ", fromStatus='" +
            getFromStatus() +
            "'" +
            ", toStatus='" +
            getToStatus() +
            "'" +
            ", timeSpentInSeconds=" +
            getTimeSpentInSeconds() +
            ", createdAt='" +
            getCreatedAt() +
            "'" +
            "}"
        );
    }
}
