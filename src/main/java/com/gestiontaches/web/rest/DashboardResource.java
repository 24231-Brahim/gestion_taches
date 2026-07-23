package com.gestiontaches.web.rest;

import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.DashboardKpiDTO;
import com.gestiontaches.service.dto.DashboardKpiDTO.ProjectProgressDTO;
import com.gestiontaches.service.dto.DashboardKpiDTO.TaskStatusCountDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardResource {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardResource.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public DashboardResource(
        TaskRepository taskRepository,
        ProjectRepository projectRepository,
        ProjectMemberRepository projectMemberRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @GetMapping("/kpis")
    @Transactional(readOnly = true)
    public DashboardKpiDTO getKpis() {
        LOG.debug("REST request to get Dashboard KPIs");

        DashboardKpiDTO dto = new DashboardKpiDTO();
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);

        LocalDate today = LocalDate.now();
        Instant cutoff = Instant.now().minus(14, ChronoUnit.DAYS);

        if (isAdmin) {
            dto.setTotalProjects(projectRepository.count());
            dto.setTeamMembers(projectMemberRepository.countDistinctUsers());
            dto.setTotalTasks(taskRepository.count());
            dto.setCompletedTasks(taskRepository.countByStatus(TaskStatus.DONE));
            dto.setOverdueTasks(taskRepository.countOverdueTasksGlobal(today, cutoff));

            List<Object[]> projectStats = taskRepository.countTasksGroupByProjectAll();
            populateProjectProgressAndActiveProjects(dto, projectStats);

            List<Object[]> statusStats = taskRepository.countTasksGroupByStatus();
            populateTaskDistribution(dto, statusStats);
        } else if (currentLogin != null) {
            List<Project> userProjects = projectRepository.findAllByOwnerLoginOrMemberLogin(currentLogin);
            List<Long> projectIds = userProjects.stream().map(Project::getId).toList();

            dto.setTotalProjects((long) userProjects.size());

            if (projectIds.isEmpty()) {
                dto.setTeamMembers(0L);
                dto.setTotalTasks(0L);
                dto.setCompletedTasks(0L);
                dto.setOverdueTasks(0L);
                dto.setActiveProjects(0L);
                dto.setProjectProgress(Collections.emptyList());
                dto.setTaskDistribution(Collections.emptyList());
            } else {
                dto.setTeamMembers(projectMemberRepository.countDistinctUsersByProjectIds(projectIds));
                dto.setTotalTasks(taskRepository.countByProjectIdIn(projectIds));
                dto.setCompletedTasks(taskRepository.countByProjectIdInAndStatus(projectIds, TaskStatus.DONE));
                dto.setOverdueTasks(taskRepository.countOverdueTasksByProjectIds(projectIds, today, cutoff));

                List<Object[]> projectStats = taskRepository.countTasksGroupByProjectForProjects(projectIds);
                populateProjectProgressAndActiveProjects(dto, projectStats);

                List<Object[]> statusStats = taskRepository.countTasksGroupByStatusForProjects(projectIds);
                populateTaskDistribution(dto, statusStats);
            }
        }

        return dto;
    }

    private void populateProjectProgressAndActiveProjects(DashboardKpiDTO dto, List<Object[]> projectStats) {
        long activeProjects = projectStats
            .stream()
            .filter(row -> {
                long total = ((Number) row[2]).longValue();
                long done = ((Number) row[3]).longValue();
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
    }

    private void populateTaskDistribution(DashboardKpiDTO dto, List<Object[]> statusStats) {
        List<TaskStatusCountDTO> distribution = statusStats
            .stream()
            .map(row -> new TaskStatusCountDTO(row[0] != null ? row[0].toString() : "UNKNOWN", ((Number) row[1]).longValue()))
            .toList();
        dto.setTaskDistribution(distribution);
    }
}
