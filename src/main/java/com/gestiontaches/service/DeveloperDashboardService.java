package com.gestiontaches.service;

import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.service.dto.DashboardKpiDTO.TaskStatusCountDTO;
import com.gestiontaches.service.dto.DeveloperDashboardStatisticsDTO;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Builds the stats shown on the developer-scoped home dashboard. Everything here is filtered to
 * the current user: their own assigned tasks and the projects they belong to as a
 * {@link com.gestiontaches.domain.ProjectMember} — never system-wide totals (those belong to the
 * admin/PM dashboard, see {@link com.gestiontaches.web.rest.DashboardResource}).
 */
@Service
@Transactional(readOnly = true)
public class DeveloperDashboardService {

    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectPermissionService projectPermissionService;

    public DeveloperDashboardService(
        TaskRepository taskRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectPermissionService projectPermissionService
    ) {
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectPermissionService = projectPermissionService;
    }

    public DeveloperDashboardStatisticsDTO getStatistics() {
        Long userId = projectPermissionService.resolveCurrentUserId();

        DeveloperDashboardStatisticsDTO dto = new DeveloperDashboardStatisticsDTO();
        dto.setAssignedTasksTotal(taskRepository.countByAssigneeId(userId));
        dto.setInProgressTasks(taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.IN_PROGRESS));
        dto.setDoneTasks(taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.DONE));
        // No dueDate field exists on Task yet, so "overdue" is a heuristic: still open a week
        // after creation. Swap this out if/when a real dueDate column is added.
        dto.setOverdueTasks(
            taskRepository.countByAssigneeIdAndStatusNotInAndCreatedAtBefore(
                userId,
                List.of(TaskStatus.DONE),
                Instant.now().minus(7, ChronoUnit.DAYS)
            )
        );
        dto.setMemberProjectsCount(projectMemberRepository.findByUserId(userId).size());

        List<TaskStatusCountDTO> distribution = taskRepository
            .countTasksGroupByStatusForAssignee(userId)
            .stream()
            .map(row -> new TaskStatusCountDTO(((TaskStatus) row[0]).name(), ((Number) row[1]).longValue()))
            .toList();
        dto.setTaskDistribution(distribution);

        return dto;
    }
}
