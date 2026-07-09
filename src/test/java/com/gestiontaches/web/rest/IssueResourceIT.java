package com.gestiontaches.web.rest;

import static com.gestiontaches.domain.IssueAsserts.*;
import static com.gestiontaches.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestiontaches.IntegrationTest;
import com.gestiontaches.domain.Epic;
import com.gestiontaches.domain.Issue;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.Sprint;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.IssueStatus;
import com.gestiontaches.domain.enumeration.IssueType;
import com.gestiontaches.domain.enumeration.Priority;
import com.gestiontaches.repository.IssueRepository;
import com.gestiontaches.service.IssueService;
import com.gestiontaches.service.dto.IssueDTO;
import com.gestiontaches.service.mapper.IssueMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link IssueResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = { "ROLE_ADMIN" })
class IssueResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final IssueType DEFAULT_TYPE = IssueType.STORY;
    private static final IssueType UPDATED_TYPE = IssueType.BUG;

    private static final IssueStatus DEFAULT_STATUS = IssueStatus.BACKLOG;
    private static final IssueStatus UPDATED_STATUS = IssueStatus.TODO;

    private static final Priority DEFAULT_PRIORITY = Priority.LOWEST;
    private static final Priority UPDATED_PRIORITY = Priority.LOW;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/issues";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private IssueRepository issueRepository;

    @Mock
    private IssueRepository issueRepositoryMock;

    @Autowired
    private IssueMapper issueMapper;

    @Mock
    private IssueService issueServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restIssueMockMvc;

    private Issue issue;

    private Issue insertedIssue;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Issue createEntity(EntityManager em) {
        Issue issue = new Issue()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
            .type(DEFAULT_TYPE)
            .status(DEFAULT_STATUS)
            .priority(DEFAULT_PRIORITY)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
        // Add required entity
        Project project;
        if (TestUtil.findAll(em, Project.class).isEmpty()) {
            project = ProjectResourceIT.createEntity();
            em.persist(project);
            em.flush();
        } else {
            project = TestUtil.findAll(em, Project.class).get(0);
        }
        issue.setProject(project);
        return issue;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Issue createUpdatedEntity(EntityManager em) {
        Issue updatedIssue = new Issue()
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .type(UPDATED_TYPE)
            .status(UPDATED_STATUS)
            .priority(UPDATED_PRIORITY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        // Add required entity
        Project project;
        if (TestUtil.findAll(em, Project.class).isEmpty()) {
            project = ProjectResourceIT.createUpdatedEntity();
            em.persist(project);
            em.flush();
        } else {
            project = TestUtil.findAll(em, Project.class).get(0);
        }
        updatedIssue.setProject(project);
        return updatedIssue;
    }

    @BeforeEach
    void initTest() {
        issue = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedIssue != null) {
            issueRepository.delete(insertedIssue);
            insertedIssue = null;
        }
    }

    @Test
    @Transactional
    void createIssue() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Issue
        IssueDTO issueDTO = issueMapper.toDto(issue);
        var returnedIssueDTO = om.readValue(
            restIssueMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            IssueDTO.class
        );

        // Validate the Issue in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedIssue = issueMapper.toEntity(returnedIssueDTO);
        assertIssueUpdatableFieldsEquals(returnedIssue, getPersistedIssue(returnedIssue));

        insertedIssue = returnedIssue;
    }

    @Test
    @Transactional
    void createIssueWithExistingId() throws Exception {
        // Create the Issue with an existing ID
        issue.setId(1L);
        IssueDTO issueDTO = issueMapper.toDto(issue);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restIssueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Issue in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        issue.setTitle(null);

        // Create the Issue, which fails.
        IssueDTO issueDTO = issueMapper.toDto(issue);

        restIssueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        issue.setType(null);

        // Create the Issue, which fails.
        IssueDTO issueDTO = issueMapper.toDto(issue);

        restIssueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        issue.setStatus(null);

        // Create the Issue, which fails.
        IssueDTO issueDTO = issueMapper.toDto(issue);

        restIssueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPriorityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        issue.setPriority(null);

        // Create the Issue, which fails.
        IssueDTO issueDTO = issueMapper.toDto(issue);

        restIssueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        issue.setCreatedAt(null);

        // Create the Issue, which fails.
        IssueDTO issueDTO = issueMapper.toDto(issue);

        restIssueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllIssues() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList
        restIssueMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(issue.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllIssuesWithEagerRelationshipsIsEnabled() throws Exception {
        when(issueServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restIssueMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(issueServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllIssuesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(issueServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restIssueMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(issueRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getIssue() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get the issue
        restIssueMockMvc
            .perform(get(ENTITY_API_URL_ID, issue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(issue.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getIssuesByIdFiltering() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        Long id = issue.getId();

        defaultIssueFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultIssueFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultIssueFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllIssuesByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where title equals to
        defaultIssueFiltering("title.equals=" + DEFAULT_TITLE, "title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllIssuesByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where title in
        defaultIssueFiltering("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE, "title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllIssuesByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where title is not null
        defaultIssueFiltering("title.specified=true", "title.specified=false");
    }

    @Test
    @Transactional
    void getAllIssuesByTitleContainsSomething() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where title contains
        defaultIssueFiltering("title.contains=" + DEFAULT_TITLE, "title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllIssuesByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where title does not contain
        defaultIssueFiltering("title.doesNotContain=" + UPDATED_TITLE, "title.doesNotContain=" + DEFAULT_TITLE);
    }

    @Test
    @Transactional
    void getAllIssuesByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where description equals to
        defaultIssueFiltering("description.equals=" + DEFAULT_DESCRIPTION, "description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllIssuesByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where description in
        defaultIssueFiltering("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION, "description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllIssuesByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where description is not null
        defaultIssueFiltering("description.specified=true", "description.specified=false");
    }

    @Test
    @Transactional
    void getAllIssuesByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where description contains
        defaultIssueFiltering("description.contains=" + DEFAULT_DESCRIPTION, "description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllIssuesByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where description does not contain
        defaultIssueFiltering("description.doesNotContain=" + UPDATED_DESCRIPTION, "description.doesNotContain=" + DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllIssuesByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where type equals to
        defaultIssueFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllIssuesByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where type in
        defaultIssueFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllIssuesByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where type is not null
        defaultIssueFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllIssuesByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where status equals to
        defaultIssueFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllIssuesByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where status in
        defaultIssueFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllIssuesByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where status is not null
        defaultIssueFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllIssuesByPriorityIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where priority equals to
        defaultIssueFiltering("priority.equals=" + DEFAULT_PRIORITY, "priority.equals=" + UPDATED_PRIORITY);
    }

    @Test
    @Transactional
    void getAllIssuesByPriorityIsInShouldWork() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where priority in
        defaultIssueFiltering("priority.in=" + DEFAULT_PRIORITY + "," + UPDATED_PRIORITY, "priority.in=" + UPDATED_PRIORITY);
    }

    @Test
    @Transactional
    void getAllIssuesByPriorityIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where priority is not null
        defaultIssueFiltering("priority.specified=true", "priority.specified=false");
    }

    @Test
    @Transactional
    void getAllIssuesByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where createdAt equals to
        defaultIssueFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllIssuesByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where createdAt in
        defaultIssueFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllIssuesByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where createdAt is not null
        defaultIssueFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllIssuesByUpdatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where updatedAt equals to
        defaultIssueFiltering("updatedAt.equals=" + DEFAULT_UPDATED_AT, "updatedAt.equals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllIssuesByUpdatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where updatedAt in
        defaultIssueFiltering("updatedAt.in=" + DEFAULT_UPDATED_AT + "," + UPDATED_UPDATED_AT, "updatedAt.in=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllIssuesByUpdatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        // Get all the issueList where updatedAt is not null
        defaultIssueFiltering("updatedAt.specified=true", "updatedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllIssuesBySprintIsEqualToSomething() throws Exception {
        Sprint sprint;
        if (TestUtil.findAll(em, Sprint.class).isEmpty()) {
            issueRepository.saveAndFlush(issue);
            sprint = SprintResourceIT.createEntity(em);
        } else {
            sprint = TestUtil.findAll(em, Sprint.class).get(0);
        }
        em.persist(sprint);
        em.flush();
        issue.setSprint(sprint);
        issueRepository.saveAndFlush(issue);
        Long sprintId = sprint.getId();
        // Get all the issueList where sprint equals to sprintId
        defaultIssueShouldBeFound("sprintId.equals=" + sprintId);

        // Get all the issueList where sprint equals to (sprintId + 1)
        defaultIssueShouldNotBeFound("sprintId.equals=" + (sprintId + 1));
    }

    @Test
    @Transactional
    void getAllIssuesByEpicIsEqualToSomething() throws Exception {
        Epic epic;
        if (TestUtil.findAll(em, Epic.class).isEmpty()) {
            issueRepository.saveAndFlush(issue);
            epic = EpicResourceIT.createEntity(em);
        } else {
            epic = TestUtil.findAll(em, Epic.class).get(0);
        }
        em.persist(epic);
        em.flush();
        issue.setEpic(epic);
        issueRepository.saveAndFlush(issue);
        Long epicId = epic.getId();
        // Get all the issueList where epic equals to epicId
        defaultIssueShouldBeFound("epicId.equals=" + epicId);

        // Get all the issueList where epic equals to (epicId + 1)
        defaultIssueShouldNotBeFound("epicId.equals=" + (epicId + 1));
    }

    @Test
    @Transactional
    void getAllIssuesByProjectIsEqualToSomething() throws Exception {
        Project project;
        if (TestUtil.findAll(em, Project.class).isEmpty()) {
            issueRepository.saveAndFlush(issue);
            project = ProjectResourceIT.createEntity();
        } else {
            project = TestUtil.findAll(em, Project.class).get(0);
        }
        em.persist(project);
        em.flush();
        issue.setProject(project);
        issueRepository.saveAndFlush(issue);
        Long projectId = project.getId();
        // Get all the issueList where project equals to projectId
        defaultIssueShouldBeFound("projectId.equals=" + projectId);

        // Get all the issueList where project equals to (projectId + 1)
        defaultIssueShouldNotBeFound("projectId.equals=" + (projectId + 1));
    }

    private void defaultIssueFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultIssueShouldBeFound(shouldBeFound);
        defaultIssueShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultIssueShouldBeFound(String filter) throws Exception {
        restIssueMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(issue.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));

        // Check, that the count call also returns 1
        restIssueMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultIssueShouldNotBeFound(String filter) throws Exception {
        restIssueMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restIssueMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingIssue() throws Exception {
        // Get the issue
        restIssueMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingIssue() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the issue
        Issue updatedIssue = issueRepository.findById(issue.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedIssue are not directly saved in db
        em.detach(updatedIssue);
        updatedIssue
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .type(UPDATED_TYPE)
            .status(UPDATED_STATUS)
            .priority(UPDATED_PRIORITY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        IssueDTO issueDTO = issueMapper.toDto(updatedIssue);

        restIssueMockMvc
            .perform(
                put(ENTITY_API_URL_ID, issueDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO))
            )
            .andExpect(status().isOk());

        // Validate the Issue in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedIssueToMatchAllProperties(updatedIssue);
    }

    @Test
    @Transactional
    @WithMockUser(username = "developer", authorities = { "ROLE_DEVELOPER" })
    void putExistingIssue_asAssignedDeveloper_shouldSucceed() throws Exception {
        User assignee = createUser("developer");
        issue.setAssignee(assignee);
        insertedIssue = issueRepository.saveAndFlush(issue);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        Issue updatedIssue = issueRepository.findById(issue.getId()).orElseThrow();
        em.detach(updatedIssue);
        updatedIssue.title(UPDATED_TITLE).status(UPDATED_STATUS).updatedAt(UPDATED_UPDATED_AT);
        IssueDTO issueDTO = issueMapper.toDto(updatedIssue);

        restIssueMockMvc
            .perform(
                put(ENTITY_API_URL_ID, issueDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO))
            )
            .andExpect(status().isOk());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedIssue(issue).getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(getPersistedIssue(issue).getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    @WithMockUser(username = "developer", authorities = { "ROLE_DEVELOPER" })
    void patchIssueStatus_asAssignedDeveloper_shouldMoveIssueOnKanbanBoard() throws Exception {
        User assignee = createUser("developer");
        issue.setAssignee(assignee);
        insertedIssue = issueRepository.saveAndFlush(issue);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        Issue partialUpdatedIssue = new Issue();
        partialUpdatedIssue.setId(issue.getId());
        partialUpdatedIssue.status(IssueStatus.IN_PROGRESS);

        restIssueMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedIssue.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedIssue))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(IssueStatus.IN_PROGRESS.toString()));

        Issue persistedIssue = getPersistedIssue(issue);
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(persistedIssue.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
        assertThat(persistedIssue.getUpdatedAt()).isNotNull();
    }

    @Test
    @Transactional
    @WithMockUser(username = "developer", authorities = { "ROLE_DEVELOPER" })
    void putExistingIssue_asUnassignedDeveloper_shouldForbid() throws Exception {
        User assignee = createUser("otherdeveloper");
        issue.setAssignee(assignee);
        insertedIssue = issueRepository.saveAndFlush(issue);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        Issue updatedIssue = issueRepository.findById(issue.getId()).orElseThrow();
        em.detach(updatedIssue);
        updatedIssue.title(UPDATED_TITLE).status(UPDATED_STATUS).updatedAt(UPDATED_UPDATED_AT);
        IssueDTO issueDTO = issueMapper.toDto(updatedIssue);

        restIssueMockMvc
            .perform(
                put(ENTITY_API_URL_ID, issueDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO))
            )
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertThat(getPersistedIssue(issue).getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(getPersistedIssue(issue).getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    void putNonExistingIssue() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issue.setId(longCount.incrementAndGet());

        // Create the Issue
        IssueDTO issueDTO = issueMapper.toDto(issue);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restIssueMockMvc
            .perform(
                put(ENTITY_API_URL_ID, issueDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Issue in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchIssue() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issue.setId(longCount.incrementAndGet());

        // Create the Issue
        IssueDTO issueDTO = issueMapper.toDto(issue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIssueMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(issueDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Issue in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamIssue() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issue.setId(longCount.incrementAndGet());

        // Create the Issue
        IssueDTO issueDTO = issueMapper.toDto(issue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIssueMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Issue in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateIssueWithPatch() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the issue using partial update
        Issue partialUpdatedIssue = new Issue();
        partialUpdatedIssue.setId(issue.getId());

        partialUpdatedIssue.title(UPDATED_TITLE).description(UPDATED_DESCRIPTION).priority(UPDATED_PRIORITY).updatedAt(UPDATED_UPDATED_AT);

        restIssueMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedIssue.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedIssue))
            )
            .andExpect(status().isOk());

        // Validate the Issue in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertIssueUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedIssue, issue), getPersistedIssue(issue));
    }

    @Test
    @Transactional
    void fullUpdateIssueWithPatch() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the issue using partial update
        Issue partialUpdatedIssue = new Issue();
        partialUpdatedIssue.setId(issue.getId());

        partialUpdatedIssue
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .type(UPDATED_TYPE)
            .status(UPDATED_STATUS)
            .priority(UPDATED_PRIORITY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restIssueMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedIssue.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedIssue))
            )
            .andExpect(status().isOk());

        // Validate the Issue in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertIssueUpdatableFieldsEquals(partialUpdatedIssue, getPersistedIssue(partialUpdatedIssue));
    }

    @Test
    @Transactional
    void patchNonExistingIssue() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issue.setId(longCount.incrementAndGet());

        // Create the Issue
        IssueDTO issueDTO = issueMapper.toDto(issue);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restIssueMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, issueDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(issueDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Issue in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchIssue() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issue.setId(longCount.incrementAndGet());

        // Create the Issue
        IssueDTO issueDTO = issueMapper.toDto(issue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIssueMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(issueDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Issue in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamIssue() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        issue.setId(longCount.incrementAndGet());

        // Create the Issue
        IssueDTO issueDTO = issueMapper.toDto(issue);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIssueMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(issueDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Issue in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteIssue() throws Exception {
        // Initialize the database
        insertedIssue = issueRepository.saveAndFlush(issue);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the issue
        restIssueMockMvc
            .perform(delete(ENTITY_API_URL_ID, issue.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_DEVELOPER" })
    void deleteIssue_asDeveloper_shouldForbid() throws Exception {
        insertedIssue = issueRepository.saveAndFlush(issue);

        long databaseSizeBeforeDelete = getRepositoryCount();

        restIssueMockMvc
            .perform(delete(ENTITY_API_URL_ID, issue.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        assertSameRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_PROJET_MANAGER" })
    void createIssue_asProjetManager_shouldSucceed() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        IssueDTO issueDTO = issueMapper.toDto(issue);
        restIssueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO)))
            .andExpect(status().isCreated());
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_DEVELOPER" })
    void createIssue_asDeveloper_shouldSucceed() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        IssueDTO issueDTO = issueMapper.toDto(issue);
        restIssueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO)))
            .andExpect(status().isCreated());
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_USER" })
    void createIssue_asUser_shouldForbid() throws Exception {
        IssueDTO issueDTO = issueMapper.toDto(issue);
        restIssueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(issueDTO)))
            .andExpect(status().isForbidden());
    }

    protected long getRepositoryCount() {
        return issueRepository.count();
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

    protected Issue getPersistedIssue(Issue issue) {
        return issueRepository.findById(issue.getId()).orElseThrow();
    }

    private User createUser(String login) {
        User user = UserResourceIT.createEntity();
        user.setLogin(login);
        user.setEmail(login + "@localhost");
        em.persist(user);
        em.flush();
        return user;
    }

    protected void assertPersistedIssueToMatchAllProperties(Issue expectedIssue) {
        assertIssueAllPropertiesEquals(expectedIssue, getPersistedIssue(expectedIssue));
    }

    protected void assertPersistedIssueToMatchUpdatableProperties(Issue expectedIssue) {
        assertIssueAllUpdatablePropertiesEquals(expectedIssue, getPersistedIssue(expectedIssue));
    }
}
