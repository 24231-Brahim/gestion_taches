package com.gestiontaches.service.dto;

import com.gestiontaches.domain.enumeration.Priority;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.domain.enumeration.TaskStatus;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * A DTO for the admin user detail view, containing user info, tasks, projects, and activity.
 */
public class UserAdminDetailDTO implements Serializable {

    private Long id;
    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private boolean activated;
    private String langKey;
    private String createdBy;
    private Instant createdDate;
    private Set<String> authorities;

    private List<TaskSummaryDTO> tasks;
    private List<ProjectMembershipDTO> projects;
    private List<TaskHistoryEntryDTO> recentActivity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public List<TaskSummaryDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskSummaryDTO> tasks) {
        this.tasks = tasks;
    }

    public List<ProjectMembershipDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectMembershipDTO> projects) {
        this.projects = projects;
    }

    public List<TaskHistoryEntryDTO> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<TaskHistoryEntryDTO> recentActivity) {
        this.recentActivity = recentActivity;
    }

    /**
     * Lightweight task summary for the admin detail view.
     */
    public static class TaskSummaryDTO implements Serializable {

        private Long id;
        private String title;
        private TaskStatus status;
        private Priority priority;
        private Instant createdAt;
        private Long projectId;
        private String projectName;
        private String projectKey;
        private Long sprintId;
        private String sprintName;
        private Long epicId;
        private String epicTitle;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public TaskStatus getStatus() {
            return status;
        }

        public void setStatus(TaskStatus status) {
            this.status = status;
        }

        public Priority getPriority() {
            return priority;
        }

        public void setPriority(Priority priority) {
            this.priority = priority;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getProjectKey() {
            return projectKey;
        }

        public void setProjectKey(String projectKey) {
            this.projectKey = projectKey;
        }

        public Long getSprintId() {
            return sprintId;
        }

        public void setSprintId(Long sprintId) {
            this.sprintId = sprintId;
        }

        public String getSprintName() {
            return sprintName;
        }

        public void setSprintName(String sprintName) {
            this.sprintName = sprintName;
        }

        public Long getEpicId() {
            return epicId;
        }

        public void setEpicId(Long epicId) {
            this.epicId = epicId;
        }

        public String getEpicTitle() {
            return epicTitle;
        }

        public void setEpicTitle(String epicTitle) {
            this.epicTitle = epicTitle;
        }
    }

    /**
     * Project membership summary.
     */
    public static class ProjectMembershipDTO implements Serializable {

        private Long projectId;
        private String projectName;
        private String projectKey;
        private ProjectRole role;
        private Instant joinedAt;

        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getProjectKey() {
            return projectKey;
        }

        public void setProjectKey(String projectKey) {
            this.projectKey = projectKey;
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
    }

    /**
     * Task history entry for the activity feed.
     */
    public static class TaskHistoryEntryDTO implements Serializable {

        private Long id;
        private String action;
        private String oldValue;
        private String newValue;
        private Instant createdAt;
        private Long taskId;
        private String taskTitle;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getOldValue() {
            return oldValue;
        }

        public void setOldValue(String oldValue) {
            this.oldValue = oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public String getTaskTitle() {
            return taskTitle;
        }

        public void setTaskTitle(String taskTitle) {
            this.taskTitle = taskTitle;
        }
    }
}
