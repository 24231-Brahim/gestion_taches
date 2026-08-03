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
import com.gestiontaches.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
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

    private final EpicService epicService;

    private final SprintService sprintService;

    public TaskService(
        TaskRepository taskRepository,
        TaskMapper taskMapper,
        UserRepository userRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectPermissionService projectPermissionService,
        NotificationService notificationService,
        TaskTransitionRepository taskTransitionRepository,
        EntityEventSseService entityEventSseService,
        EpicService epicService,
        SprintService sprintService
    ) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectPermissionService = projectPermissionService;
        this.notificationService = notificationService;
        this.taskTransitionRepository = taskTransitionRepository;
        this.entityEventSseService = entityEventSseService;
        this.epicService = epicService;
        this.sprintService = sprintService;
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
            String currentLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
                new BadRequestAlertException("Current user not found", "task", "usernotfound")
            );
            User currentUser = userRepository
                .findOneByLogin(currentLogin)
                .orElseThrow(() -> new BadRequestAlertException("User not found: " + currentLogin, "task", "usernotfound"));
            task.setCreatedBy(currentUser);
        }
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(java.time.Instant.now());
        }
        task = taskRepository.save(task);
        boolean created = taskDTO.getId() == null;
        if (created) {
            notifyTaskCreated(task);
        } else if (task.getStatus() != null && oldStatus != null && oldStatus != task.getStatus()) {
            recordTransition(task, oldStatus, task.getStatus());
            recomputeContainerStatus(task);
        }
        TaskDTO result = taskMapper.toDto(task);
        Long projectId = result.getProject() != null ? result.getProject().getId() : null;
        String eventType = created ? EntityEventType.CREATED : EntityEventType.UPDATED;
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
            recomputeContainerStatus(task);
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
                if (oldStatus != null && savedTask.getStatus() != null && oldStatus != savedTask.getStatus()) {
                    recomputeContainerStatus(savedTask);
                }
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
        Task existing = taskRepository
            .findById(taskId)
            .orElseThrow(() -> new BadRequestAlertException("Task not found", "task", "idnotfound"));
        Long projectId = existing.getProject().getId();
        Long currentUserId = SecurityUtils.getCurrentUserId().orElseGet(() -> {
            String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
                new BadRequestAlertException("Current user not found", "task", "usernotfound")
            );
            return userRepository
                .findOneByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new BadRequestAlertException("Current user not found", "task", "usernotfound"));
        });
        ProjectRole role = projectPermissionService.getCurrentUserRole(projectId);
        if (role == ProjectRole.OWNER || role == ProjectRole.MANAGER) {
            return;
        }
        if (role == ProjectRole.MEMBER && existing.getCreatedBy() != null && existing.getCreatedBy().getId().equals(currentUserId)) {
            return;
        }
        throw new AccessDeniedException("Access denied: you can only update your own tasks");
    }

    private void notifyStatusChangeIfNeeded(Task existingTask, Task savedTask, TaskStatus oldStatus) {
        if (existingTask == null || oldStatus == null || savedTask.getStatus() == null) {
            return;
        }
        if (oldStatus == savedTask.getStatus()) {
            return;
        }
        try {
            String statusLabel = statusLabel(savedTask.getStatus());
            User currentUser = resolveCurrentUser();
            User creator = existingTask.getCreatedBy();
            User assignee = existingTask.getAssignee();
            Instant now = Instant.now();

            if (creator != null && (currentUser == null || !currentUser.getId().equals(creator.getId()))) {
                NotificationDTO notification = new NotificationDTO();
                notification.setMessage("La tâche '" + savedTask.getTitle() + "' que vous avez créée est passée à " + statusLabel);
                notification.setTaskId(savedTask.getId());
                notification.setTaskTitle(savedTask.getTitle());
                notification.setUserId(creator.getId());
                notification.setIsRead(false);
                notification.setCreatedAt(now);
                notificationService.save(notification);
            }

            boolean sameUser = creator != null && assignee != null && creator.getId().equals(assignee.getId());
            if (assignee != null && !sameUser && (currentUser == null || !currentUser.getId().equals(assignee.getId()))) {
                NotificationDTO notification = new NotificationDTO();
                notification.setMessage("La tâche '" + savedTask.getTitle() + "' qui vous est assignée est passée à " + statusLabel);
                notification.setTaskId(savedTask.getId());
                notification.setTaskTitle(savedTask.getTitle());
                notification.setUserId(assignee.getId());
                notification.setIsRead(false);
                notification.setCreatedAt(now);
                notificationService.save(notification);
            }
        } catch (Exception e) {
            LOG.warn("Failed to notify status change for task {}: {}", savedTask.getId(), e.getMessage());
        }
    }

    private void notifyTaskCreated(Task task) {
        if (task.getAssignee() == null || task.getAssignee().getId() == null) {
            return;
        }
        try {
            User currentUser = resolveCurrentUser();
            User assignee = task.getAssignee();
            if (currentUser != null && currentUser.getId().equals(assignee.getId())) {
                return;
            }
            NotificationDTO notification = new NotificationDTO();
            notification.setMessage("Vous avez été assigné à la tâche #" + task.getId() + " : " + task.getTitle());
            notification.setTaskId(task.getId());
            notification.setTaskTitle(task.getTitle());
            notification.setUserId(assignee.getId());
            notification.setIsRead(false);
            notification.setCreatedAt(Instant.now());
            notificationService.save(notification);
        } catch (Exception e) {
            LOG.warn("Failed to notify task creation for task {}: {}", task.getId(), e.getMessage());
        }
    }

    private void recomputeContainerStatus(Task task) {
        try {
            if (task.getEpic() != null && task.getEpic().getId() != null) {
                epicService.recomputeStatus(task.getEpic().getId());
            }
            if (task.getSprint() != null && task.getSprint().getId() != null) {
                sprintService.recomputeStatus(task.getSprint().getId());
            }
        } catch (Exception e) {
            LOG.warn("Failed to recompute container status for task {}: {}", task.getId(), e.getMessage());
        }
    }

    private String statusLabel(TaskStatus status) {
        return switch (status) {
            case NEW -> "Nouvelle";
            case TODO -> "À faire";
            case IN_PROGRESS -> "En cours";
            case IN_REVIEW -> "En revue";
            case DONE -> "Terminée";
            case CANCELLED -> "Annulée";
        };
    }

    private User resolveCurrentUser() {
        try {
            String login = SecurityUtils.getCurrentUserLogin().orElse(null);
            if (login == null) {
                return null;
            }
            return userRepository.findOneByLogin(login).orElse(null);
        } catch (Exception e) {
            LOG.warn("Failed to resolve current user: {}", e.getMessage());
            return null;
        }
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
        if (savedTask.getAssignee() == null) {
            return;
        }
        try {
            Long newAssigneeId = savedTask.getAssignee().getId();
            Long oldAssigneeId = oldAssignee != null ? oldAssignee.getId() : null;
            if (Objects.equals(newAssigneeId, oldAssigneeId)) {
                return;
            }
            User currentUser = resolveCurrentUser();
            if (currentUser != null && currentUser.getId().equals(newAssigneeId)) {
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
        } catch (Exception e) {
            LOG.warn("Failed to notify assignee change for task {}: {}", savedTask.getId(), e.getMessage());
        }
    }

    private void validateSprintEpicBelongToProject(TaskDTO taskDTO) {
        Long projectId = taskDTO.getProject() != null ? taskDTO.getProject().getId() : null;
        if (projectId == null) {
            return;
        }
        if (taskDTO.getSprint() != null && taskDTO.getSprint().getId() != null) {
            boolean sprintMatches = taskDTO.getSprint().getProject() != null && projectId.equals(taskDTO.getSprint().getProject().getId());
            if (!sprintMatches) {
                throw new BadRequestAlertException("Sprint must belong to the same project as the task", "task", "sprintprojectmismatch");
            }
        }
        if (taskDTO.getEpic() != null && taskDTO.getEpic().getId() != null) {
            boolean epicMatches = taskDTO.getEpic().getProject() != null && projectId.equals(taskDTO.getEpic().getProject().getId());
            if (!epicMatches) {
                throw new BadRequestAlertException("Epic must belong to the same project as the task", "task", "epicprojectmismatch");
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
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("Current user not found", "task", "usernotfound")
        );
        User currentUser = userRepository
            .findOneByLogin(currentLogin)
            .orElseThrow(() -> new BadRequestAlertException("User not found: " + currentLogin, "task", "usernotfound"));
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
                    .orElseThrow(() ->
                        new BadRequestAlertException("Assignee must be a member of the project", "task", "assigneenotmember")
                    );
            }
        }
        task = taskRepository.save(task);
        notifyTaskCreated(task);
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
        Task task = taskRepository.findById(id).orElseThrow(() -> new BadRequestAlertException("Task not found", "task", "idnotfound"));
        Long projectId = task.getProject().getId();
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            taskRepository.deleteById(id);
            entityEventSseService.sendEvent(new EntityChangeEvent(EntityEventType.ENTITY_TASK, EntityEventType.DELETED, id, projectId));
            return;
        }
        Long currentUserId = SecurityUtils.getCurrentUserId().orElseGet(() -> {
            String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
                new BadRequestAlertException("Current user not found", "task", "usernotfound")
            );
            return userRepository
                .findOneByLogin(login)
                .map(User::getId)
                .orElseThrow(() -> new BadRequestAlertException("Current user not found", "task", "usernotfound"));
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
        throw new AccessDeniedException("Access denied: you can only delete your own tasks");
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
                    .orElseThrow(() ->
                        new BadRequestAlertException("User is not a member of the project for this task", "task", "assigneenotmember")
                    );
                User oldAssignee = task.getAssignee();
                task.setAssignee(user);
                Task savedTask = taskRepository.save(task);
                notifyAssigneeChange(task, savedTask, oldAssignee);
                return savedTask;
            })
            .map(taskMapper::toDto)
            .map(dto -> {
                Long pid = dto.getProject() != null ? dto.getProject().getId() : null;
                entityEventSseService.sendEvent(
                    new EntityChangeEvent(EntityEventType.ENTITY_TASK, EntityEventType.UPDATED, dto.getId(), pid)
                );
                return dto;
            })
            .orElseThrow(() -> new BadRequestAlertException("Task not found with id " + taskId, "task", "idnotfound"));
    }
}
