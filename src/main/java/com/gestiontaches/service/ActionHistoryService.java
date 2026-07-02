package com.gestiontaches.service;

import com.gestiontaches.domain.ActionHistory;
import com.gestiontaches.repository.ActionHistoryRepository;
import com.gestiontaches.service.dto.ActionHistoryDTO;
import com.gestiontaches.service.mapper.ActionHistoryMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.gestiontaches.domain.ActionHistory}.
 */
@Service
@Transactional
public class ActionHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(ActionHistoryService.class);

    private final ActionHistoryRepository actionHistoryRepository;

    private final ActionHistoryMapper actionHistoryMapper;

    public ActionHistoryService(ActionHistoryRepository actionHistoryRepository, ActionHistoryMapper actionHistoryMapper) {
        this.actionHistoryRepository = actionHistoryRepository;
        this.actionHistoryMapper = actionHistoryMapper;
    }

    /**
     * Save a actionHistory.
     *
     * @param actionHistoryDTO the entity to save.
     * @return the persisted entity.
     */
    public ActionHistoryDTO save(ActionHistoryDTO actionHistoryDTO) {
        LOG.debug("Request to save ActionHistory : {}", actionHistoryDTO);
        ActionHistory actionHistory = actionHistoryMapper.toEntity(actionHistoryDTO);
        actionHistory = actionHistoryRepository.save(actionHistory);
        return actionHistoryMapper.toDto(actionHistory);
    }

    /**
     * Update a actionHistory.
     *
     * @param actionHistoryDTO the entity to save.
     * @return the persisted entity.
     */
    public ActionHistoryDTO update(ActionHistoryDTO actionHistoryDTO) {
        LOG.debug("Request to update ActionHistory : {}", actionHistoryDTO);
        ActionHistory actionHistory = actionHistoryMapper.toEntity(actionHistoryDTO);
        actionHistory = actionHistoryRepository.save(actionHistory);
        return actionHistoryMapper.toDto(actionHistory);
    }

    /**
     * Partially update a actionHistory.
     *
     * @param actionHistoryDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ActionHistoryDTO> partialUpdate(ActionHistoryDTO actionHistoryDTO) {
        LOG.debug("Request to partially update ActionHistory : {}", actionHistoryDTO);

        return actionHistoryRepository
            .findById(actionHistoryDTO.getId())
            .map(existingActionHistory -> {
                actionHistoryMapper.partialUpdate(existingActionHistory, actionHistoryDTO);

                return existingActionHistory;
            })
            .map(actionHistoryRepository::save)
            .map(actionHistoryMapper::toDto);
    }

    /**
     * Get all the actionHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ActionHistoryDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all ActionHistories");
        return actionHistoryRepository.findAll(pageable).map(actionHistoryMapper::toDto);
    }

    /**
     * Get one actionHistory by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ActionHistoryDTO> findOne(Long id) {
        LOG.debug("Request to get ActionHistory : {}", id);
        return actionHistoryRepository.findById(id).map(actionHistoryMapper::toDto);
    }

    /**
     * Delete the actionHistory by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete ActionHistory : {}", id);
        actionHistoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ActionHistoryDTO> findByIssueId(Long issueId) {
        LOG.debug("Request to get ActionHistories for Issue : {}", issueId);
        return actionHistoryRepository.findByIssueIdOrderByCreatedAtDesc(issueId).stream().map(actionHistoryMapper::toDto).toList();
    }
}
