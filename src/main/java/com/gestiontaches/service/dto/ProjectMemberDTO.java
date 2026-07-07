package com.gestiontaches.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProjectMemberDTO implements Serializable {

    private Long id;

    private Long projectId;

    private Long userId;

    private String userLogin;

    @NotNull
    @Size(max = 50)
    private String role;

    @NotNull
    private Instant joinedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectMemberDTO)) {
            return false;
        }

        ProjectMemberDTO that = (ProjectMemberDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return (
            "ProjectMemberDTO{" +
            "id=" +
            getId() +
            ", projectId='" +
            getProjectId() +
            "'" +
            ", userId='" +
            getUserId() +
            "'" +
            ", userLogin='" +
            getUserLogin() +
            "'" +
            ", role='" +
            getRole() +
            "'" +
            ", joinedAt='" +
            getJoinedAt() +
            "'" +
            "}"
        );
    }
}
