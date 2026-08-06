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
 * A UserPresence.
 *
 * <p>Lightweight presence marker used by the chat: one row per user holding the timestamp
 * of their last activity. A user is considered {@code online} when {@code lastActiveAt} is
 * within a short freshness window (see {@link com.gestiontaches.service.ChatService}).</p>
 */
@Entity
@Table(name = "chat_user_presence")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class UserPresence implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User user;

    @NotNull
    @Column(name = "last_active_at", nullable = false)
    private Instant lastActiveAt;

    public Long getId() {
        return this.id;
    }

    public UserPresence id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return this.user;
    }

    public UserPresence user(User user) {
        this.setUser(user);
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getLastActiveAt() {
        return this.lastActiveAt;
    }

    public UserPresence lastActiveAt(Instant lastActiveAt) {
        this.setLastActiveAt(lastActiveAt);
        return this;
    }

    public void setLastActiveAt(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserPresence)) {
            return false;
        }
        return getId() != null && getId().equals(((UserPresence) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UserPresence{" + "id=" + getId() + ", lastActiveAt='" + getLastActiveAt() + "'" + "}";
    }
}
