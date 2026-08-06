package com.gestiontaches.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.gestiontaches.domain.UserPresence} entity.
 */
public class UserPresenceDTO implements Serializable {

    private Long userId;

    private boolean online;

    private Instant lastActiveAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Instant getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserPresenceDTO)) {
            return false;
        }
        UserPresenceDTO that = (UserPresenceDTO) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "UserPresenceDTO{" + "userId=" + getUserId() + ", online=" + isOnline() + "}";
    }
}
