package com.gestiontaches.web.rest;

import static com.gestiontaches.domain.ActionHistoryAsserts.*;
import static com.gestiontaches.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestiontaches.IntegrationTest;
import com.gestiontaches.domain.ActionHistory;
import com.gestiontaches.domain.Issue;
import com.gestiontaches.repository.ActionHistoryRepository;
import com.gestiontaches.service.dto.ActionHistoryDTO;
import com.gestiontaches.service.mapper.ActionHistoryMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ActionHistoryResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(authorities = { "ROLE_ADMIN" })
class ActionHistoryResourceIT {

    private static final String DEFAULT_ACTION = "AAAAAAAAAA";
    private static final String UPDATED_ACTION = "BBBBBBBBBB";

    private static final String DEFAULT_FIELD_CHANGED = "AAAAAAAAAA";
    private static final String UPDATED_FIELD_CHANGED = "BBBBBBBBBB";

    private static final String DEFAULT_OLD_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_OLD_VALUE = "BBBBBBBBBB";

    private static final String DEFAULT_NEW_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_NEW_VALUE = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/action-histories";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ActionHistoryRepository actionHistoryRepository;

    @Autowired
    private ActionHistoryMapper actionHistoryMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restActionHistoryMockMvc;

    private ActionHistory actionHistory;

    private ActionHistory insertedActionHistory;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ActionHistory createEntity(EntityManager em) {
        ActionHistory actionHistory = new ActionHistory()
            .action(DEFAULT_ACTION)
            .fieldChanged(DEFAULT_FIELD_CHANGED)
            .oldValue(DEFAULT_OLD_VALUE)
            .newValue(DEFAULT_NEW_VALUE)
            .createdAt(DEFAULT_CREATED_AT);
        // Add required entity
        Issue issue;
        if (TestUtil.findAll(em, Issue.class).isEmpty()) {
            issue = IssueResourceIT.createEntity(em);
            em.persist(issue);
            em.flush();
        } else {
            issue = TestUtil.findAll(em, Issue.class).get(0);
        }
        actionHistory.setIssue(issue);
        return actionHistory;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ActionHistory createUpdatedEntity(EntityManager em) {
        ActionHistory updatedActionHistory = new ActionHistory()
            .action(UPDATED_ACTION)
            .fieldChanged(UPDATED_FIELD_CHANGED)
            .oldValue(UPDATED_OLD_VALUE)
            .newValue(UPDATED_NEW_VALUE)
            .createdAt(UPDATED_CREATED_AT);
        // Add required entity
        Issue issue;
        if (TestUtil.findAll(em, Issue.class).isEmpty()) {
            issue = IssueResourceIT.createUpdatedEntity(em);
            em.persist(issue);
            em.flush();
        } else {
            issue = TestUtil.findAll(em, Issue.class).get(0);
        }
        updatedActionHistory.setIssue(issue);
        return updatedActionHistory;
    }

    @BeforeEach
    void initTest() {
        actionHistory = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedActionHistory != null) {
            actionHistoryRepository.delete(insertedActionHistory);
            insertedActionHistory = null;
        }
    }

    @Test
    @Transactional
    void createActionHistory() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the ActionHistory
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);
        var returnedActionHistoryDTO = om.readValue(
            restActionHistoryMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(actionHistoryDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ActionHistoryDTO.class
        );

        // Validate the ActionHistory in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedActionHistory = actionHistoryMapper.toEntity(returnedActionHistoryDTO);
        assertActionHistoryUpdatableFieldsEquals(returnedActionHistory, getPersistedActionHistory(returnedActionHistory));

        insertedActionHistory = returnedActionHistory;
    }

    @Test
    @Transactional
    void createActionHistoryWithExistingId() throws Exception {
        // Create the ActionHistory with an existing ID
        actionHistory.setId(1L);
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restActionHistoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(actionHistoryDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ActionHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkActionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        actionHistory.setAction(null);

        // Create the ActionHistory, which fails.
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);

        restActionHistoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(actionHistoryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCreatedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        actionHistory.setCreatedAt(null);

        // Create the ActionHistory, which fails.
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);

        restActionHistoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(actionHistoryDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllActionHistories() throws Exception {
        // Initialize the database
        insertedActionHistory = actionHistoryRepository.saveAndFlush(actionHistory);

        // Get all the actionHistoryList
        restActionHistoryMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(actionHistory.getId().intValue())))
            .andExpect(jsonPath("$.[*].action").value(hasItem(DEFAULT_ACTION)))
            .andExpect(jsonPath("$.[*].fieldChanged").value(hasItem(DEFAULT_FIELD_CHANGED)))
            .andExpect(jsonPath("$.[*].oldValue").value(hasItem(DEFAULT_OLD_VALUE)))
            .andExpect(jsonPath("$.[*].newValue").value(hasItem(DEFAULT_NEW_VALUE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())));
    }

    @Test
    @Transactional
    void getActionHistory() throws Exception {
        // Initialize the database
        insertedActionHistory = actionHistoryRepository.saveAndFlush(actionHistory);

        // Get the actionHistory
        restActionHistoryMockMvc
            .perform(get(ENTITY_API_URL_ID, actionHistory.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(actionHistory.getId().intValue()))
            .andExpect(jsonPath("$.action").value(DEFAULT_ACTION))
            .andExpect(jsonPath("$.fieldChanged").value(DEFAULT_FIELD_CHANGED))
            .andExpect(jsonPath("$.oldValue").value(DEFAULT_OLD_VALUE))
            .andExpect(jsonPath("$.newValue").value(DEFAULT_NEW_VALUE))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()));
    }

    @Test
    @Transactional
    void getNonExistingActionHistory() throws Exception {
        // Get the actionHistory
        restActionHistoryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingActionHistory() throws Exception {
        // Initialize the database
        insertedActionHistory = actionHistoryRepository.saveAndFlush(actionHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the actionHistory
        ActionHistory updatedActionHistory = actionHistoryRepository.findById(actionHistory.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedActionHistory are not directly saved in db
        em.detach(updatedActionHistory);
        updatedActionHistory
            .action(UPDATED_ACTION)
            .fieldChanged(UPDATED_FIELD_CHANGED)
            .oldValue(UPDATED_OLD_VALUE)
            .newValue(UPDATED_NEW_VALUE)
            .createdAt(UPDATED_CREATED_AT);
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(updatedActionHistory);

        restActionHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, actionHistoryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(actionHistoryDTO))
            )
            .andExpect(status().isOk());

        // Validate the ActionHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedActionHistoryToMatchAllProperties(updatedActionHistory);
    }

    @Test
    @Transactional
    void putNonExistingActionHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        actionHistory.setId(longCount.incrementAndGet());

        // Create the ActionHistory
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restActionHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, actionHistoryDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(actionHistoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ActionHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchActionHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        actionHistory.setId(longCount.incrementAndGet());

        // Create the ActionHistory
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restActionHistoryMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(actionHistoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ActionHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamActionHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        actionHistory.setId(longCount.incrementAndGet());

        // Create the ActionHistory
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restActionHistoryMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(actionHistoryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ActionHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateActionHistoryWithPatch() throws Exception {
        // Initialize the database
        insertedActionHistory = actionHistoryRepository.saveAndFlush(actionHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the actionHistory using partial update
        ActionHistory partialUpdatedActionHistory = new ActionHistory();
        partialUpdatedActionHistory.setId(actionHistory.getId());

        partialUpdatedActionHistory.fieldChanged(UPDATED_FIELD_CHANGED).oldValue(UPDATED_OLD_VALUE).newValue(UPDATED_NEW_VALUE);

        restActionHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedActionHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedActionHistory))
            )
            .andExpect(status().isOk());

        // Validate the ActionHistory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertActionHistoryUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedActionHistory, actionHistory),
            getPersistedActionHistory(actionHistory)
        );
    }

    @Test
    @Transactional
    void fullUpdateActionHistoryWithPatch() throws Exception {
        // Initialize the database
        insertedActionHistory = actionHistoryRepository.saveAndFlush(actionHistory);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the actionHistory using partial update
        ActionHistory partialUpdatedActionHistory = new ActionHistory();
        partialUpdatedActionHistory.setId(actionHistory.getId());

        partialUpdatedActionHistory
            .action(UPDATED_ACTION)
            .fieldChanged(UPDATED_FIELD_CHANGED)
            .oldValue(UPDATED_OLD_VALUE)
            .newValue(UPDATED_NEW_VALUE)
            .createdAt(UPDATED_CREATED_AT);

        restActionHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedActionHistory.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedActionHistory))
            )
            .andExpect(status().isOk());

        // Validate the ActionHistory in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertActionHistoryUpdatableFieldsEquals(partialUpdatedActionHistory, getPersistedActionHistory(partialUpdatedActionHistory));
    }

    @Test
    @Transactional
    void patchNonExistingActionHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        actionHistory.setId(longCount.incrementAndGet());

        // Create the ActionHistory
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restActionHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, actionHistoryDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(actionHistoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ActionHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchActionHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        actionHistory.setId(longCount.incrementAndGet());

        // Create the ActionHistory
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restActionHistoryMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(actionHistoryDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the ActionHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamActionHistory() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        actionHistory.setId(longCount.incrementAndGet());

        // Create the ActionHistory
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restActionHistoryMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(actionHistoryDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the ActionHistory in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteActionHistory() throws Exception {
        // Initialize the database
        insertedActionHistory = actionHistoryRepository.saveAndFlush(actionHistory);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the actionHistory
        restActionHistoryMockMvc
            .perform(delete(ENTITY_API_URL_ID, actionHistory.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_DEVELOPER" })
    void createActionHistory_asDeveloper_shouldSucceed() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);
        restActionHistoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(actionHistoryDTO)))
            .andExpect(status().isCreated());
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = { "ROLE_USER" })
    void createActionHistory_asUser_shouldForbid() throws Exception {
        ActionHistoryDTO actionHistoryDTO = actionHistoryMapper.toDto(actionHistory);
        restActionHistoryMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(actionHistoryDTO)))
            .andExpect(status().isForbidden());
    }

    protected long getRepositoryCount() {
        return actionHistoryRepository.count();
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

    protected ActionHistory getPersistedActionHistory(ActionHistory actionHistory) {
        return actionHistoryRepository.findById(actionHistory.getId()).orElseThrow();
    }

    protected void assertPersistedActionHistoryToMatchAllProperties(ActionHistory expectedActionHistory) {
        assertActionHistoryAllPropertiesEquals(expectedActionHistory, getPersistedActionHistory(expectedActionHistory));
    }

    protected void assertPersistedActionHistoryToMatchUpdatableProperties(ActionHistory expectedActionHistory) {
        assertActionHistoryAllUpdatablePropertiesEquals(expectedActionHistory, getPersistedActionHistory(expectedActionHistory));
    }
}
