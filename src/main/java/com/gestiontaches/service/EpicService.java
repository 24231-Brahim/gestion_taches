package com.gestiontaches.service;

import com.gestiontaches.domain.Epic;
import com.gestiontaches.repository.EpicRepository;
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

    public EpicService(EpicRepository epicRepository, EpicMapper epicMapper) {
        this.epicRepository = epicRepository;
        this.epicMapper = epicMapper;
    }

    /**
     * Save a epic.
     *
     * @param epicDTO the entity to save.
     * @return the persisted entity.
     */
    public EpicDTO save(EpicDTO epicDTO) {
        LOG.debug("Request to save Epic : {}", epicDTO);
        Epic epic = epicMapper.toEntity(epicDTO);
        epic = epicRepository.save(epic);
        return epicMapper.toDto(epic);
    }

    /**
     * Update a epic.
     *
     * @param epicDTO the entity to save.
     * @return the persisted entity.
     */
    public EpicDTO update(EpicDTO epicDTO) {
        LOG.debug("Request to update Epic : {}", epicDTO);
        Epic epic = epicMapper.toEntity(epicDTO);
        epic = epicRepository.save(epic);
        return epicMapper.toDto(epic);
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
                epicMapper.partialUpdate(existingEpic, epicDTO);

                return existingEpic;
            })
            .map(epicRepository::save)
            .map(epicMapper::toDto);
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
        epicRepository.deleteById(id);
    }
}
