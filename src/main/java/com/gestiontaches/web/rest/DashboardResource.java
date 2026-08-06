package com.gestiontaches.web.rest;

import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.ProjectPermissionService;
import com.gestiontaches.service.dto.DashboardKpiDTO;
import com.gestiontaches.service.dto.DashboardKpiDTO.ProjectProgressDTO;
import com.gestiontaches.service.dto.DashboardKpiDTO.TaskStatusCountDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardResource {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardResource.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectPermissionService projectPermissionService;

    public DashboardResource(
        TaskRepository taskRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectPermissionService projectPermissionService
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectPermissionService = projectPermissionService;
    }

    @GetMapping("/kpis")
    @Transactional(readOnly = true)
    public DashboardKpiDTO getKpis() {
        LOG.debug("REST request to get Dashboard KPIs");

        // Only ADMIN has global (cross-project) access. Every other role — including
        // PROJET_MANAGER — sees KPIs scoped to the projects they own or belong to.
        List<Long> projectIds = null;
        if (!projectPermissionService.hasGlobalProjectAccess()) {
            String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user not found")
            );
            projectIds = projectRepository.findProjectIdsByOwnerLoginOrMemberLogin(login);
        }

        DashboardKpiDTO dto = new DashboardKpiDTO();

        if (projectIds != null && projectIds.isEmpty()) {
            return emptyKpis();
        }

        dto.setTotalProjects(projectIds != null ? projectIds.size() : projectRepository.count());
        dto.setTeamMembers(
            projectIds != null
                ? projectMemberRepository.countDistinctUsersByProjectIds(projectIds)
                : projectMemberRepository.countDistinctUsers()
        );

        long totalTasks = projectIds != null ? taskRepository.countByProjectIdIn(projectIds) : taskRepository.count();
        dto.setTotalTasks(totalTasks);

        long completedTasks =
            projectIds != null
                ? taskRepository.countByStatusAndProjectIdIn(TaskStatus.DONE, projectIds)
                : taskRepository.countByStatus(TaskStatus.DONE);
        dto.setCompletedTasks(completedTasks);

        long overdueTasks =
            projectIds != null
                ? taskRepository.countByStatusNotInAndProjectIdIn(List.of(TaskStatus.DONE), projectIds)
                : taskRepository.countByStatusNotIn(List.of(TaskStatus.DONE));
        dto.setOverdueTasks(overdueTasks);

        List<Object[]> projectStats =
            projectIds != null
                ? taskRepository.countTasksGroupByProjectInProjectIds(projectIds)
                : taskRepository.countTasksGroupByProject();
        long activeProjects = projectStats
            .stream()
            .filter(row -> {
                long done = ((Number) row[3]).longValue();
                long total = ((Number) row[2]).longValue();
                return total > 0 && done < total;
            })
            .count();
        dto.setActiveProjects(activeProjects);

        List<ProjectProgressDTO> progress = projectStats
            .stream()
            .map(row -> {
                ProjectProgressDTO p = new ProjectProgressDTO();
                p.setProjectId(((Number) row[0]).longValue());
                p.setProjectName((String) row[1]);
                p.setTotalTasks(((Number) row[2]).longValue());
                p.setDoneTasks(((Number) row[3]).longValue());
                return p;
            })
            .sorted((a, b) ->
                Long.compare(
                    a.getTotalTasks() > 0 ? (a.getDoneTasks() * 100) / a.getTotalTasks() : 0,
                    b.getTotalTasks() > 0 ? (b.getDoneTasks() * 100) / b.getTotalTasks() : 0
                )
            )
            .limit(10)
            .toList();
        dto.setProjectProgress(progress);

        List<Object[]> statusStats =
            projectIds != null ? taskRepository.countTasksGroupByStatusInProjectIds(projectIds) : taskRepository.countTasksGroupByStatus();
        List<TaskStatusCountDTO> distribution = statusStats
            .stream()
            .map(row -> new TaskStatusCountDTO(((TaskStatus) row[0]).name(), ((Number) row[1]).longValue()))
            .toList();
        dto.setTaskDistribution(distribution);

        return dto;
    }

    private DashboardKpiDTO emptyKpis() {
        DashboardKpiDTO dto = new DashboardKpiDTO();
        dto.setProjectProgress(List.of());
        dto.setTaskDistribution(List.of());
        return dto;
    }
}
