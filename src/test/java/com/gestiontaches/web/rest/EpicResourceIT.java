package com.gestiontaches.web.rest;

import static com.gestiontaches.domain.EpicAsserts.*;
import static com.gestiontaches.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestiontaches.IntegrationTest;
import com.gestiontaches.domain.Epic;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.enumeration.EpicStatus;
import com.gestiontaches.domain.enumeration.Priority;
import com.gestiontaches.repository.EpicRepository;
import com.gestiontaches.service.EpicService;
import com.gestiontaches.service.dto.EpicDTO;
import com.gestiontaches.service.mapper.EpicMapper;
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
 * Integration tests for the {@link EpicResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = { "ROLE_ADMIN" })
class EpicResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final EpicStatus DEFAULT_STATUS = EpicStatus.TODO;
    private static final EpicStatus UPDATED_STATUS = EpicStatus.IN_PROGRESS;

    private static final Priority DEFAULT_PRIORITY = Priority.LOWEST;
    private static final Priority UPDATED_PRIORITY = Priority.LOW;

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/epics";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EpicRepository epicRepository;

    @Mock
    private EpicRepository epicRepositoryMock;

    @Autowired
    private EpicMapper epicMapper;

    @Mock
    private EpicService epicServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restEpicMockMvc;

    private Epic epic;

    private Epic insertedEpic;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Epic createEntity(EntityManager em) {
        Epic epic = new Epic()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
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
        epic.setProject(project);
        return epic;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Epic createUpdatedEntity(EntityManager em) {
        Epic updatedEpic = new Epic()
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
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
        updatedEpic.setProject(project);
        return updatedEpic;
    }

    @BeforeEach
    void initTest() {
        epic = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedEpic != null) {
            epicRepository.delete(insertedEpic);
            insertedEpic = null;
        }
    }

    @Test
    @Transactional
    void createEpic() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Epic
        EpicDTO epicDTO = epicMapper.toDto(epic);
        var returnedEpicDTO = om.readValue(
            restEpicMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            EpicDTO.class
        );

        // Validate the Epic in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedEpic = epicMapper.toEntity(returnedEpicDTO);
        assertEpicUpdatableFieldsEquals(returnedEpic, getPersistedEpic(returnedEpic));

        insertedEpic = returnedEpic;
    }

    @Test
    @Transactional
    void createEpicWithExistingId() throws Exception {
        // Create the Epic with an existing ID
        epic.setId(1L);
        EpicDTO epicDTO = epicMapper.toDto(epic);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restEpicMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Epic in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        epic.setTitle(null);

        // Create the Epic, which fails.
        EpicDTO epicDTO = epicMapper.toDto(epic);

        restEpicMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkStatusIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        epic.setStatus(null);

        // Create the Epic, which fails.
        EpicDTO epicDTO = epicMapper.toDto(epic);

        restEpicMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPriorityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        epic.setPriority(null);

        // Create the Epic, which fails.
        EpicDTO epicDTO = epicMapper.toDto(epic);

        restEpicMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        epic.setCreatedAt(null);

        // Create the Epic, which fails.
        EpicDTO epicDTO = epicMapper.toDto(epic);

        restEpicMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllEpics() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList
        restEpicMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(epic.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEpicsWithEagerRelationshipsIsEnabled() throws Exception {
        when(epicServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEpicMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(epicServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllEpicsWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(epicServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restEpicMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(epicRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getEpic() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get the epic
        restEpicMockMvc
            .perform(get(ENTITY_API_URL_ID, epic.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(epic.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.priority").value(DEFAULT_PRIORITY.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()));
    }

    @Test
    @Transactional
    void getEpicsByIdFiltering() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        Long id = epic.getId();

        defaultEpicFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultEpicFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultEpicFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllEpicsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where title equals to
        defaultEpicFiltering("title.equals=" + DEFAULT_TITLE, "title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllEpicsByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where title in
        defaultEpicFiltering("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE, "title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllEpicsByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where title is not null
        defaultEpicFiltering("title.specified=true", "title.specified=false");
    }

    @Test
    @Transactional
    void getAllEpicsByTitleContainsSomething() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where title contains
        defaultEpicFiltering("title.contains=" + DEFAULT_TITLE, "title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllEpicsByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where title does not contain
        defaultEpicFiltering("title.doesNotContain=" + UPDATED_TITLE, "title.doesNotContain=" + DEFAULT_TITLE);
    }

    @Test
    @Transactional
    void getAllEpicsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where description equals to
        defaultEpicFiltering("description.equals=" + DEFAULT_DESCRIPTION, "description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllEpicsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where description in
        defaultEpicFiltering("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION, "description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllEpicsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where description is not null
        defaultEpicFiltering("description.specified=true", "description.specified=false");
    }

    @Test
    @Transactional
    void getAllEpicsByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where description contains
        defaultEpicFiltering("description.contains=" + DEFAULT_DESCRIPTION, "description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllEpicsByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where description does not contain
        defaultEpicFiltering("description.doesNotContain=" + UPDATED_DESCRIPTION, "description.doesNotContain=" + DEFAULT_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllEpicsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where status equals to
        defaultEpicFiltering("status.equals=" + DEFAULT_STATUS, "status.equals=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllEpicsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where status in
        defaultEpicFiltering("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS, "status.in=" + UPDATED_STATUS);
    }

    @Test
    @Transactional
    void getAllEpicsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where status is not null
        defaultEpicFiltering("status.specified=true", "status.specified=false");
    }

    @Test
    @Transactional
    void getAllEpicsByPriorityIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where priority equals to
        defaultEpicFiltering("priority.equals=" + DEFAULT_PRIORITY, "priority.equals=" + UPDATED_PRIORITY);
    }

    @Test
    @Transactional
    void getAllEpicsByPriorityIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where priority in
        defaultEpicFiltering("priority.in=" + DEFAULT_PRIORITY + "," + UPDATED_PRIORITY, "priority.in=" + UPDATED_PRIORITY);
    }

    @Test
    @Transactional
    void getAllEpicsByPriorityIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where priority is not null
        defaultEpicFiltering("priority.specified=true", "priority.specified=false");
    }

    @Test
    @Transactional
    void getAllEpicsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where createdAt equals to
        defaultEpicFiltering("createdAt.equals=" + DEFAULT_CREATED_AT, "createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllEpicsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where createdAt in
        defaultEpicFiltering("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT, "createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllEpicsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where createdAt is not null
        defaultEpicFiltering("createdAt.specified=true", "createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllEpicsByUpdatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where updatedAt equals to
        defaultEpicFiltering("updatedAt.equals=" + DEFAULT_UPDATED_AT, "updatedAt.equals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllEpicsByUpdatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where updatedAt in
        defaultEpicFiltering("updatedAt.in=" + DEFAULT_UPDATED_AT + "," + UPDATED_UPDATED_AT, "updatedAt.in=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllEpicsByUpdatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        // Get all the epicList where updatedAt is not null
        defaultEpicFiltering("updatedAt.specified=true", "updatedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllEpicsByProjectIsEqualToSomething() throws Exception {
        Project project;
        if (TestUtil.findAll(em, Project.class).isEmpty()) {
            epicRepository.saveAndFlush(epic);
            project = ProjectResourceIT.createEntity();
        } else {
            project = TestUtil.findAll(em, Project.class).get(0);
        }
        em.persist(project);
        em.flush();
        epic.setProject(project);
        epicRepository.saveAndFlush(epic);
        Long projectId = project.getId();
        // Get all the epicList where project equals to projectId
        defaultEpicShouldBeFound("projectId.equals=" + projectId);

        // Get all the epicList where project equals to (projectId + 1)
        defaultEpicShouldNotBeFound("projectId.equals=" + (projectId + 1));
    }

    private void defaultEpicFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultEpicShouldBeFound(shouldBeFound);
        defaultEpicShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultEpicShouldBeFound(String filter) throws Exception {
        restEpicMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(epic.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].priority").value(hasItem(DEFAULT_PRIORITY.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())));

        // Check, that the count call also returns 1
        restEpicMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultEpicShouldNotBeFound(String filter) throws Exception {
        restEpicMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restEpicMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingEpic() throws Exception {
        // Get the epic
        restEpicMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingEpic() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the epic
        Epic updatedEpic = epicRepository.findById(epic.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedEpic are not directly saved in db
        em.detach(updatedEpic);
        updatedEpic
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .status(UPDATED_STATUS)
            .priority(UPDATED_PRIORITY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        EpicDTO epicDTO = epicMapper.toDto(updatedEpic);

        restEpicMockMvc
            .perform(put(ENTITY_API_URL_ID, epicDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isOk());

        // Validate the Epic in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedEpicToMatchAllProperties(updatedEpic);
    }

    @Test
    @Transactional
    void putNonExistingEpic() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        epic.setId(longCount.incrementAndGet());

        // Create the Epic
        EpicDTO epicDTO = epicMapper.toDto(epic);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEpicMockMvc
            .perform(put(ENTITY_API_URL_ID, epicDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Epic in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchEpic() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        epic.setId(longCount.incrementAndGet());

        // Create the Epic
        EpicDTO epicDTO = epicMapper.toDto(epic);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEpicMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(epicDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Epic in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamEpic() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        epic.setId(longCount.incrementAndGet());

        // Create the Epic
        EpicDTO epicDTO = epicMapper.toDto(epic);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEpicMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Epic in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateEpicWithPatch() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the epic using partial update
        Epic partialUpdatedEpic = new Epic();
        partialUpdatedEpic.setId(epic.getId());

        partialUpdatedEpic.description(UPDATED_DESCRIPTION).status(UPDATED_STATUS).createdAt(UPDATED_CREATED_AT);

        restEpicMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEpic.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEpic))
            )
            .andExpect(status().isOk());

        // Validate the Epic in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEpicUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedEpic, epic), getPersistedEpic(epic));
    }

    @Test
    @Transactional
    void fullUpdateEpicWithPatch() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the epic using partial update
        Epic partialUpdatedEpic = new Epic();
        partialUpdatedEpic.setId(epic.getId());

        partialUpdatedEpic
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .status(UPDATED_STATUS)
            .priority(UPDATED_PRIORITY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restEpicMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedEpic.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedEpic))
            )
            .andExpect(status().isOk());

        // Validate the Epic in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertEpicUpdatableFieldsEquals(partialUpdatedEpic, getPersistedEpic(partialUpdatedEpic));
    }

    @Test
    @Transactional
    void patchNonExistingEpic() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        epic.setId(longCount.incrementAndGet());

        // Create the Epic
        EpicDTO epicDTO = epicMapper.toDto(epic);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restEpicMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, epicDTO.getId()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(epicDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Epic in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchEpic() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        epic.setId(longCount.incrementAndGet());

        // Create the Epic
        EpicDTO epicDTO = epicMapper.toDto(epic);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEpicMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(epicDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Epic in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamEpic() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        epic.setId(longCount.incrementAndGet());

        // Create the Epic
        EpicDTO epicDTO = epicMapper.toDto(epic);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restEpicMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Epic in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteEpic() throws Exception {
        // Initialize the database
        insertedEpic = epicRepository.saveAndFlush(epic);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the epic
        restEpicMockMvc
            .perform(delete(ENTITY_API_URL_ID, epic.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_PROJET_MANAGER" })
    void createEpic_asProjetManager_shouldSucceed() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        EpicDTO epicDTO = epicMapper.toDto(epic);
        restEpicMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isCreated());
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_DEVELOPER" })
    void createEpic_asDeveloper_shouldForbid() throws Exception {
        EpicDTO epicDTO = epicMapper.toDto(epic);
        restEpicMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_USER" })
    void createEpic_asUser_shouldForbid() throws Exception {
        EpicDTO epicDTO = epicMapper.toDto(epic);
        restEpicMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(epicDTO)))
            .andExpect(status().isForbidden());
    }

    protected long getRepositoryCount() {
        return epicRepository.count();
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

    protected Epic getPersistedEpic(Epic epic) {
        return epicRepository.findById(epic.getId()).orElseThrow();
    }

    protected void assertPersistedEpicToMatchAllProperties(Epic expectedEpic) {
        assertEpicAllPropertiesEquals(expectedEpic, getPersistedEpic(expectedEpic));
    }

    protected void assertPersistedEpicToMatchUpdatableProperties(Epic expectedEpic) {
        assertEpicAllUpdatablePropertiesEquals(expectedEpic, getPersistedEpic(expectedEpic));
    }
}
