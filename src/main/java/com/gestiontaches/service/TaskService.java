package com.gestiontaches.service;

import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.TaskTransition;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.TaskTransitionRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.EntityChangeEvent;
import com.gestiontaches.service.dto.EntityEventType;
import com.gestiontaches.service.dto.NotificationDTO;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.mapper.TaskMapper;
import java.time.Instant;
import java.util.Objects;
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

    private final NotificationService notificationService;

    private final TaskTransitionRepository taskTransitionRepository;

    private final EntityEventSseService entityEventSseService;

    public TaskService(
        TaskRepository taskRepository,
        TaskMapper taskMapper,
        UserRepository userRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectPermissionService projectPermissionService,
        NotificationService notificationService,
        TaskTransitionRepository taskTransitionRepository,
        EntityEventSseService entityEventSseService
    ) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectPermissionService = projectPermissionService;
        this.notificationService = notificationService;
        this.taskTransitionRepository = taskTransitionRepository;
        this.entityEventSseService = entityEventSseService;
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
        validateSprintEpicBelongToProject(taskDTO);
        TaskStatus oldStatus = null;
        if (taskDTO.getId() != null) {
            oldStatus = taskRepository.findById(taskDTO.getId()).map(Task::getStatus).orElse(null);
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
        if (taskDTO.getId() != null && task.getStatus() != null && oldStatus != null && oldStatus != task.getStatus()) {
            recordTransition(task, oldStatus, task.getStatus());
        }
        TaskDTO result = taskMapper.toDto(task);
        Long projectId = result.getProject() != null ? result.getProject().getId() : null;
        String eventType = taskDTO.getId() != null ? EntityEventType.UPDATED : EntityEventType.CREATED;
        entityEventSseService.sendEvent(new EntityChangeEvent(EntityEventType.ENTITY_TASK, eventType, result.getId(), projectId));
        return result;
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
        validateSprintEpicBelongToProject(taskDTO);
        Task existingTask = taskRepository.findById(taskDTO.getId()).orElse(null);
        TaskStatus oldStatus = existingTask != null ? existingTask.getStatus() : null;
        User oldAssignee = existingTask != null ? existingTask.getAssignee() : null;
        Task task = taskMapper.toEntity(taskDTO);
        if (task.getUpdatedAt() == null) {
            task.setUpdatedAt(java.time.Instant.now());
        }
        task = taskRepository.save(task);
        notifyStatusChangeIfNeeded(existingTask, task, oldStatus);
        notifyAssigneeChange(existingTask, task, oldAssignee);
        if (existingTask != null && task.getStatus() != null && oldStatus != null && oldStatus != task.getStatus()) {
            recordTransition(task, oldStatus, task.getStatus());
        }
        TaskDTO result = taskMapper.toDto(task);
        Long projectId = result.getProject() != null ? result.getProject().getId() : null;
        entityEventSseService.sendEvent(
            new EntityChangeEvent(EntityEventType.ENTITY_TASK, EntityEventType.UPDATED, result.getId(), projectId)
        );
        return result;
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
                TaskStatus oldStatus = existingTask.getStatus();
                User oldAssignee = existingTask.getAssignee();
                taskMapper.partialUpdate(existingTask, taskDTO);
                if (taskDTO.getUpdatedAt() == null) {
                    existingTask.setUpdatedAt(java.time.Instant.now());
                }
                Task savedTask = taskRepository.save(existingTask);
                notifyStatusChangeIfNeeded(existingTask, savedTask, oldStatus);
                notifyAssigneeChange(existingTask, savedTask, oldAssignee);
                return savedTask;
            })
            .map(taskMapper::toDto)
            .map(dto -> {
                Long projectId = dto.getProject() != null ? dto.getProject().getId() : null;
                entityEventSseService.sendEvent(
                    new EntityChangeEvent(EntityEventType.ENTITY_TASK, EntityEventType.UPDATED, dto.getId(), projectId)
                );
                return dto;
            });
    }

    private void checkTaskUpdatePermission(Long taskId) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return;
        }
        Task existing = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        Long projectId = existing.getProject().getId();
        Long currentUserId = SecurityUtils.getCurrentUserId().orElseGet(() -> {
            String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
            return userRepository
                .findOneByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        });
        ProjectRole role = projectPermissionService.getCurrentUserRole(projectId);
        if (role == ProjectRole.OWNER || role == ProjectRole.MANAGER) {
            return;
        }
        if (role == ProjectRole.MEMBER && existing.getCreatedBy() != null && existing.getCreatedBy().getId().equals(currentUserId)) {
            return;
        }
        throw new RuntimeException("Access denied: you can only update your own tasks");
    }

    private void notifyStatusChangeIfNeeded(Task existingTask, Task savedTask, TaskStatus oldStatus) {
        if (existingTask == null || oldStatus == null || savedTask.getStatus() == null) {
            return;
        }
        if (oldStatus == savedTask.getStatus()) {
            return;
        }
        if (savedTask.getStatus() != TaskStatus.DONE && savedTask.getStatus() != TaskStatus.CANCELLED) {
            return;
        }

        String statusLabel = savedTask.getStatus() == TaskStatus.DONE ? "DONE" : "CANCELLED";
        User creator = existingTask.getCreatedBy();
        User assignee = existingTask.getAssignee();
        boolean creatorIsAdmin = creator != null && hasRole(creator, AuthoritiesConstants.ADMIN);
        boolean assigneeIsAdmin = assignee != null && hasRole(assignee, AuthoritiesConstants.ADMIN);

        if (!creatorIsAdmin && !assigneeIsAdmin) {
            return;
        }

        boolean sameUser = creator != null && assignee != null && creator.getId().equals(assignee.getId());
        Instant now = Instant.now();

        if (creatorIsAdmin) {
            NotificationDTO notification = new NotificationDTO();
            notification.setMessage("La tâche '" + savedTask.getTitle() + "' que vous avez créée est passée à " + statusLabel);
            notification.setTaskId(savedTask.getId());
            notification.setTaskTitle(savedTask.getTitle());
            notification.setUserId(creator.getId());
            notification.setIsRead(false);
            notification.setCreatedAt(now);
            notificationService.save(notification);
        }

        if (assigneeIsAdmin && !sameUser) {
            NotificationDTO notification = new NotificationDTO();
            notification.setMessage("La tâche '" + savedTask.getTitle() + "' qui vous est assignée est passée à " + statusLabel);
            notification.setTaskId(savedTask.getId());
            notification.setTaskTitle(savedTask.getTitle());
            notification.setUserId(assignee.getId());
            notification.setIsRead(false);
            notification.setCreatedAt(now);
            notificationService.save(notification);
        }
    }

    private boolean hasRole(User user, String role) {
        return (
            user.getAuthorities() != null &&
            user
                .getAuthorities()
                .stream()
                .anyMatch(a -> role.equals(a.getName()))
        );
    }

    private void recordTransition(Task task, TaskStatus fromStatus, TaskStatus toStatus) {
        try {
            String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
            User currentUser = currentLogin != null ? userRepository.findOneByLogin(currentLogin).orElse(null) : null;
            TaskTransition transition = new TaskTransition();
            transition.setTask(task);
            transition.setFromStatus(fromStatus.name());
            transition.setToStatus(toStatus.name());
            transition.setUser(currentUser);
            transition.setCreatedAt(Instant.now());
            taskTransitionRepository.save(transition);
        } catch (Exception e) {
            LOG.warn("Failed to record task transition for task {}: {}", task.getId(), e.getMessage());
        }
    }

    private void notifyAssigneeChange(Task existingTask, Task savedTask, User oldAssignee) {
        if (savedTask.getAssignee() == null || savedTask.getCreatedBy() == null) {
            return;
        }
        Long newAssigneeId = savedTask.getAssignee().getId();
        Long oldAssigneeId = oldAssignee != null ? oldAssignee.getId() : null;
        if (Objects.equals(newAssigneeId, oldAssigneeId)) {
            return;
        }
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        NotificationDTO notification = new NotificationDTO();
        notification.setMessage(
            currentLogin + " vous a assign\u00e9 \u00e0 la t\u00e2che #" + savedTask.getId() + " : " + savedTask.getTitle()
        );
        notification.setTaskId(savedTask.getId());
        notification.setTaskTitle(savedTask.getTitle());
        notification.setUserId(newAssigneeId);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notificationService.save(notification);
    }

    private void validateSprintEpicBelongToProject(TaskDTO taskDTO) {
        Long projectId = taskDTO.getProject() != null ? taskDTO.getProject().getId() : null;
        if (projectId == null) {
            return;
        }
        if (taskDTO.getSprint() != null && taskDTO.getSprint().getId() != null) {
            boolean sprintMatches = taskDTO.getSprint().getProject() != null && projectId.equals(taskDTO.getSprint().getProject().getId());
            if (!sprintMatches) {
                throw new RuntimeException("Sprint must belong to the same project as the task");
            }
        }
        if (taskDTO.getEpic() != null && taskDTO.getEpic().getId() != null) {
            boolean epicMatches = taskDTO.getEpic().getProject() != null && projectId.equals(taskDTO.getEpic().getProject().getId());
            if (!epicMatches) {
                throw new RuntimeException("Epic must belong to the same project as the task");
            }
        }
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
        TaskDTO result = taskMapper.toDto(task);
        entityEventSseService.sendEvent(
            new EntityChangeEvent(EntityEventType.ENTITY_TASK, EntityEventType.CREATED, result.getId(), projectId)
        );
        return result;
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
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            taskRepository.deleteById(id);
            entityEventSseService.sendEvent(new EntityChangeEvent(EntityEventType.ENTITY_TASK, EntityEventType.DELETED, id, projectId));
            return;
        }
        Long currentUserId = SecurityUtils.getCurrentUserId().orElseGet(() -> {
            String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
            return userRepository
                .findOneByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        });
        ProjectRole role = projectPermissionService.getCurrentUserRole(projectId);
        if (role == ProjectRole.OWNER || role == ProjectRole.MANAGER) {
            taskRepository.deleteById(id);
            entityEventSseService.sendEvent(new EntityChangeEvent(EntityEventType.ENTITY_TASK, EntityEventType.DELETED, id, projectId));
            return;
        }
        if (role == ProjectRole.MEMBER && task.getCreatedBy() != null && task.getCreatedBy().getId().equals(currentUserId)) {
            taskRepository.deleteById(id);
            entityEventSseService.sendEvent(new EntityChangeEvent(EntityEventType.ENTITY_TASK, EntityEventType.DELETED, id, projectId));
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
            .map(dto -> {
                Long pid = dto.getProject() != null ? dto.getProject().getId() : null;
                entityEventSseService.sendEvent(
                    new EntityChangeEvent(EntityEventType.ENTITY_TASK, EntityEventType.UPDATED, dto.getId(), pid)
                );
                return dto;
            })
            .orElseThrow(() -> new RuntimeException("Task not found with id " + taskId));
    }
}
