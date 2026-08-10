package com.gestiontaches.web.rest;

import static com.gestiontaches.domain.ProjectAsserts.*;
import static com.gestiontaches.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestiontaches.IntegrationTest;
import com.gestiontaches.domain.Attachment;
import com.gestiontaches.domain.Authority;
import com.gestiontaches.domain.ChatMessage;
import com.gestiontaches.domain.Comment;
import com.gestiontaches.domain.Conversation;
import com.gestiontaches.domain.ConversationMember;
import com.gestiontaches.domain.Epic;
import com.gestiontaches.domain.Notification;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.Sprint;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.TaskHistory;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.ConversationType;
import com.gestiontaches.domain.enumeration.EpicStatus;
import com.gestiontaches.domain.enumeration.Priority;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.domain.enumeration.SprintStatus;
import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.AttachmentRepository;
import com.gestiontaches.repository.ChatMessageRepository;
import com.gestiontaches.repository.CommentRepository;
import com.gestiontaches.repository.ConversationMemberRepository;
import com.gestiontaches.repository.ConversationRepository;
import com.gestiontaches.repository.EpicRepository;
import com.gestiontaches.repository.NotificationRepository;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.repository.SprintRepository;
import com.gestiontaches.repository.TaskHistoryRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.service.dto.ProjectDTO;
import com.gestiontaches.service.dto.ProjectMemberDTO;
import com.gestiontaches.service.mapper.ProjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProjectResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", authorities = { "ROLE_ADMIN" })
class ProjectResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String DEFAULT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_KEY = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/projects";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProjectMockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private EpicRepository epicRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationMemberRepository conversationMemberRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private Project project;

    private Project insertedProject;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Project createEntity() {
        return new Project().name(DEFAULT_NAME).description(DEFAULT_DESCRIPTION).key(DEFAULT_KEY).createdAt(DEFAULT_CREATED_AT);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Project createUpdatedEntity() {
        return new Project().name(UPDATED_NAME).description(UPDATED_DESCRIPTION).key(UPDATED_KEY).createdAt(UPDATED_CREATED_AT);
    }

    @BeforeEach
    void initTest() {
        project = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedProject != null) {
            projectRepository.delete(insertedProject);
            insertedProject = null;
        }
    }

    @Test
    @Transactional
    void createProject() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);
        var returnedProjectDTO = om.readValue(
            restProjectMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ProjectDTO.class
        );

        // Validate the Project in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedProject = projectMapper.toEntity(returnedProjectDTO);
        assertProjectUpdatableFieldsEquals(returnedProject, getPersistedProject(returnedProject));

        insertedProject = returnedProject;
    }

    @Test
    @Transactional
    void createProjectWithExistingId() throws Exception {
        // Create the Project with an existing ID
        project.setId(1L);
        ProjectDTO projectDTO = projectMapper.toDto(project);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        project.setName(null);

        // Create the Project, which fails.
        ProjectDTO projectDTO = projectMapper.toDto(project);

        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkKeyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        project.setKey(null);

        // Create the Project, which fails.
        ProjectDTO projectDTO = projectMapper.toDto(project);

        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        project.setCreatedAt(null);

        // Create the Project, which fails.
        ProjectDTO projectDTO = projectMapper.toDto(project);

        restProjectMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllProjects() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get all the projectList
        restProjectMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(project.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @Test
    @Transactional
    void getProject() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        // Get the project
        restProjectMockMvc
            .perform(get(ENTITY_API_URL_ID, project.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(project.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingProject() throws Exception {
        // Get the project
        restProjectMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingProject() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the project
        Project updatedProject = projectRepository.findById(project.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedProject are not directly saved in db
        em.detach(updatedProject);
        updatedProject.name(UPDATED_NAME).description(UPDATED_DESCRIPTION).key(UPDATED_KEY).createdAt(UPDATED_CREATED_AT);
        ProjectDTO projectDTO = projectMapper.toDto(updatedProject);

        restProjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, projectDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isOk());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedProjectToMatchAllProperties(updatedProject);
    }

    @Test
    @Transactional
    void putNonExistingProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, projectDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProjectWithPatch() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the project using partial update
        Project partialUpdatedProject = new Project();
        partialUpdatedProject.setId(project.getId());

        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProject.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProject))
            )
            .andExpect(status().isOk());

        // Validate the Project in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProjectUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedProject, project), getPersistedProject(project));
    }

    @Test
    @Transactional
    void fullUpdateProjectWithPatch() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the project using partial update
        Project partialUpdatedProject = new Project();
        partialUpdatedProject.setId(project.getId());

        partialUpdatedProject.name(UPDATED_NAME).description(UPDATED_DESCRIPTION).key(UPDATED_KEY).createdAt(UPDATED_CREATED_AT);

        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProject.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedProject))
            )
            .andExpect(status().isOk());

        // Validate the Project in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertProjectUpdatableFieldsEquals(partialUpdatedProject, getPersistedProject(partialUpdatedProject));
    }

    @Test
    @Transactional
    void patchNonExistingProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, projectDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(projectDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProject() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        project.setId(longCount.incrementAndGet());

        // Create the Project
        ProjectDTO projectDTO = projectMapper.toDto(project);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProjectMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(projectDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Project in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProject() throws Exception {
        // Initialize the database
        insertedProject = projectRepository.saveAndFlush(project);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the project
        restProjectMockMvc
            .perform(delete(ENTITY_API_URL_ID, project.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    void deleteProjectWithChildrenCascades() throws Exception {
        User owner = createUser("del-owner");
        User member = createUser("del-member");
        em.flush();

        Project p = createEntity().key("DELP").name("Delete Me");
        em.persist(p);
        em.flush();
        p.setOwner(owner);

        ProjectMember ownerMember = new ProjectMember().project(p).user(owner).role(ProjectRole.OWNER).joinedAt(Instant.now());
        ProjectMember memberMember = new ProjectMember().project(p).user(member).role(ProjectRole.MEMBER).joinedAt(Instant.now());
        em.persist(ownerMember);
        em.persist(memberMember);

        Sprint sprint = new Sprint().name("Sprint 1").status(SprintStatus.PLANNED).project(p);
        em.persist(sprint);

        Epic epic = new Epic().title("Epic 1").status(EpicStatus.TODO).priority(Priority.MEDIUM).createdAt(Instant.now()).project(p);
        em.persist(epic);

        Task task = new Task()
            .title("Task 1")
            .status(TaskStatus.NEW)
            .priority(Priority.MEDIUM)
            .createdAt(Instant.now())
            .project(p)
            .sprint(sprint)
            .epic(epic)
            .assignee(member);
        em.persist(task);

        Comment comment = new Comment().content("A comment").createdAt(Instant.now()).task(task).author(member);
        em.persist(comment);

        Attachment attachment = new Attachment()
            .fileName("file.txt")
            .filePath("file.txt")
            .uploadedAt(Instant.now())
            .task(task)
            .uploadedBy(member);
        em.persist(attachment);

        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setUser(member);
        history.setAction("STATUS_CHANGE");
        history.setCreatedAt(Instant.now());
        em.persist(history);

        Notification notification = new Notification()
            .message("Task update")
            .task(task)
            .user(member)
            .isRead(false)
            .createdAt(Instant.now());
        em.persist(notification);

        Conversation conversation = new Conversation().type(ConversationType.GENERAL).project(p).createdAt(Instant.now());
        em.persist(conversation);

        ConversationMember convMember = new ConversationMember().conversation(conversation).user(owner).joinedAt(Instant.now());
        em.persist(convMember);

        ChatMessage message = new ChatMessage()
            .content("Hello")
            .conversation(conversation)
            .sender(owner)
            .createdAt(Instant.now())
            .deleted(false);
        message.setMentions(new HashSet<>(List.of(member.getId())));
        em.persist(message);
        em.flush();
        em.clear();

        Long projectId = p.getId();

        restProjectMockMvc
            .perform(delete(ENTITY_API_URL_ID, projectId).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        em.flush();

        assertThat(projectRepository.findById(projectId)).isEmpty();
        assertThat(projectMemberRepository.findById(ownerMember.getId())).isEmpty();
        assertThat(projectMemberRepository.findById(memberMember.getId())).isEmpty();
        assertThat(sprintRepository.findById(sprint.getId())).isEmpty();
        assertThat(epicRepository.findById(epic.getId())).isEmpty();
        assertThat(taskRepository.findById(task.getId())).isEmpty();
        assertThat(commentRepository.findById(comment.getId())).isEmpty();
        assertThat(attachmentRepository.findById(attachment.getId())).isEmpty();
        assertThat(taskHistoryRepository.findById(history.getId())).isEmpty();
        assertThat(notificationRepository.findById(notification.getId())).isEmpty();
        assertThat(conversationRepository.findById(conversation.getId())).isEmpty();
        assertThat(conversationMemberRepository.findById(convMember.getId())).isEmpty();
        assertThat(chatMessageRepository.findById(message.getId())).isEmpty();
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
        user.setAuthorities(new HashSet<>(List.of(userAuth)));
        em.persist(user);
        em.flush();
        return user;
    }

    protected long getRepositoryCount() {
        return projectRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Project getPersistedProject(Project project) {
        return projectRepository.findById(project.getId()).orElseThrow();
    }

    protected void assertPersistedProjectToMatchAllProperties(Project expectedProject) {
        assertProjectAllPropertiesEquals(expectedProject, getPersistedProject(expectedProject));
    }

    protected void assertPersistedProjectToMatchUpdatableProperties(Project expectedProject) {
        assertProjectAllUpdatablePropertiesEquals(expectedProject, getPersistedProject(expectedProject));
    }

    @Test
    @Transactional
    void getCurrentUserMemberships_AdminNotExplicitMember_GetsOwnerRole() throws Exception {
        // Create a project directly in the repo WITHOUT adding admin as a member
        Project project1 = createEntity().key("TEST1").name("Test1");
        insertedProject = projectRepository.saveAndFlush(project1);

        Project project2 = createEntity().key("TEST2").name("Test2");
        project2 = projectRepository.saveAndFlush(project2);

        // Admin is NOT an explicit member of either project, but should get OWNER role for all
        MvcResult result = restProjectMockMvc
            .perform(get(ENTITY_API_URL + "/my-roles").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        // Verify the response contains entries for both projects with OWNER role
        org.assertj.core.api.Assertions.assertThat(responseBody).contains("\"projectId\":" + insertedProject.getId());
        org.assertj.core.api.Assertions.assertThat(responseBody).contains("\"projectId\":" + project2.getId());
        org.assertj.core.api.Assertions.assertThat(responseBody).contains("\"role\":\"OWNER\"");
    }
}
