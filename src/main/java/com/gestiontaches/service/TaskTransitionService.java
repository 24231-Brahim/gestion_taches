package com.gestiontaches.service;

import com.gestiontaches.domain.TaskTransition;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.TaskTransitionRepository;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.TaskTransitionDTO;
import com.gestiontaches.service.dto.UserDTO;
import com.gestiontaches.service.mapper.TaskTransitionMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.gestiontaches.domain.TaskTransition}.
 */
@Service
@Transactional
public class TaskTransitionService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskTransitionService.class);

    private final TaskTransitionRepository taskTransitionRepository;

    private final TaskTransitionMapper taskTransitionMapper;

    private final UserService userService;

    public TaskTransitionService(
        TaskTransitionRepository taskTransitionRepository,
        TaskTransitionMapper taskTransitionMapper,
        UserService userService
    ) {
        this.taskTransitionRepository = taskTransitionRepository;
        this.taskTransitionMapper = taskTransitionMapper;
        this.userService = userService;
    }

    /**
     * Save a taskTransition.
     *
     * @param taskTransitionDTO the entity to save.
     * @return the persisted entity.
     */
    public TaskTransitionDTO save(TaskTransitionDTO taskTransitionDTO) {
        LOG.debug("Request to save TaskTransition : {}", taskTransitionDTO);
        User currentUser = userService
            .getUserWithAuthoritiesByLogin(SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("No user logged in")))
            .orElseThrow(() -> new RuntimeException("User not found"));
        taskTransitionDTO.setUser(new UserDTO(currentUser));
        TaskTransition taskTransition = taskTransitionMapper.toEntity(taskTransitionDTO);
        taskTransition = taskTransitionRepository.save(taskTransition);
        return taskTransitionMapper.toDto(taskTransition);
    }

    /**
     * Update a taskTransition.
     *
     * @param taskTransitionDTO the entity to save.
     * @return the persisted entity.
     */
    public TaskTransitionDTO update(TaskTransitionDTO taskTransitionDTO) {
        LOG.debug("Request to update TaskTransition : {}", taskTransitionDTO);
        TaskTransition taskTransition = taskTransitionMapper.toEntity(taskTransitionDTO);
        taskTransition = taskTransitionRepository.save(taskTransition);
        return taskTransitionMapper.toDto(taskTransition);
    }

    /**
     * Partially update a taskTransition.
     *
     * @param taskTransitionDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<TaskTransitionDTO> partialUpdate(TaskTransitionDTO taskTransitionDTO) {
        LOG.debug("Request to partially update TaskTransition : {}", taskTransitionDTO);

        return taskTransitionRepository
            .findById(taskTransitionDTO.getId())
            .map(existingTaskTransition -> {
                taskTransitionMapper.partialUpdate(existingTaskTransition, taskTransitionDTO);

                return existingTaskTransition;
            })
            .map(taskTransitionRepository::save)
            .map(taskTransitionMapper::toDto);
    }

    /**
     * Get all the taskTransitions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<TaskTransitionDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all TaskTransitions");
        return taskTransitionRepository.findAll(pageable).map(taskTransitionMapper::toDto);
    }

    /**
     * Get one taskTransition by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TaskTransitionDTO> findOne(Long id) {
        LOG.debug("Request to get TaskTransition : {}", id);
        return taskTransitionRepository.findById(id).map(taskTransitionMapper::toDto);
    }

    /**
     * Delete the taskTransition by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete TaskTransition : {}", id);
        taskTransitionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TaskTransitionDTO> findByTaskId(Long taskId) {
        LOG.debug("Request to get TaskTransitions for Task : {}", taskId);
        return taskTransitionRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream().map(taskTransitionMapper::toDto).toList();
    }
}
