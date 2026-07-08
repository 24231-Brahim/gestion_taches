package com.gestiontaches.service;

import com.gestiontaches.domain.Sprint;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.domain.enumeration.SprintStatus;
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

    private final ProjectPermissionService projectPermissionService;

    public SprintService(SprintRepository sprintRepository, SprintMapper sprintMapper, ProjectPermissionService projectPermissionService) {
        this.sprintRepository = sprintRepository;
        this.sprintMapper = sprintMapper;
        this.projectPermissionService = projectPermissionService;
    }

    /**
     * Save a sprint.
     *
     * @param sprintDTO the entity to save.
     * @return the persisted entity.
     */
    public SprintDTO save(SprintDTO sprintDTO) {
        LOG.debug("Request to save Sprint : {}", sprintDTO);
        if (sprintDTO.getProject() != null && sprintDTO.getProject().getId() != null) {
            projectPermissionService.requireProjectRole(sprintDTO.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
        }
        validateSingleActiveSprint(sprintDTO);
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
        if (sprintDTO.getProject() != null && sprintDTO.getProject().getId() != null) {
            projectPermissionService.requireProjectRole(sprintDTO.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
        }
        validateSingleActiveSprint(sprintDTO);
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
                projectPermissionService.requireProjectRole(existingSprint.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
                sprintMapper.partialUpdate(existingSprint, sprintDTO);
                validateSingleActiveSprint(sprintMapper.toDto(existingSprint));
                return existingSprint;
            })
            .map(sprintRepository::save)
            .map(sprintMapper::toDto);
    }

    private void validateSingleActiveSprint(SprintDTO sprintDTO) {
        if (sprintDTO.getStatus() == SprintStatus.ACTIVE && sprintDTO.getProject() != null && sprintDTO.getProject().getId() != null) {
            Optional<Sprint> existingActive = sprintRepository.findByProjectIdAndStatus(
                sprintDTO.getProject().getId(),
                SprintStatus.ACTIVE
            );
            existingActive.ifPresent(s -> {
                if (!s.getId().equals(sprintDTO.getId())) {
                    throw new RuntimeException("A project can only have one active sprint at a time");
                }
            });
        }
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
        Sprint sprint = sprintRepository.findById(id).orElseThrow(() -> new RuntimeException("Sprint not found"));
        projectPermissionService.requireProjectRole(sprint.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
        sprintRepository.deleteById(id);
    }
}
