package com.gestiontaches.web.rest;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gestiontaches.IntegrationTest;
import com.gestiontaches.domain.Authority;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.security.AuthoritiesConstants;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 * Integration tests for the {@link DashboardResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class DashboardResourceIT {

    @Autowired
    private MockMvc restDashboardMockMvc;

    @Autowired
    private EntityManager em;

    private User pmUser;
    private User otherUser;
    private Project pmProject;
    private Project foreignProject;

    @BeforeEach
    void setUp() {
        pmUser = createUser("pm-owner", AuthoritiesConstants.PROJET_MANAGER);
        otherUser = createUser("other-owner", AuthoritiesConstants.PROJET_MANAGER);

        pmProject = new Project().name("PM Project").key("PMPROJ").createdAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        em.persist(pmProject);
        pmProject.setOwner(pmUser);
        addMember(pmProject, pmUser, ProjectRole.OWNER);
        addMember(pmProject, otherUser, ProjectRole.MEMBER);

        foreignProject = new Project().name("Foreign Project").key("FORPRJ").createdAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        em.persist(foreignProject);
        foreignProject.setOwner(otherUser);
        addMember(foreignProject, otherUser, ProjectRole.OWNER);

        persistTask(pmProject, TaskStatus.DONE);
        persistTask(pmProject, TaskStatus.NEW);
        persistTask(foreignProject, TaskStatus.DONE);
        persistTask(foreignProject, TaskStatus.DONE);
        persistTask(foreignProject, TaskStatus.IN_PROGRESS);
        em.flush();
    }

    @Test
    @WithMockUser(username = "admin", authorities = { "ROLE_ADMIN" })
    void getKpisAsAdminSeesSystemWideTotals() throws Exception {
        restDashboardMockMvc
            .perform(get("/api/dashboard/kpis").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.totalProjects").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.totalTasks").value(greaterThanOrEqualTo(5)))
            .andExpect(jsonPath("$.completedTasks").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.overdueTasks").exists())
            .andExpect(jsonPath("$.projectProgress.length()").value(greaterThanOrEqualTo(2)));
    }

    @Test
    @WithMockUser(username = "pm-owner", authorities = { "ROLE_PROJET_MANAGER" })
    void getKpisAsManagerIsScopedToOwnProjects() throws Exception {
        restDashboardMockMvc
            .perform(get("/api/dashboard/kpis").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.totalProjects").value(1))
            .andExpect(jsonPath("$.totalTasks").value(2))
            .andExpect(jsonPath("$.completedTasks").value(1))
            .andExpect(jsonPath("$.overdueTasks").value(1))
            .andExpect(jsonPath("$.projectProgress[0].projectName").value("PM Project"))
            .andExpect(jsonPath("$.projectProgress.length()").value(1));
    }

    @Test
    @WithMockUser(username = "manager", authorities = { "ROLE_PROJET_MANAGER", "ROLE_USER" })
    void getKpisAsManagerWithoutMembershipReturnsZeros() throws Exception {
        restDashboardMockMvc
            .perform(get("/api/dashboard/kpis").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.totalProjects").value(0))
            .andExpect(jsonPath("$.totalTasks").value(0))
            .andExpect(jsonPath("$.completedTasks").value(0))
            .andExpect(jsonPath("$.overdueTasks").value(0))
            .andExpect(jsonPath("$.projectProgress.length()").value(0));
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
        user.setAuthorities(new java.util.HashSet<>(List.of(userAuth, specificAuth)));
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

    private void persistTask(Project project, TaskStatus status) {
        com.gestiontaches.domain.Task task = new com.gestiontaches.domain.Task()
            .title("Task " + status)
            .status(status)
            .priority(com.gestiontaches.domain.enumeration.Priority.MEDIUM)
            .createdAt(Instant.now())
            .project(project);
        em.persist(task);
        em.flush();
    }
}
