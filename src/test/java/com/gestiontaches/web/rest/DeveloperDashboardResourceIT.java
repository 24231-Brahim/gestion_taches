package com.gestiontaches.web.rest;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gestiontaches.IntegrationTest;
import com.gestiontaches.domain.Authority;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.Priority;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.security.AuthoritiesConstants;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link DeveloperDashboardResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class DeveloperDashboardResourceIT {

    @Autowired
    private MockMvc restDeveloperDashboardMockMvc;

    @Autowired
    private EntityManager em;

    private User userA;
    private User userB;
    private Project project;

    @BeforeEach
    void setUp() {
        userA = createUser("dash-user-a", AuthoritiesConstants.USER);
        userB = createUser("dash-user-b", AuthoritiesConstants.USER);

        project = new Project().name("Dash Project").key("DASHPJ").createdAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        em.persist(project);
        project.setOwner(userA);
        addMember(project, userA, ProjectRole.MEMBER);
        addMember(project, userB, ProjectRole.MEMBER);

        persistTask(project, userA, TaskStatus.IN_PROGRESS);
        persistTask(project, userA, TaskStatus.DONE);
        // Task assigned to another member: must NOT leak into userA's stats.
        persistTask(project, userB, TaskStatus.NEW);
        em.flush();
    }

    @Test
    @WithMockUser(username = "dash-user-a", authorities = { "ROLE_USER" })
    void getStatisticsAsPlainUserReturnsOwnScopedStats() throws Exception {
        restDeveloperDashboardMockMvc
            .perform(get("/api/developer-dashboard/statistics").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.assignedTasksTotal").value(2))
            .andExpect(jsonPath("$.inProgressTasks").value(1))
            .andExpect(jsonPath("$.doneTasks").value(1))
            .andExpect(jsonPath("$.overdueTasks").value(0))
            .andExpect(jsonPath("$.memberProjectsCount").value(1))
            .andExpect(jsonPath("$.taskDistribution", hasSize(2)))
            .andExpect(jsonPath("$.taskDistribution[*].status", hasItem("IN_PROGRESS")))
            .andExpect(jsonPath("$.taskDistribution[*].status", hasItem("DONE")));
    }

    @Test
    @WithMockUser(username = "dash-user-b", authorities = { "ROLE_USER" })
    void getStatisticsAsAnotherUserSeesOnlyTheirOwnTasks() throws Exception {
        restDeveloperDashboardMockMvc
            .perform(get("/api/developer-dashboard/statistics").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.assignedTasksTotal").value(1))
            .andExpect(jsonPath("$.inProgressTasks").value(0))
            .andExpect(jsonPath("$.doneTasks").value(0))
            .andExpect(jsonPath("$.memberProjectsCount").value(1))
            .andExpect(jsonPath("$.taskDistribution[0].status").value("NEW"))
            .andExpect(jsonPath("$.taskDistribution[0].count").value(1));
    }

    @Test
    @WithMockUser(username = "dash-user-a", authorities = { "ROLE_USER" })
    void getStatisticsCountsOverdueTasksViaCreationDateHeuristic() throws Exception {
        Task oldTask = new Task()
            .title("Old open task")
            .status(TaskStatus.NEW)
            .priority(Priority.HIGH)
            .createdAt(Instant.now().minus(30, ChronoUnit.DAYS))
            .project(project)
            .assignee(userA);
        em.persist(oldTask);
        em.flush();

        restDeveloperDashboardMockMvc
            .perform(get("/api/developer-dashboard/statistics").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assignedTasksTotal").value(3))
            .andExpect(jsonPath("$.overdueTasks").value(1));
    }

    @Test
    @WithUnauthenticatedMockUser
    void getStatisticsIsForbiddenForAnonymous() throws Exception {
        restDeveloperDashboardMockMvc
            .perform(get("/api/developer-dashboard/statistics").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    private User createUser(String login, String authorityName) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setEmail(login + "@test.com");
        user.setActivated(true);
        user.setFirstName(login);
        user.setLastName(login);
        Authority userAuth = findOrCreateAuthority("ROLE_USER");
        Authority specificAuth = findOrCreateAuthority(authorityName);
        user.setAuthorities(new HashSet<>(List.of(userAuth, specificAuth)));
        em.persist(user);
        em.flush();
        return user;
    }

    private Authority findOrCreateAuthority(String name) {
        Authority authority = em.find(Authority.class, name);
        if (authority == null) {
            authority = new Authority().name(name);
            em.persist(authority);
            em.flush();
        }
        return authority;
    }

    private void addMember(Project project, User user, ProjectRole role) {
        ProjectMember member = new ProjectMember().project(project).user(user).role(role).joinedAt(Instant.now());
        em.persist(member);
        em.flush();
    }

    private void persistTask(Project project, User assignee, TaskStatus status) {
        Task task = new Task()
            .title("Task " + status + " for " + assignee.getLogin())
            .status(status)
            .priority(Priority.MEDIUM)
            .createdAt(Instant.now())
            .project(project)
            .assignee(assignee);
        em.persist(task);
        em.flush();
    }
}
