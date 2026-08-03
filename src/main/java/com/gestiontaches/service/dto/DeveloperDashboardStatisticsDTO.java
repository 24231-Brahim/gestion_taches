package com.gestiontaches.service.dto;

import com.gestiontaches.service.dto.DashboardKpiDTO.TaskStatusCountDTO;
import java.io.Serializable;
import java.util.List;

/**
 * Stats for the developer-scoped home dashboard: everything here is scoped to the current user
 * (their own assigned tasks and the projects they're a member of), never system-wide totals.
 */
public class DeveloperDashboardStatisticsDTO implements Serializable {

    private long assignedTasksTotal;
    private long inProgressTasks;
    private long doneTasks;
    private long overdueTasks;
    private long memberProjectsCount;
    private List<TaskStatusCountDTO> taskDistribution;

    public long getAssignedTasksTotal() {
        return assignedTasksTotal;
    }

    public void setAssignedTasksTotal(long assignedTasksTotal) {
        this.assignedTasksTotal = assignedTasksTotal;
    }

    public long getInProgressTasks() {
        return inProgressTasks;
    }

    public void setInProgressTasks(long inProgressTasks) {
        this.inProgressTasks = inProgressTasks;
    }

    public long getDoneTasks() {
        return doneTasks;
    }

    public void setDoneTasks(long doneTasks) {
        this.doneTasks = doneTasks;
    }

    public long getOverdueTasks() {
        return overdueTasks;
    }

    public void setOverdueTasks(long overdueTasks) {
        this.overdueTasks = overdueTasks;
    }

    public long getMemberProjectsCount() {
        return memberProjectsCount;
    }

    public void setMemberProjectsCount(long memberProjectsCount) {
        this.memberProjectsCount = memberProjectsCount;
    }

    public List<TaskStatusCountDTO> getTaskDistribution() {
        return taskDistribution;
    }

    public void setTaskDistribution(List<TaskStatusCountDTO> taskDistribution) {
        this.taskDistribution = taskDistribution;
    }
}
