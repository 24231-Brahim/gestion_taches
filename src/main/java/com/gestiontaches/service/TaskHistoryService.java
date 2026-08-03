package com.gestiontaches.service;

import com.gestiontaches.domain.TaskHistory;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.TaskHistoryRepository;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.TaskHistoryDTO;
import com.gestiontaches.service.dto.UserDTO;
import com.gestiontaches.service.mapper.TaskHistoryMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.gestiontaches.domain.TaskHistory}.
 */
@Service
@Transactional
public class TaskHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskHistoryService.class);

    private final TaskHistoryRepository taskHistoryRepository;

    private final TaskHistoryMapper taskHistoryMapper;

    private final UserService userService;

    public TaskHistoryService(TaskHistoryRepository taskHistoryRepository, TaskHistoryMapper taskHistoryMapper, UserService userService) {
        this.taskHistoryRepository = taskHistoryRepository;
        this.taskHistoryMapper = taskHistoryMapper;
        this.userService = userService;
    }

    /**
     * Save a taskHistory.
     *
     * @param taskHistoryDTO the entity to save.
     * @return the persisted entity.
     */
    public TaskHistoryDTO save(TaskHistoryDTO taskHistoryDTO) {
        LOG.debug("Request to save TaskHistory : {}", taskHistoryDTO);
        User currentUser = userService
            .getUserWithAuthoritiesByLogin(SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("No user logged in")))
            .orElseThrow(() -> new RuntimeException("User not found"));
        taskHistoryDTO.setUser(new UserDTO(currentUser));
        TaskHistory taskHistory = taskHistoryMapper.toEntity(taskHistoryDTO);
        taskHistory = taskHistoryRepository.save(taskHistory);
        return taskHistoryMapper.toDto(taskHistory);
    }

    /**
     * Update a taskHistory.
     *
     * @param taskHistoryDTO the entity to save.
     * @return the persisted entity.
     */
    public TaskHistoryDTO update(TaskHistoryDTO taskHistoryDTO) {
        LOG.debug("Request to update TaskHistory : {}", taskHistoryDTO);
        TaskHistory taskHistory = taskHistoryMapper.toEntity(taskHistoryDTO);
        taskHistory = taskHistoryRepository.save(taskHistory);
        return taskHistoryMapper.toDto(taskHistory);
    }

    /**
     * Partially update a taskHistory.
     *
     * @param taskHistoryDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<TaskHistoryDTO> partialUpdate(TaskHistoryDTO taskHistoryDTO) {
        LOG.debug("Request to partially update TaskHistory : {}", taskHistoryDTO);

        return taskHistoryRepository
            .findById(taskHistoryDTO.getId())
            .map(existingTaskHistory -> {
                taskHistoryMapper.partialUpdate(existingTaskHistory, taskHistoryDTO);

                return existingTaskHistory;
            })
            .map(taskHistoryRepository::save)
            .map(taskHistoryMapper::toDto);
    }

    /**
     * Get all the taskHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<TaskHistoryDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all TaskHistories");
        return taskHistoryRepository.findAll(pageable).map(taskHistoryMapper::toDto);
    }

    /**
     * Get one taskHistory by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TaskHistoryDTO> findOne(Long id) {
        LOG.debug("Request to get TaskHistory : {}", id);
        return taskHistoryRepository.findById(id).map(taskHistoryMapper::toDto);
    }

    /**
     * Delete the taskHistory by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete TaskHistory : {}", id);
        taskHistoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TaskHistoryDTO> findByTaskId(Long taskId) {
        LOG.debug("Request to get TaskHistories for Task : {}", taskId);
        return taskHistoryRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream().map(taskHistoryMapper::toDto).toList();
    }

    /**
     * Get the 10 most recent taskHistories authored by the current user, for the developer dashboard's
     * "recent activity" feed.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<TaskHistoryDTO> findRecentForCurrentUser() {
        LOG.debug("Request to get recent TaskHistories for current user");
        User currentUser = userService
            .getUserWithAuthoritiesByLogin(SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("No user logged in")))
            .orElseThrow(() -> new RuntimeException("User not found"));
        return taskHistoryRepository
            .findTop10ByUserIdOrderByCreatedAtDesc(currentUser.getId())
            .stream()
            .map(taskHistoryMapper::toDto)
            .toList();
    }
}
