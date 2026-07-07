package com.gestiontaches.web.rest;

import static com.gestiontaches.domain.SprintAsserts.*;
import static com.gestiontaches.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestiontaches.IntegrationTest;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.Sprint;
import com.gestiontaches.domain.enumeration.SprintStatus;
import com.gestiontaches.repository.SprintRepository;
import com.gestiontaches.service.SprintService;
import com.gestiontaches.service.dto.SprintDTO;
import com.gestiontaches.service.mapper.SprintMapper;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
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
 * Integration tests for the {@link SprintResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = { "ROLE_ADMIN" })
class SprintResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_GOAL = "AAAAAAAAAA";
    private static final String UPDATED_GOAL = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_START_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_START_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_START_DATE = LocalDate.ofEpochDay(-1L);

    private static final LocalDate DEFAULT_END_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_END_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_END_DATE = LocalDate.ofEpochDay(-1L);

    private static final SprintStatus DEFAULT_STATUS = SprintStatus.PLANNED;
    private static final SprintStatus UPDATED_STATUS = SprintStatus.ACTIVE;

    private static final String ENTITY_API_URL = "/api/sprints";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SprintRepository sprintRepository;

    @Mock
    private SprintRepository sprintRepositoryMock;

    @Autowired
    private SprintMapper sprintMapper;

    @Mock
    private SprintService sprintServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSprintMockMvc;

    private Sprint sprint;

    private Sprint insertedSprint;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sprint createEntity(EntityManager em) {
        Sprint sprint = new Sprint()
            .name(DEFAULT_NAME)
            .goal(DEFAULT_GOAL)
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE)
            .status(DEFAULT_STATUS);
        // Add required entity
        Project project;
        if (TestUtil.findAll(em, Project.class).isEmpty()) {
            project = ProjectResourceIT.createEntity();
            em.persist(project);
            em.flush();
        } else {
            project = TestUtil.findAll(em, Project.class).get(0);
        }
        sprint.setProject(project);
        return sprint;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Sprint createUpdatedEntity(EntityManager em) {
        Sprint updatedSprint = new Sprint()
            .name(UPDATED_NAME)
            .goal(UPDATED_GOAL)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .status(UPDATED_STATUS);
        // Add required entity
        Project project;
        if (TestUtil.findAll(em, Project.class).isEmpty()) {
            project = ProjectResourceIT.createUpdatedEntity();
            em.persist(project);
            em.flush();
        } else {
            project = TestUtil.findAll(em, Project.class).get(0);
        }
        updatedSprint.setProject(project);
        return updatedSprint;
    }

    @BeforeEach
    void initTest() {
        sprint = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedSprint != null) {
            sprintRepository.delete(insertedSprint);
            insertedSprint = null;
        }
    }

    @Test
    @Transactional
    void createSprint() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Sprint
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);
        var returnedSprintDTO = om.readValue(
            restSprintMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sprintDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            SprintDTO.class
        );

        // Validate the Sprint in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedSprint = sprintMapper.toEntity(returnedSprintDTO);
        assertSprintUpdatableFieldsEquals(returnedSprint, getPersistedSprint(returnedSprint));

        insertedSprint = returnedSprint;
    }

    @Test
    @Transactional
    void createSprintWithExistingId() throws Exception {
        // Create the Sprint with an existing ID
        sprint.setId(1L);
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSprintMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sprintDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Sprint in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        sprint.setName(null);

        // Create the Sprint, which fails.
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);

        restSprintMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sprintDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        sprint.setStatus(null);

        // Create the Sprint, which fails.
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);

        restSprintMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sprintDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSprints() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList
        restSprintMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sprint.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].goal").value(hasItem(DEFAULT_GOAL)))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSprintsWithEagerRelationshipsIsEnabled() throws Exception {
        when(sprintServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSprintMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(sprintServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSprintsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(sprintServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restSprintMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(sprintRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getSprint() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get the sprint
        restSprintMockMvc
            .perform(get(ENTITY_API_URL_ID, sprint.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(sprint.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.goal").value(DEFAULT_GOAL))
            .andExpect(jsonPath("$.startDate").value(DEFAULT_START_DATE.toString()))
            .andExpect(jsonPath("$.endDate").value(DEFAULT_END_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    void getSprintsByIdFiltering() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        Long id = sprint.getId();

        defaultSprintFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultSprintFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultSprintFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSprintsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where name equals to
        defaultSprintFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSprintsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where name in
        defaultSprintFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSprintsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where name is not null
        defaultSprintFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllSprintsByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where name contains
        defaultSprintFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSprintsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where name does not contain
        defaultSprintFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    @Test
    @Transactional
    void getAllSprintsByGoalIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where goal equals to
        defaultSprintFiltering("goal.equals=" + DEFAULT_GOAL, "goal.equals=" + UPDATED_GOAL);
    }

    @Test
    @Transactional
    void getAllSprintsByGoalIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where goal in
        defaultSprintFiltering("goal.in=" + DEFAULT_GOAL + "," + UPDATED_GOAL, "goal.in=" + UPDATED_GOAL);
    }

    @Test
    @Transactional
    void getAllSprintsByGoalIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where goal is not null
        defaultSprintFiltering("goal.specified=true", "goal.specified=false");
    }

    @Test
    @Transactional
    void getAllSprintsByGoalContainsSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where goal contains
        defaultSprintFiltering("goal.contains=" + DEFAULT_GOAL, "goal.contains=" + UPDATED_GOAL);
    }

    @Test
    @Transactional
    void getAllSprintsByGoalNotContainsSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where goal does not contain
        defaultSprintFiltering("goal.doesNotContain=" + UPDATED_GOAL, "goal.doesNotContain=" + DEFAULT_GOAL);
    }

    @Test
    @Transactional
    void getAllSprintsByStartDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where startDate equals to
        defaultSprintFiltering("startDate.equals=" + DEFAULT_START_DATE, "startDate.equals=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByStartDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where startDate in
        defaultSprintFiltering("startDate.in=" + DEFAULT_START_DATE + "," + UPDATED_START_DATE, "startDate.in=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByStartDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where startDate is not null
        defaultSprintFiltering("startDate.specified=true", "startDate.specified=false");
    }

    @Test
    @Transactional
    void getAllSprintsByStartDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where startDate is greater than or equal to
        defaultSprintFiltering("startDate.greaterThanOrEqual=" + DEFAULT_START_DATE, "startDate.greaterThanOrEqual=" + UPDATED_START_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByStartDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where startDate is less than or equal to
        defaultSprintFiltering("startDate.lessThanOrEqual=" + DEFAULT_START_DATE, "startDate.lessThanOrEqual=" + SMALLER_START_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByStartDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where startDate is less than
        defaultSprintFiltering("startDate.lessThan=" + UPDATED_START_DATE, "startDate.lessThan=" + DEFAULT_START_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByStartDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where startDate is greater than
        defaultSprintFiltering("startDate.greaterThan=" + SMALLER_START_DATE, "startDate.greaterThan=" + DEFAULT_START_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByEndDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where endDate equals to
        defaultSprintFiltering("endDate.equals=" + DEFAULT_END_DATE, "endDate.equals=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByEndDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where endDate in
        defaultSprintFiltering("endDate.in=" + DEFAULT_END_DATE + "," + UPDATED_END_DATE, "endDate.in=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByEndDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where endDate is not null
        defaultSprintFiltering("endDate.specified=true", "endDate.specified=false");
    }

    @Test
    @Transactional
    void getAllSprintsByEndDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where endDate is greater than or equal to
        defaultSprintFiltering("endDate.greaterThanOrEqual=" + DEFAULT_END_DATE, "endDate.greaterThanOrEqual=" + UPDATED_END_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByEndDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where endDate is less than or equal to
        defaultSprintFiltering("endDate.lessThanOrEqual=" + DEFAULT_END_DATE, "endDate.lessThanOrEqual=" + SMALLER_END_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByEndDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where endDate is less than
        defaultSprintFiltering("endDate.lessThan=" + UPDATED_END_DATE, "endDate.lessThan=" + DEFAULT_END_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByEndDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where endDate is greater than
        defaultSprintFiltering("endDate.greaterThan=" + SMALLER_END_DATE, "endDate.greaterThan=" + DEFAULT_END_DATE);
    }

    @Test
    @Transactional
    void getAllSprintsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where status equals to
        defaultSprintFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllSprintsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where status in
        defaultSprintFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllSprintsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        // Get all the sprintList where status is not null
        defaultSprintFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllSprintsByProjectIsEqualToSomething() throws Exception {
        Project project;
        if (TestUtil.findAll(em, Project.class).isEmpty()) {
            sprintRepository.saveAndFlush(sprint);
            project = ProjectResourceIT.createEntity();
        } else {
            project = TestUtil.findAll(em, Project.class).get(0);
        }
        em.persist(project);
        em.flush();
        sprint.setProject(project);
        sprintRepository.saveAndFlush(sprint);
        Long projectId = project.getId();
        // Get all the sprintList where project equals to projectId
        defaultSprintShouldBeFound("projectId.equals=" + projectId);

        // Get all the sprintList where project equals to (projectId + 1)
        defaultSprintShouldNotBeFound("projectId.equals=" + (projectId + 1));
    }

    private void defaultSprintFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultSprintShouldBeFound(shouldBeFound);
        defaultSprintShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSprintShouldBeFound(String filter) throws Exception {
        restSprintMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(sprint.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].goal").value(hasItem(DEFAULT_GOAL)))
            .andExpect(jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString())))
            .andExpect(jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));

        // Check, that the count call also returns 1
        restSprintMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSprintShouldNotBeFound(String filter) throws Exception {
        restSprintMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSprintMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSprint() throws Exception {
        // Get the sprint
        restSprintMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSprint() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the sprint
        Sprint updatedSprint = sprintRepository.findById(sprint.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedSprint are not directly saved in db
        em.detach(updatedSprint);
        updatedSprint.name(UPDATED_NAME).goal(UPDATED_GOAL).startDate(UPDATED_START_DATE).endDate(UPDATED_END_DATE).status(UPDATED_STATUS);
        SprintDTO sprintDTO = sprintMapper.toDto(updatedSprint);

        restSprintMockMvc
            .perform(
                put(ENTITY_API_URL_ID, sprintDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sprintDTO))
            )
            .andExpect(status().isOk());

        // Validate the Sprint in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSprintToMatchAllProperties(updatedSprint);
    }

    @Test
    @Transactional
    void putNonExistingSprint() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sprint.setId(longCount.incrementAndGet());

        // Create the Sprint
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSprintMockMvc
            .perform(
                put(ENTITY_API_URL_ID, sprintDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sprintDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sprint in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSprint() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sprint.setId(longCount.incrementAndGet());

        // Create the Sprint
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSprintMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(sprintDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sprint in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSprint() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sprint.setId(longCount.incrementAndGet());

        // Create the Sprint
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSprintMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sprintDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Sprint in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSprintWithPatch() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the sprint using partial update
        Sprint partialUpdatedSprint = new Sprint();
        partialUpdatedSprint.setId(sprint.getId());

        partialUpdatedSprint.name(UPDATED_NAME).startDate(UPDATED_START_DATE).status(UPDATED_STATUS);

        restSprintMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSprint.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSprint))
            )
            .andExpect(status().isOk());

        // Validate the Sprint in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSprintUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedSprint, sprint), getPersistedSprint(sprint));
    }

    @Test
    @Transactional
    void fullUpdateSprintWithPatch() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the sprint using partial update
        Sprint partialUpdatedSprint = new Sprint();
        partialUpdatedSprint.setId(sprint.getId());

        partialUpdatedSprint
            .name(UPDATED_NAME)
            .goal(UPDATED_GOAL)
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .status(UPDATED_STATUS);

        restSprintMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSprint.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedSprint))
            )
            .andExpect(status().isOk());

        // Validate the Sprint in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSprintUpdatableFieldsEquals(partialUpdatedSprint, getPersistedSprint(partialUpdatedSprint));
    }

    @Test
    @Transactional
    void patchNonExistingSprint() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sprint.setId(longCount.incrementAndGet());

        // Create the Sprint
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSprintMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, sprintDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(sprintDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sprint in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSprint() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sprint.setId(longCount.incrementAndGet());

        // Create the Sprint
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSprintMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(sprintDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Sprint in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSprint() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        sprint.setId(longCount.incrementAndGet());

        // Create the Sprint
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSprintMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(sprintDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Sprint in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSprint() throws Exception {
        // Initialize the database
        insertedSprint = sprintRepository.saveAndFlush(sprint);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the sprint
        restSprintMockMvc
            .perform(delete(ENTITY_API_URL_ID, sprint.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_PROJET_MANAGER" })
    void createSprint_asProjetManager_shouldSucceed() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);
        restSprintMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sprintDTO)))
            .andExpect(status().isCreated());
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_DEVELOPER" })
    void createSprint_asDeveloper_shouldForbid() throws Exception {
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);
        restSprintMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sprintDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_USER" })
    void createSprint_asUser_shouldForbid() throws Exception {
        SprintDTO sprintDTO = sprintMapper.toDto(sprint);
        restSprintMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(sprintDTO)))
            .andExpect(status().isForbidden());
    }

    protected long getRepositoryCount() {
        return sprintRepository.count();
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

    protected Sprint getPersistedSprint(Sprint sprint) {
        return sprintRepository.findById(sprint.getId()).orElseThrow();
    }

    protected void assertPersistedSprintToMatchAllProperties(Sprint expectedSprint) {
        assertSprintAllPropertiesEquals(expectedSprint, getPersistedSprint(expectedSprint));
    }

    protected void assertPersistedSprintToMatchUpdatableProperties(Sprint expectedSprint) {
        assertSprintAllUpdatablePropertiesEquals(expectedSprint, getPersistedSprint(expectedSprint));
    }
}
