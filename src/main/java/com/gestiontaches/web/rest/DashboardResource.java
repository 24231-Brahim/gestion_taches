package com.gestiontaches.web.rest;

import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.service.dto.DashboardKpiDTO;
import com.gestiontaches.service.dto.DashboardKpiDTO.ProjectProgressDTO;
import com.gestiontaches.service.dto.DashboardKpiDTO.TaskStatusCountDTO;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

        dto.setTotalProjects(projectRepository.count());
        dto.setTeamMembers(projectMemberRepository.countDistinctUsers());

        long totalTasks = taskRepository.count();
        dto.setTotalTasks(totalTasks);

        long completedTasks = taskRepository.countByStatus(TaskStatus.DONE);
        dto.setCompletedTasks(completedTasks);

        long overdueTasks = taskRepository.countByStatusNotIn(Arrays.asList(TaskStatus.DONE, TaskStatus.CANCELLED));
        dto.setOverdueTasks(overdueTasks);

        List<Object[]> projectStats = taskRepository.countTasksGroupByProject();
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

        List<Object[]> statusStats = taskRepository.countTasksGroupByStatus();
        List<TaskStatusCountDTO> distribution = statusStats
            .stream()
            .map(row -> new TaskStatusCountDTO((String) row[0], ((Number) row[1]).longValue()))
            .toList();
        dto.setTaskDistribution(distribution);

        return dto;
    }
}
