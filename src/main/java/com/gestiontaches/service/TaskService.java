package com.gestiontaches.service;

import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.mapper.TaskMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.gestiontaches.domain.Task}.
 */
@Service
@Transactional
public class TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    private final UserRepository userRepository;

    private final ProjectMemberRepository projectMemberRepository;

    private final ProjectPermissionService projectPermissionService;

    public TaskService(
        TaskRepository taskRepository,
        TaskMapper taskMapper,
        UserRepository userRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectPermissionService projectPermissionService
    ) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectPermissionService = projectPermissionService;
    }

    /**
     * Save a task.
     *
     * @param taskDTO the entity to save.
     * @return the persisted entity.
     */
    public TaskDTO save(TaskDTO taskDTO) {
        LOG.debug("Request to save Task : {}", taskDTO);
        if (taskDTO.getProject() != null && taskDTO.getProject().getId() != null) {
            projectPermissionService.requireProjectRole(
                taskDTO.getProject().getId(),
                ProjectRole.OWNER,
                ProjectRole.MANAGER,
                ProjectRole.MEMBER
            );
        }
        Task task = taskMapper.toEntity(taskDTO);
        if (task.getId() == null) {
            String currentLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
            User currentUser = userRepository
                .findOneByLogin(currentLogin)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentLogin));
            task.setCreatedBy(currentUser);
        }
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    /**
     * Update a task.
     *
     * @param taskDTO the entity to save.
     * @return the persisted entity.
     */
    public TaskDTO update(TaskDTO taskDTO) {
        LOG.debug("Request to update Task : {}", taskDTO);
        if (taskDTO.getId() != null) {
            checkTaskUpdatePermission(taskDTO.getId());
        }
        Task task = taskMapper.toEntity(taskDTO);
        task.setUpdatedAt(java.time.Instant.now());
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    /**
     * Partially update a task.
     *
     * @param taskDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<TaskDTO> partialUpdate(TaskDTO taskDTO) {
        LOG.debug("Request to partially update Task : {}", taskDTO);
        if (taskDTO.getId() != null) {
            checkTaskUpdatePermission(taskDTO.getId());
        }

        return taskRepository
            .findById(taskDTO.getId())
            .map(existingTask -> {
                taskMapper.partialUpdate(existingTask, taskDTO);
                existingTask.setUpdatedAt(java.time.Instant.now());
                return existingTask;
            })
            .map(taskRepository::save)
            .map(taskMapper::toDto);
    }

    private void checkTaskUpdatePermission(Long taskId) {
        Task existing = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        Long projectId = existing.getProject().getId();
        Long currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Current user not found"));
        ProjectRole role = projectPermissionService.getCurrentUserRole(projectId);
        if (role == ProjectRole.OWNER || role == ProjectRole.MANAGER) {
            return;
        }
        if (role == ProjectRole.MEMBER && existing.getCreatedBy() != null && existing.getCreatedBy().getId().equals(currentUserId)) {
            return;
        }
        throw new RuntimeException("Access denied: you can only update your own tasks");
    }

    /**
     * Get all the tasks with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<TaskDTO> findAllWithEagerRelationships(Pageable pageable) {
        return taskRepository.findAllWithEagerRelationships(pageable).map(taskMapper::toDto);
    }

    /**
     * Get one task by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TaskDTO> findOne(Long id) {
        LOG.debug("Request to get Task : {}", id);
        return taskRepository.findOneWithEagerRelationships(id).map(taskMapper::toDto);
    }

    /**
     * Create a task for a specific project with ownership validation.
     *
     * @param taskDTO the task to create.
     * @param projectId the project id.
     * @return the persisted task DTO.
     */
    public TaskDTO createForProject(TaskDTO taskDTO, Long projectId) {
        LOG.debug("Request to save Task for Project {} : {}", projectId, taskDTO);
        projectPermissionService.requireProjectRole(projectId, ProjectRole.OWNER, ProjectRole.MANAGER, ProjectRole.MEMBER);
        Task task = taskMapper.toEntity(taskDTO);
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        User currentUser = userRepository
            .findOneByLogin(currentLogin)
            .orElseThrow(() -> new RuntimeException("User not found: " + currentLogin));
        task.setCreatedBy(currentUser);
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.NEW);
        }
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(java.time.Instant.now());
        }
        if (task.getAssignee() != null) {
            Long assigneeId = task.getAssignee().getId();
            if (assigneeId != null) {
                projectMemberRepository
                    .findByProjectIdAndUserId(projectId, assigneeId)
                    .orElseThrow(() -> new RuntimeException("Assignee must be a member of the project"));
            }
        }
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    /**
     * Delete the task by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Task : {}", id);
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        Long projectId = task.getProject().getId();
        Long currentUserId = SecurityUtils.getCurrentUserId().orElseThrow(() -> new RuntimeException("Current user not found"));
        ProjectRole role = projectPermissionService.getCurrentUserRole(projectId);
        if (role == ProjectRole.OWNER || role == ProjectRole.MANAGER) {
            taskRepository.deleteById(id);
            return;
        }
        if (role == ProjectRole.MEMBER && task.getCreatedBy() != null && task.getCreatedBy().getId().equals(currentUserId)) {
            taskRepository.deleteById(id);
            return;
        }
        throw new RuntimeException("Access denied: you can only delete your own tasks");
    }

    /**
     * Assign a user to a task.
     *
     * @param taskId the id of the task.
     * @param user the user to assign.
     * @return the updated task DTO.
     */
    public TaskDTO assign(Long taskId, User user) {
        LOG.debug("Request to assign user {} to Task : {}", user.getLogin(), taskId);
        return taskRepository
            .findById(taskId)
            .map(task -> {
                projectPermissionService.requireProjectRole(task.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
                // Verify user is a member of the project
                ProjectMember member = projectMemberRepository
                    .findByProjectIdAndUserId(task.getProject().getId(), user.getId())
                    .orElseThrow(() -> new RuntimeException("User is not a member of the project for this task"));
                task.setAssignee(user);
                return taskRepository.save(task);
            })
            .map(taskMapper::toDto)
            .orElseThrow(() -> new RuntimeException("Task not found with id " + taskId));
    }
}
