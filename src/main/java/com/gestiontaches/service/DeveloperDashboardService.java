package com.gestiontaches.service;

import com.gestiontaches.domain.enumeration.IssueStatus;
import com.gestiontaches.domain.enumeration.SprintStatus;
import com.gestiontaches.repository.IssueRepository;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.DeveloperDashboardStatisticsDTO;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DeveloperDashboardService {

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;

    public DeveloperDashboardService(ProjectRepository projectRepository, IssueRepository issueRepository) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
    }

    public DeveloperDashboardStatisticsDTO getStatistics() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("Current user not found"));

        DeveloperDashboardStatisticsDTO dto = new DeveloperDashboardStatisticsDTO();
        dto.setTotalProjects(projectRepository.countByOwnerLoginOrMemberLogin(login));
        dto.setActiveProjects(projectRepository.countActiveProjectsForUser(login, SprintStatus.ACTIVE));
        dto.setTotalTasks(issueRepository.countByAssigneeLogin(login));
        dto.setCompletedTasks(issueRepository.countByAssigneeLoginAndStatus(login, IssueStatus.DONE));
        dto.setOverdueTasks(issueRepository.countOverdueAssignedIssues(login, IssueStatus.DONE, Instant.now().minus(7, ChronoUnit.DAYS)));
        return dto;
    }
}
