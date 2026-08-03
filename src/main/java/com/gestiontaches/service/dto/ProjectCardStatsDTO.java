package com.gestiontaches.service.dto;

import java.io.Serializable;

/**
 * Aggregated stats for a single project card on the Projects list page:
 * task progress (done/total) and the current active sprint, if any.
 */
public class ProjectCardStatsDTO implements Serializable {

    private Long projectId;
    private long totalTasks;
    private long doneTasks;
    private Long activeSprintId;
    private String activeSprintName;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public Long getActiveSprintId() {
        return activeSprintId;
    }

    public void setActiveSprintId(Long activeSprintId) {
        this.activeSprintId = activeSprintId;
    }

    public String getActiveSprintName() {
        return activeSprintName;
    }

    public void setActiveSprintName(String activeSprintName) {
        this.activeSprintName = activeSprintName;
    }
}
