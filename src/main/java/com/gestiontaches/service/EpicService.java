package com.gestiontaches.service;

import com.gestiontaches.domain.Epic;
import com.gestiontaches.domain.enumeration.EpicStatus;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.repository.EpicRepository;
import com.gestiontaches.service.dto.EntityChangeEvent;
import com.gestiontaches.service.dto.EntityEventType;
import com.gestiontaches.service.dto.EpicDTO;
import com.gestiontaches.service.mapper.EpicMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.gestiontaches.domain.Epic}.
 */
@Service
@Transactional
public class EpicService {

    private static final Logger LOG = LoggerFactory.getLogger(EpicService.class);

    private final EpicRepository epicRepository;

    private final EpicMapper epicMapper;

    private final ProjectPermissionService projectPermissionService;

    private final EntityEventSseService entityEventSseService;

    public EpicService(
        EpicRepository epicRepository,
        EpicMapper epicMapper,
        ProjectPermissionService projectPermissionService,
        EntityEventSseService entityEventSseService
    ) {
        this.epicRepository = epicRepository;
        this.epicMapper = epicMapper;
        this.projectPermissionService = projectPermissionService;
        this.entityEventSseService = entityEventSseService;
    }

    /**
     * Save a epic.
     *
     * @param epicDTO the entity to save.
     * @return the persisted entity.
     */
    public EpicDTO save(EpicDTO epicDTO) {
        LOG.debug("Request to save Epic : {}", epicDTO);
        if (epicDTO.getProject() != null && epicDTO.getProject().getId() != null) {
            projectPermissionService.requireProjectRole(epicDTO.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
        }
        if (epicDTO.getStatus() == null) {
            epicDTO.setStatus(EpicStatus.TODO);
        }
        Epic epic = epicMapper.toEntity(epicDTO);
        epic = epicRepository.save(epic);
        EpicDTO result = epicMapper.toDto(epic);
        Long projectId = result.getProject() != null ? result.getProject().getId() : null;
        String eventType = epicDTO.getId() != null ? EntityEventType.UPDATED : EntityEventType.CREATED;
        entityEventSseService.sendEvent(new EntityChangeEvent(EntityEventType.ENTITY_EPIC, eventType, result.getId(), projectId));
        return result;
    }

    /**
     * Update a epic.
     *
     * @param epicDTO the entity to save.
     * @return the persisted entity.
     */
    public EpicDTO update(EpicDTO epicDTO) {
        LOG.debug("Request to update Epic : {}", epicDTO);
        if (epicDTO.getProject() != null && epicDTO.getProject().getId() != null) {
            projectPermissionService.requireProjectRole(epicDTO.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
        }
        validateEpicStatusTransition(epicDTO);
        Epic epic = epicMapper.toEntity(epicDTO);
        epic = epicRepository.save(epic);
        EpicDTO result = epicMapper.toDto(epic);
        Long projectId = result.getProject() != null ? result.getProject().getId() : null;
        entityEventSseService.sendEvent(
            new EntityChangeEvent(EntityEventType.ENTITY_EPIC, EntityEventType.UPDATED, result.getId(), projectId)
        );
        return result;
    }

    /**
     * Partially update a epic.
     *
     * @param epicDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<EpicDTO> partialUpdate(EpicDTO epicDTO) {
        LOG.debug("Request to partially update Epic : {}", epicDTO);

        return epicRepository
            .findById(epicDTO.getId())
            .map(existingEpic -> {
                projectPermissionService.requireProjectRole(existingEpic.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
                EpicStatus oldStatus = existingEpic.getStatus();
                epicMapper.partialUpdate(existingEpic, epicDTO);
                if (epicDTO.getStatus() != null && oldStatus != epicDTO.getStatus()) {
                    validateEpicStatusTransition(epicDTO);
                    existingEpic.setStatus(epicDTO.getStatus());
                } else {
                    existingEpic.setStatus(oldStatus);
                }
                return existingEpic;
            })
            .map(epicRepository::save)
            .map(epicMapper::toDto)
            .map(dto -> {
                Long pid = dto.getProject() != null ? dto.getProject().getId() : null;
                entityEventSseService.sendEvent(
                    new EntityChangeEvent(EntityEventType.ENTITY_EPIC, EntityEventType.UPDATED, dto.getId(), pid)
                );
                return dto;
            });
    }

    private void validateEpicStatusTransition(EpicDTO epicDTO) {
        if (epicDTO.getId() == null) {
            return;
        }
        Epic existing = epicRepository.findById(epicDTO.getId()).orElse(null);
        if (existing == null || existing.getStatus() == epicDTO.getStatus()) {
            return;
        }
        EpicStatus current = existing.getStatus();
        EpicStatus next = epicDTO.getStatus();

        if (current == EpicStatus.DONE || current == EpicStatus.CANCELLED) {
            throw new RuntimeException("Cannot change status of a " + current + " epic");
        }
        if (current == EpicStatus.TODO && next != EpicStatus.IN_PROGRESS) {
            throw new RuntimeException("A TODO epic can only transition to IN_PROGRESS");
        }
        if (current == EpicStatus.IN_PROGRESS && next != EpicStatus.DONE && next != EpicStatus.CANCELLED) {
            throw new RuntimeException("An IN_PROGRESS epic can only transition to DONE or CANCELLED");
        }
    }

    /**
     * Get all the epics with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<EpicDTO> findAllWithEagerRelationships(Pageable pageable) {
        return epicRepository.findAllWithEagerRelationships(pageable).map(epicMapper::toDto);
    }

    /**
     * Get one epic by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<EpicDTO> findOne(Long id) {
        LOG.debug("Request to get Epic : {}", id);
        return epicRepository.findOneWithEagerRelationships(id).map(epicMapper::toDto);
    }

    /**
     * Delete the epic by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Epic : {}", id);
        Epic epic = epicRepository.findById(id).orElseThrow(() -> new RuntimeException("Epic not found"));
        projectPermissionService.requireProjectRole(epic.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
        Long projectId = epic.getProject().getId();
        epicRepository.deleteById(id);
        entityEventSseService.sendEvent(new EntityChangeEvent(EntityEventType.ENTITY_EPIC, EntityEventType.DELETED, id, projectId));
    }
}
