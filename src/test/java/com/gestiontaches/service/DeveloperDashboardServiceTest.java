package com.gestiontaches.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gestiontaches.domain.enumeration.IssueStatus;
import com.gestiontaches.domain.enumeration.SprintStatus;
import com.gestiontaches.repository.IssueRepository;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.service.dto.DeveloperDashboardStatisticsDTO;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class DeveloperDashboardServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private IssueRepository issueRepository;

    @InjectMocks
    private DeveloperDashboardService developerDashboardService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnDeveloperScopedStatistics() {
        SecurityContextHolder.setContext(
            new org.springframework.security.core.context.SecurityContextImpl(
                new UsernamePasswordAuthenticationToken("developer", "password", List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER")))
            )
        );

        when(projectRepository.countByOwnerLoginOrMemberLogin("developer")).thenReturn(3L);
        when(projectRepository.countActiveProjectsForUser("developer", SprintStatus.ACTIVE)).thenReturn(2L);
        when(issueRepository.countByAssigneeLogin("developer")).thenReturn(8L);
        when(issueRepository.countByAssigneeLoginAndStatus("developer", IssueStatus.DONE)).thenReturn(4L);
        when(issueRepository.countOverdueAssignedIssues(any(String.class), any(IssueStatus.class), any(Instant.class))).thenReturn(1L);

        DeveloperDashboardStatisticsDTO stats = developerDashboardService.getStatistics();

        assertThat(stats.getTotalProjects()).isEqualTo(3L);
        assertThat(stats.getActiveProjects()).isEqualTo(2L);
        assertThat(stats.getTotalTasks()).isEqualTo(8L);
        assertThat(stats.getCompletedTasks()).isEqualTo(4L);
        assertThat(stats.getOverdueTasks()).isEqualTo(1L);
    }
}
