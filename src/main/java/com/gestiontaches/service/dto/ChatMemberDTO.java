package com.gestiontaches.service.dto;

import com.gestiontaches.domain.enumeration.ProjectRole;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A member of the chat for a given project.
 *
 * <p>Combines project membership ({@link com.gestiontaches.domain.ProjectMember}) with
 * the per-conversation read state. Used both for the members sidebar and for conversation participants.</p>
 */
public class ChatMemberDTO implements Serializable {

    private Long userId;

    private String userLogin;

    private ProjectRole role;

    private Instant joinedAt;

    /** Read state of this member inside a conversation (null outside a conversation context). */
    private Instant lastReadAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Instant getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(Instant lastReadAt) {
        this.lastReadAt = lastReadAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatMemberDTO)) {
            return false;
        }
        ChatMemberDTO that = (ChatMemberDTO) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return ("ChatMemberDTO{" + "userId=" + getUserId() + ", userLogin='" + getUserLogin() + "'" + ", role='" + getRole() + "'" + "}");
    }
}
