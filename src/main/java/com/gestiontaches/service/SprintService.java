package com.gestiontaches.service;

import com.gestiontaches.domain.Sprint;
import com.gestiontaches.repository.SprintRepository;
import com.gestiontaches.service.dto.SprintDTO;
import com.gestiontaches.service.mapper.SprintMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.gestiontaches.domain.Sprint}.
 */
@Service
@Transactional
public class SprintService {

    private static final Logger LOG = LoggerFactory.getLogger(SprintService.class);

    private final SprintRepository sprintRepository;

    private final SprintMapper sprintMapper;

    public SprintService(SprintRepository sprintRepository, SprintMapper sprintMapper) {
        this.sprintRepository = sprintRepository;
        this.sprintMapper = sprintMapper;
    }

    /**
     * Save a sprint.
     *
     * @param sprintDTO the entity to save.
     * @return the persisted entity.
     */
    public SprintDTO save(SprintDTO sprintDTO) {
        LOG.debug("Request to save Sprint : {}", sprintDTO);
        Sprint sprint = sprintMapper.toEntity(sprintDTO);
        sprint = sprintRepository.save(sprint);
        return sprintMapper.toDto(sprint);
    }

    /**
     * Update a sprint.
     *
     * @param sprintDTO the entity to save.
     * @return the persisted entity.
     */
    public SprintDTO update(SprintDTO sprintDTO) {
        LOG.debug("Request to update Sprint : {}", sprintDTO);
        Sprint sprint = sprintMapper.toEntity(sprintDTO);
        sprint = sprintRepository.save(sprint);
        return sprintMapper.toDto(sprint);
    }

    /**
     * Partially update a sprint.
     *
     * @param sprintDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SprintDTO> partialUpdate(SprintDTO sprintDTO) {
        LOG.debug("Request to partially update Sprint : {}", sprintDTO);

        return sprintRepository
            .findById(sprintDTO.getId())
            .map(existingSprint -> {
                sprintMapper.partialUpdate(existingSprint, sprintDTO);

                return existingSprint;
            })
            .map(sprintRepository::save)
            .map(sprintMapper::toDto);
    }

    /**
     * Get all the sprints with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<SprintDTO> findAllWithEagerRelationships(Pageable pageable) {
        return sprintRepository.findAllWithEagerRelationships(pageable).map(sprintMapper::toDto);
    }

    /**
     * Get one sprint by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SprintDTO> findOne(Long id) {
        LOG.debug("Request to get Sprint : {}", id);
        return sprintRepository.findOneWithEagerRelationships(id).map(sprintMapper::toDto);
    }

    /**
     * Delete the sprint by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Sprint : {}", id);
        sprintRepository.deleteById(id);
    }
}
