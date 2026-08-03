package com.gestiontaches.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.dto.AttachmentDTO;
import com.gestiontaches.service.dto.CommentDTO;
import com.gestiontaches.service.dto.ProjectDTO;
import com.gestiontaches.service.dto.ProjectMemberDTO;
import com.gestiontaches.service.dto.SprintDTO;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.dto.UserDTO;
import com.gestiontaches.service.mapper.ProjectMapper;
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
        // Mirrors real seed data: every account gets ROLE_USER paired with one specific role.
        // ROLE_DEVELOPER here is what makes these fixtures reach Task/Comment/Attachment endpoints
        // at all — those controllers' @PreAuthorize lists don't include a bare ROLE_USER.
        return createUserWithAuthority(login, AuthoritiesConstants.DEVELOPER);
    }

    private void addMember(Project project, User user, ProjectRole role) {
        ProjectMember member = new ProjectMember().project(project).user(user).role(role).joinedAt(Instant.now());
        em.persist(member);
        em.flush();
    }

    private User createUserWithAuthority(String login, String authorityName) {
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

    private com.gestiontaches.domain.Task persistTask(User assignee) {
        com.gestiontaches.domain.Task task = new com.gestiontaches.domain.Task()
            .title("Test Task")
            .status(com.gestiontaches.domain.enumeration.TaskStatus.NEW)
            .priority(com.gestiontaches.domain.enumeration.Priority.MEDIUM)
            .createdAt(Instant.now())
            .project(project)
            .assignee(assignee);
        em.persist(task);
        em.flush();
        return task;
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
    @WithMockUser(username = "member-user")
    @Transactional
    void member_can_view_project_members() throws Exception {
        // Viewing the member list is read access, available to any project member — only
        // add/remove/re-role are OWNER/MANAGER-restricted (see member_cannot_add_member above).
        mockMvc.perform(get(PROJECT_API + "/{id}/members", project.getId())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "member-user", roles = { "DEVELOPER" })
    @Transactional
    void developer_only_member_can_view_project_members() throws Exception {
        // Regression test: a real account whose ONLY system authority is ROLE_DEVELOPER (no paired
        // ROLE_USER — this is how "member-user" was actually configured in production for accounts
        // added directly as project members, e.g. fatima) must still pass the controller-level
        // @PreAuthorize on GET /{id}/members. The default @WithMockUser(username=...) grants
        // ROLE_USER implicitly, which masked this gap in member_can_view_project_members above.
        mockMvc.perform(get(PROJECT_API + "/{id}/members", project.getId())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "outsider-user")
    @Transactional
    void outsider_cannot_view_project_members() throws Exception {
        mockMvc.perform(get(PROJECT_API + "/{id}/members", project.getId())).andExpect(status().isForbidden());
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
    void member_cannot_create_task() throws Exception {
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

    // --- Read-endpoint membership enforcement tests ---

    @Test
    @WithMockUser(username = "member-user")
    @Transactional
    void member_can_list_sprints_for_own_project() throws Exception {
        mockMvc.perform(get(SPRINT_API + "?projectId.equals=" + project.getId())).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "outsider-user")
    @Transactional
    void outsider_cannot_list_sprints_for_project() throws Exception {
        mockMvc.perform(get(SPRINT_API + "?projectId.equals=" + project.getId())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "outsider-user")
    @Transactional
    void outsider_cannot_list_epics_for_project() throws Exception {
        mockMvc.perform(get(EPIC_API + "?projectId.equals=" + project.getId())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "outsider-user")
    @Transactional
    void outsider_cannot_list_tasks_for_project() throws Exception {
        mockMvc.perform(get("/api/tasks?projectId.equals=" + project.getId())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "outsider-user")
    @Transactional
    void outsider_cannot_list_tasks_without_any_scoping_filter() throws Exception {
        mockMvc.perform(get("/api/tasks")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "outsider-user")
    @Transactional
    void outsider_cannot_get_task_belonging_to_other_project() throws Exception {
        com.gestiontaches.domain.Task task = new com.gestiontaches.domain.Task()
            .title("Test Task")
            .status(com.gestiontaches.domain.enumeration.TaskStatus.NEW)
            .priority(com.gestiontaches.domain.enumeration.Priority.MEDIUM)
            .createdAt(Instant.now())
            .project(project);
        em.persist(task);
        em.flush();
        mockMvc.perform(get("/api/tasks/{id}", task.getId())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "outsider-user")
    @Transactional
    void outsider_can_list_own_assigned_tasks_without_project_membership() throws Exception {
        com.gestiontaches.domain.Task task = new com.gestiontaches.domain.Task()
            .title("Test Task")
            .status(com.gestiontaches.domain.enumeration.TaskStatus.NEW)
            .priority(com.gestiontaches.domain.enumeration.Priority.MEDIUM)
            .createdAt(Instant.now())
            .project(project)
            .assignee(outsiderUser);
        em.persist(task);
        em.flush();
        mockMvc.perform(get("/api/tasks?assigneeId.equals=" + outsiderUser.getId())).andExpect(status().isOk());
    }

    // --- Task delete tests ---

    @Test
    @WithMockUser(username = "owner-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void owner_can_delete_task() throws Exception {
        com.gestiontaches.domain.Task task = persistTask(memberUser);
        mockMvc.perform(delete("/api/tasks/{id}", task.getId())).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "member-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void member_cannot_delete_task_even_when_assigned_to_self() throws Exception {
        com.gestiontaches.domain.Task task = persistTask(memberUser);
        mockMvc.perform(delete("/api/tasks/{id}", task.getId())).andExpect(status().isForbidden());
    }

    // --- Task edit / reassignment tests ---

    @Test
    @WithMockUser(username = "member-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void member_can_edit_status_of_own_assigned_task() throws Exception {
        com.gestiontaches.domain.Task task = persistTask(memberUser);
        String patch = "{\"id\":" + task.getId() + ",\"status\":\"IN_PROGRESS\"}";
        mockMvc
            .perform(patch("/api/tasks/{id}", task.getId()).contentType("application/merge-patch+json").content(patch))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "member-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void member_cannot_edit_task_assigned_to_someone_else() throws Exception {
        // Task is assigned to the owner, not to member-user — a plain MEMBER may only edit a task
        // they are personally assigned to.
        com.gestiontaches.domain.Task task = persistTask(ownerUser);
        String patchBody = "{\"id\":" + task.getId() + ",\"status\":\"IN_PROGRESS\"}";
        mockMvc
            .perform(patch("/api/tasks/{id}", task.getId()).contentType("application/merge-patch+json").content(patchBody))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void member_cannot_reassign_own_task_via_assign_endpoint() throws Exception {
        com.gestiontaches.domain.Task task = persistTask(memberUser);
        String body = "{\"userId\":" + managerUser.getId() + "}";
        mockMvc
            .perform(patch("/api/tasks/{id}/assign", task.getId()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void member_cannot_reassign_own_task_by_smuggling_assignee_in_patch() throws Exception {
        com.gestiontaches.domain.Task task = persistTask(memberUser);
        UserDTO newAssignee = new UserDTO();
        newAssignee.setId(managerUser.getId());
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setStatus(com.gestiontaches.domain.enumeration.TaskStatus.IN_PROGRESS);
        dto.setAssignee(newAssignee);
        mockMvc
            .perform(patch("/api/tasks/{id}", task.getId()).contentType("application/merge-patch+json").content(om.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "owner-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void owner_can_reassign_task() throws Exception {
        com.gestiontaches.domain.Task task = persistTask(memberUser);
        String body = "{\"userId\":" + managerUser.getId() + "}";
        mockMvc
            .perform(patch("/api/tasks/{id}/assign", task.getId()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());
    }

    // --- Comment tests ---

    @Test
    @WithMockUser(username = "member-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void member_can_comment_on_task_in_own_project() throws Exception {
        com.gestiontaches.domain.Task task = persistTask(memberUser);
        CommentDTO dto = new CommentDTO();
        dto.setContent("A comment");
        dto.setCreatedAt(Instant.now());
        TaskDTO taskRef = new TaskDTO();
        taskRef.setId(task.getId());
        dto.setTask(taskRef);
        mockMvc
            .perform(post("/api/comments").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "outsider-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void outsider_cannot_comment_on_task_outside_their_project() throws Exception {
        com.gestiontaches.domain.Task task = persistTask(memberUser);
        CommentDTO dto = new CommentDTO();
        dto.setContent("A comment");
        dto.setCreatedAt(Instant.now());
        TaskDTO taskRef = new TaskDTO();
        taskRef.setId(task.getId());
        dto.setTask(taskRef);
        mockMvc
            .perform(post("/api/comments").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void member_cannot_edit_another_users_comment() throws Exception {
        com.gestiontaches.domain.Task task = persistTask(memberUser);
        com.gestiontaches.domain.Comment comment = new com.gestiontaches.domain.Comment()
            .content("Original")
            .createdAt(Instant.now())
            .task(task)
            .author(ownerUser);
        em.persist(comment);
        em.flush();

        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent("Edited by someone else");
        mockMvc
            .perform(
                patch("/api/comments/{id}", comment.getId()).contentType("application/merge-patch+json").content(om.writeValueAsString(dto))
            )
            .andExpect(status().isForbidden());
    }

    // --- Attachment tests ---

    @Test
    @WithMockUser(username = "outsider-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void outsider_cannot_upload_attachment_on_task_outside_their_project() throws Exception {
        com.gestiontaches.domain.Task task = persistTask(memberUser);
        AttachmentDTO dto = new AttachmentDTO();
        dto.setFileName("file.txt");
        dto.setFilePath("file.txt");
        dto.setUploadedAt(Instant.now());
        TaskDTO taskRef = new TaskDTO();
        taskRef.setId(task.getId());
        dto.setTask(taskRef);
        mockMvc
            .perform(post("/api/attachments").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "member-user", roles = { "USER", "DEVELOPER" })
    @Transactional
    void member_cannot_delete_another_users_attachment() throws Exception {
        com.gestiontaches.domain.Task task = persistTask(memberUser);
        com.gestiontaches.domain.Attachment attachment = new com.gestiontaches.domain.Attachment()
            .fileName("file.txt")
            .filePath("file.txt")
            .uploadedAt(Instant.now())
            .task(task)
            .uploadedBy(ownerUser);
        em.persist(attachment);
        em.flush();
        mockMvc.perform(delete("/api/attachments/{id}", attachment.getId())).andExpect(status().isForbidden());
    }

    // --- PROJET_MANAGER system-role parity test (no explicit project-level role) ---

    @Test
    @WithMockUser(username = "system-pm", authorities = "ROLE_PROJET_MANAGER")
    @Transactional
    void system_projet_manager_can_manage_project_without_explicit_project_membership() throws Exception {
        createUserWithAuthority("system-pm", AuthoritiesConstants.PROJET_MANAGER);
        ProjectDTO dto = projectMapper.toDto(project);
        dto.setName("Updated By System PM");
        mockMvc
            .perform(
                put(PROJECT_API + "/{id}", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsString(dto))
            )
            .andExpect(status().isOk());
    }
}
