package com.gestiontaches.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestiontaches.IntegrationTest;
import com.gestiontaches.domain.Authority;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.service.dto.ProjectDTO;
import com.gestiontaches.service.dto.ProjectMemberDTO;
import com.gestiontaches.service.dto.SprintDTO;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.mapper.ProjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
class ProjectRolePermissionIT {

    private static final String PROJECT_API = "/api/projects";
    private static final String TASK_API = "/api/tasks/projects";
    private static final String SPRINT_API = "/api/sprints";
    private static final String EPIC_API = "/api/epics";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMapper projectMapper;

    private User ownerUser;
    private User managerUser;
    private User memberUser;
    private User outsiderUser;
    private Project project;

    @BeforeEach
    void setUp() {
        ownerUser = createUser("owner-user");
        managerUser = createUser("manager-user");
        memberUser = createUser("member-user");
        outsiderUser = createUser("outsider-user");

        project = new Project().name("Test Project").key("TPROJ").createdAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        em.persist(project);
        project.setOwner(ownerUser);

        addMember(project, ownerUser, ProjectRole.OWNER);
        addMember(project, managerUser, ProjectRole.MANAGER);
        addMember(project, memberUser, ProjectRole.MEMBER);
    }

    private User createUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setEmail(login + "@test.com");
        user.setActivated(true);
        user.setFirstName(login);
        user.setLastName(login);
        Authority userAuth = em.find(Authority.class, "ROLE_USER");
        if (userAuth == null) {
            userAuth = new Authority().name("ROLE_USER");
            em.persist(userAuth);
        }
        user.setAuthorities(Set.of(userAuth));
        em.persist(user);
        em.flush();
        return user;
    }

    private void addMember(Project project, User user, ProjectRole role) {
        ProjectMember member = new ProjectMember().project(project).user(user).role(role).joinedAt(Instant.now());
        em.persist(member);
        em.flush();
    }

    // --- Project update tests ---

    @Test
    @WithMockUser(username = "owner-user")
    @Transactional
    void owner_can_update_project() throws Exception {
        ProjectDTO dto = projectMapper.toDto(project);
        dto.setName("Updated Name");
        mockMvc
            .perform(
                put(PROJECT_API + "/{id}", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto))
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager-user")
    @Transactional
    void manager_can_update_project() throws Exception {
        ProjectDTO dto = projectMapper.toDto(project);
        dto.setName("Updated Name");
        mockMvc
            .perform(
                put(PROJECT_API + "/{id}", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto))
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "member-user")
    @Transactional
    void member_cannot_update_project() throws Exception {
        ProjectDTO dto = projectMapper.toDto(project);
        dto.setName("Updated Name");
        mockMvc
            .perform(
                put(PROJECT_API + "/{id}", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "outsider-user")
    @Transactional
    void outsider_cannot_update_project() throws Exception {
        ProjectDTO dto = projectMapper.toDto(project);
        dto.setName("Updated Name");
        mockMvc
            .perform(
                put(PROJECT_API + "/{id}", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto))
            )
            .andExpect(status().isForbidden());
    }

    // --- Project delete tests ---

    @Test
    @WithMockUser(username = "owner-user")
    @Transactional
    void owner_can_delete_project() throws Exception {
        mockMvc.perform(delete(PROJECT_API + "/{id}", project.getId())).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "manager-user")
    @Transactional
    void manager_cannot_delete_project() throws Exception {
        mockMvc.perform(delete(PROJECT_API + "/{id}", project.getId())).andExpect(status().isForbidden());
    }

    // --- Member management tests ---

    @Test
    @WithMockUser(username = "owner-user")
    @Transactional
    void owner_can_add_member() throws Exception {
        User newUser = createUser("new-user");
        em.flush();
        mockMvc.perform(post(PROJECT_API + "/{id}/members/{userId}", project.getId(), newUser.getId())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager-user")
    @Transactional
    void manager_can_add_member() throws Exception {
        User newUser = createUser("new-user");
        em.flush();
        mockMvc.perform(post(PROJECT_API + "/{id}/members/{userId}", project.getId(), newUser.getId())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "member-user")
    @Transactional
    void member_cannot_add_member() throws Exception {
        User newUser = createUser("new-user");
        em.flush();
        mockMvc.perform(post(PROJECT_API + "/{id}/members/{userId}", project.getId(), newUser.getId())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner-user")
    @Transactional
    void owner_can_change_role() throws Exception {
        ProjectMemberDTO dto = new ProjectMemberDTO();
        dto.setRole(ProjectRole.MANAGER);
        mockMvc
            .perform(
                patch(PROJECT_API + "/{id}/members/{userId}", project.getId(), memberUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto))
            )
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager-user")
    @Transactional
    void manager_cannot_change_role() throws Exception {
        ProjectMemberDTO dto = new ProjectMemberDTO();
        dto.setRole(ProjectRole.MANAGER);
        mockMvc
            .perform(
                patch(PROJECT_API + "/{id}/members/{userId}", project.getId(), memberUser.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto))
            )
            .andExpect(status().isForbidden());
    }

    // --- Task creation tests ---

    @Test
    @WithMockUser(username = "owner-user")
    @Transactional
    void owner_can_create_task() throws Exception {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Test Task");
        dto.setStatus(com.gestiontaches.domain.enumeration.TaskStatus.NEW);
        dto.setPriority(com.gestiontaches.domain.enumeration.Priority.MEDIUM);
        dto.setCreatedAt(Instant.now());
        dto.setProject(projectMapper.toDto(project));
        mockMvc
            .perform(
                post(TASK_API + "/{projectId}/tasks", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto))
            )
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "member-user")
    @Transactional
    void member_can_create_task() throws Exception {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Test Task");
        dto.setStatus(com.gestiontaches.domain.enumeration.TaskStatus.NEW);
        dto.setPriority(com.gestiontaches.domain.enumeration.Priority.MEDIUM);
        dto.setCreatedAt(Instant.now());
        dto.setProject(projectMapper.toDto(project));
        mockMvc
            .perform(
                post(TASK_API + "/{projectId}/tasks", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto))
            )
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "outsider-user")
    @Transactional
    void outsider_cannot_create_task() throws Exception {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Test Task");
        dto.setStatus(com.gestiontaches.domain.enumeration.TaskStatus.NEW);
        dto.setPriority(com.gestiontaches.domain.enumeration.Priority.MEDIUM);
        dto.setCreatedAt(Instant.now());
        dto.setProject(projectMapper.toDto(project));
        mockMvc
            .perform(
                post(TASK_API + "/{projectId}/tasks", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto))
            )
            .andExpect(status().isForbidden());
    }

    // --- Sprint creation tests ---

    @Test
    @WithMockUser(username = "owner-user")
    @Transactional
    void owner_can_create_sprint() throws Exception {
        SprintDTO dto = new SprintDTO();
        dto.setName("Test Sprint");
        dto.setStatus(com.gestiontaches.domain.enumeration.SprintStatus.PLANNED);
        dto.setProject(projectMapper.toDto(project));
        mockMvc
            .perform(post(SPRINT_API).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "manager-user")
    @Transactional
    void manager_can_create_sprint() throws Exception {
        SprintDTO dto = new SprintDTO();
        dto.setName("Test Sprint");
        dto.setStatus(com.gestiontaches.domain.enumeration.SprintStatus.PLANNED);
        dto.setProject(projectMapper.toDto(project));
        mockMvc
            .perform(post(SPRINT_API).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "member-user")
    @Transactional
    void member_cannot_create_sprint() throws Exception {
        SprintDTO dto = new SprintDTO();
        dto.setName("Test Sprint");
        dto.setStatus(com.gestiontaches.domain.enumeration.SprintStatus.PLANNED);
        dto.setProject(projectMapper.toDto(project));
        mockMvc
            .perform(post(SPRINT_API).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    // --- Sprint delete tests ---

    @Test
    @WithMockUser(username = "owner-user")
    @Transactional
    void owner_can_delete_sprint() throws Exception {
        com.gestiontaches.domain.Sprint sprint = new com.gestiontaches.domain.Sprint()
            .name("Test Sprint")
            .status(com.gestiontaches.domain.enumeration.SprintStatus.PLANNED)
            .project(project);
        em.persist(sprint);
        em.flush();
        mockMvc.perform(delete(SPRINT_API + "/{id}", sprint.getId())).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "member-user")
    @Transactional
    void member_cannot_delete_sprint() throws Exception {
        com.gestiontaches.domain.Sprint sprint = new com.gestiontaches.domain.Sprint()
            .name("Test Sprint")
            .status(com.gestiontaches.domain.enumeration.SprintStatus.PLANNED)
            .project(project);
        em.persist(sprint);
        em.flush();
        mockMvc.perform(delete(SPRINT_API + "/{id}", sprint.getId())).andExpect(status().isForbidden());
    }

    // --- Epic creation tests ---

    @Test
    @WithMockUser(username = "owner-user")
    @Transactional
    void owner_can_create_epic() throws Exception {
        com.gestiontaches.service.dto.EpicDTO dto = new com.gestiontaches.service.dto.EpicDTO();
        dto.setTitle("Test Epic");
        dto.setStatus(com.gestiontaches.domain.enumeration.EpicStatus.TODO);
        dto.setPriority(com.gestiontaches.domain.enumeration.Priority.MEDIUM);
        dto.setCreatedAt(Instant.now());
        dto.setProject(projectMapper.toDto(project));
        mockMvc
            .perform(post(EPIC_API).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "member-user")
    @Transactional
    void member_cannot_create_epic() throws Exception {
        com.gestiontaches.service.dto.EpicDTO dto = new com.gestiontaches.service.dto.EpicDTO();
        dto.setTitle("Test Epic");
        dto.setStatus(com.gestiontaches.domain.enumeration.EpicStatus.TODO);
        dto.setPriority(com.gestiontaches.domain.enumeration.Priority.MEDIUM);
        dto.setCreatedAt(Instant.now());
        dto.setProject(projectMapper.toDto(project));
        mockMvc
            .perform(post(EPIC_API).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }
}
