package com.gestiontaches.service.dto;

import java.io.Serializable;
import java.util.List;

public class DashboardKpiDTO implements Serializable {

    private long totalProjects;
    private long activeProjects;
    private long totalTasks;
    private long completedTasks;
    private long overdueTasks;
    private long teamMembers;
    private List<ProjectProgressDTO> projectProgress;
    private List<TaskStatusCountDTO> taskDistribution;

    public long getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(long totalProjects) {
        this.totalProjects = totalProjects;
    }

    public long getActiveProjects() {
        return activeProjects;
    }

    public void setActiveProjects(long activeProjects) {
        this.activeProjects = activeProjects;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(long completedTasks) {
        this.completedTasks = completedTasks;
    }

    public long getOverdueTasks() {
        return overdueTasks;
    }

    public void setOverdueTasks(long overdueTasks) {
        this.overdueTasks = overdueTasks;
    }

    public long getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(long teamMembers) {
        this.teamMembers = teamMembers;
    }

    public List<ProjectProgressDTO> getProjectProgress() {
        return projectProgress;
    }

    public void setProjectProgress(List<ProjectProgressDTO> projectProgress) {
        this.projectProgress = projectProgress;
    }

    public List<TaskStatusCountDTO> getTaskDistribution() {
        return taskDistribution;
    }

    public void setTaskDistribution(List<TaskStatusCountDTO> taskDistribution) {
        this.taskDistribution = taskDistribution;
    }

    public static class ProjectProgressDTO implements Serializable {

        private Long projectId;
        private String projectName;
        private long totalTasks;
        private long doneTasks;

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

        public long getTotalTasks() {
            return totalTasks;
        }

        public void setTotalTasks(long totalTasks) {
            this.totalTasks = totalTasks;
        }

        public long getDoneTasks() {
            return doneTasks;
        }

        public void setDoneTasks(long doneTasks) {
            this.doneTasks = doneTasks;
        }
    }

    public static class TaskStatusCountDTO implements Serializable {

        private String status;
        private long count;

        public TaskStatusCountDTO() {}

        public TaskStatusCountDTO(String status, long count) {
            this.status = status;
            this.count = count;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
