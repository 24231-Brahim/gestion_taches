package com.gestiontaches.service;

import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.Sprint;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.TaskHistory;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.domain.enumeration.SprintStatus;
import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.SprintRepository;
import com.gestiontaches.repository.TaskHistoryRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.service.dto.EntityChangeEvent;
import com.gestiontaches.service.dto.EntityEventType;
import com.gestiontaches.service.dto.SprintDTO;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.dto.VelocityReportDTO;
import com.gestiontaches.service.mapper.SprintMapper;
import com.gestiontaches.service.mapper.TaskMapper;
import com.gestiontaches.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SprintService {

    private static final Logger LOG = LoggerFactory.getLogger(SprintService.class);

    private final SprintRepository sprintRepository;
    private final SprintMapper sprintMapper;
    private final ProjectPermissionService projectPermissionService;
    private final TaskRepository taskRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final NotificationService notificationService;
    private final ProjectMemberService projectMemberService;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final EntityEventSseService entityEventSseService;

    public SprintService(
        SprintRepository sprintRepository,
        SprintMapper sprintMapper,
        ProjectPermissionService projectPermissionService,
        TaskRepository taskRepository,
        TaskHistoryRepository taskHistoryRepository,
        NotificationService notificationService,
        ProjectMemberService projectMemberService,
        UserRepository userRepository,
        TaskMapper taskMapper,
        EntityEventSseService entityEventSseService
    ) {
        this.sprintRepository = sprintRepository;
        this.sprintMapper = sprintMapper;
        this.projectPermissionService = projectPermissionService;
        this.taskRepository = taskRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.notificationService = notificationService;
        this.projectMemberService = projectMemberService;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
        this.entityEventSseService = entityEventSseService;
    }

    public SprintDTO save(SprintDTO sprintDTO) {
        LOG.debug("Request to save Sprint : {}", sprintDTO);
        if (sprintDTO.getProject() != null && sprintDTO.getProject().getId() != null) {
            projectPermissionService.requireProjectRole(sprintDTO.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
        }
        if (sprintDTO.getStatus() == null) {
            sprintDTO.setStatus(SprintStatus.PLANNED);
        }
        validateSingleActiveSprint(sprintDTO);
        Sprint sprint = sprintMapper.toEntity(sprintDTO);
        sprint = sprintRepository.save(sprint);
        SprintDTO result = sprintMapper.toDto(sprint);
        Long projectId = result.getProject() != null ? result.getProject().getId() : null;
        entityEventSseService.sendEvent(
            new EntityChangeEvent(EntityEventType.ENTITY_SPRINT, EntityEventType.CREATED, result.getId(), projectId)
        );
        return result;
    }

    public SprintDTO update(SprintDTO sprintDTO) {
        LOG.debug("Request to update Sprint : {}", sprintDTO);
        if (sprintDTO.getProject() != null && sprintDTO.getProject().getId() != null) {
            projectPermissionService.requireProjectRole(sprintDTO.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
        }
        validateSprintStatusTransition(sprintDTO);
        validateSingleActiveSprint(sprintDTO);
        Sprint sprint = sprintMapper.toEntity(sprintDTO);
        sprint = sprintRepository.save(sprint);
        SprintDTO result = sprintMapper.toDto(sprint);
        Long projectId = result.getProject() != null ? result.getProject().getId() : null;
        entityEventSseService.sendEvent(
            new EntityChangeEvent(EntityEventType.ENTITY_SPRINT, EntityEventType.UPDATED, result.getId(), projectId)
        );
        return result;
    }

    public Optional<SprintDTO> partialUpdate(SprintDTO sprintDTO) {
        LOG.debug("Request to partially update Sprint : {}", sprintDTO);
        return sprintRepository
            .findById(sprintDTO.getId())
            .map(existingSprint -> {
                projectPermissionService.requireProjectRole(existingSprint.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
                SprintStatus oldStatus = existingSprint.getStatus();
                sprintMapper.partialUpdate(existingSprint, sprintDTO);
                SprintDTO updatedDto = sprintMapper.toDto(existingSprint);
                updatedDto.setStatus(oldStatus);
                if (sprintDTO.getStatus() != null) {
                    updatedDto.setStatus(sprintDTO.getStatus());
                }
                validateSprintStatusTransition(updatedDto);
                validateSingleActiveSprint(updatedDto);
                existingSprint.setStatus(updatedDto.getStatus());
                return existingSprint;
            })
            .map(sprintRepository::save)
            .map(sprintMapper::toDto)
            .map(dto -> {
                Long pid = dto.getProject() != null ? dto.getProject().getId() : null;
                entityEventSseService.sendEvent(
                    new EntityChangeEvent(EntityEventType.ENTITY_SPRINT, EntityEventType.UPDATED, dto.getId(), pid)
                );
                return dto;
            });
    }

    private void validateSprintStatusTransition(SprintDTO sprintDTO) {
        if (sprintDTO.getId() == null) {
            return;
        }
        Sprint existing = sprintRepository.findById(sprintDTO.getId()).orElse(null);
        if (existing == null || existing.getStatus() == sprintDTO.getStatus()) {
            return;
        }
        SprintStatus current = existing.getStatus();
        SprintStatus next = sprintDTO.getStatus();

        if (current == SprintStatus.COMPLETED || current == SprintStatus.CANCELLED) {
            throw new BadRequestAlertException("Cannot change status of a " + current + " sprint", "sprint", "invalidstatus");
        }
        if (current == SprintStatus.PLANNED && next != SprintStatus.ACTIVE) {
            throw new BadRequestAlertException("A PLANNED sprint can only transition to ACTIVE", "sprint", "invalidstatus");
        }
        if (current == SprintStatus.ACTIVE && next != SprintStatus.COMPLETED && next != SprintStatus.CANCELLED) {
            throw new BadRequestAlertException("An ACTIVE sprint can only transition to COMPLETED or CANCELLED", "sprint", "invalidstatus");
        }
    }

    public SprintDTO startSprint(Long sprintId) {
        LOG.debug("Request to start Sprint : {}", sprintId);
        Sprint sprint = sprintRepository
            .findById(sprintId)
            .orElseThrow(() -> new BadRequestAlertException("Sprint not found", "sprint", "idnotfound"));
        projectPermissionService.requireProjectRole(sprint.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new BadRequestAlertException("Only a PLANNED sprint can be started", "sprint", "invalidstatus");
        }

        Optional<Sprint> existingActive = sprintRepository.findFirstByProjectIdAndStatusOrderByIdDesc(
            sprint.getProject().getId(),
            SprintStatus.ACTIVE
        );
        if (existingActive.isPresent() && !existingActive.get().getId().equals(sprintId)) {
            throw new BadRequestAlertException("A project can only have one active sprint at a time", "sprint", "activeexists");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        sprint = sprintRepository.save(sprint);

        User currentUser = resolveCurrentUser();
        notifyProjectMembers(sprint, currentUser, "Le sprint \"" + sprint.getName() + "\" a démarré");

        SprintDTO result = sprintMapper.toDto(sprint);
        entityEventSseService.sendEvent(
            new EntityChangeEvent(EntityEventType.ENTITY_SPRINT, EntityEventType.UPDATED, result.getId(), result.getProject().getId())
        );
        return result;
    }

    public VelocityReportDTO closeSprint(Long sprintId) {
        LOG.debug("Request to close Sprint : {}", sprintId);
        Sprint sprint = sprintRepository
            .findById(sprintId)
            .orElseThrow(() -> new BadRequestAlertException("Sprint not found", "sprint", "idnotfound"));
        projectPermissionService.requireProjectRole(sprint.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BadRequestAlertException("Only an ACTIVE sprint can be closed", "sprint", "invalidstatus");
        }

        List<Task> sprintTasks = taskRepository.findBySprintId(sprintId);
        int totalTasks = sprintTasks.size();
        int doneTasks = 0;
        int movedToBacklog = 0;
        Instant now = Instant.now();
        User currentUser = resolveCurrentUser();

        for (Task task : sprintTasks) {
            if (task.getStatus() == TaskStatus.DONE) {
                doneTasks++;
            } else {
                task.setSprint(null);
                task.setUpdatedAt(now);
                taskRepository.save(task);
                movedToBacklog++;

                TaskHistory history = new TaskHistory();
                history.setTask(task);
                history.setUser(currentUser);
                history.setAction("TASK_MOVED_TO_BACKLOG");
                history.setOldValue("Sprint: " + sprint.getName());
                history.setNewValue("Backlog");
                history.setCreatedAt(now);
                taskHistoryRepository.save(history);
                notificationService.notifyAdminsOfTaskHistory(history);
            }
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        sprint = sprintRepository.save(sprint);
        entityEventSseService.sendEvent(
            new EntityChangeEvent(EntityEventType.ENTITY_SPRINT, EntityEventType.UPDATED, sprint.getId(), sprint.getProject().getId())
        );

        int percentage = totalTasks > 0 ? ((doneTasks * 100) / totalTasks) : 0;

        VelocityReportDTO report = new VelocityReportDTO();
        report.setTachesPrevues(totalTasks);
        report.setTachesTerminees(doneTasks);
        report.setPourcentage(percentage);
        report.setTachesReportees(movedToBacklog);

        return report;
    }

    public List<TaskDTO> getBacklogTasks(Long projectId) {
        return taskRepository.findByProjectIdAndSprintIsNullWithToOneRelationships(projectId).stream().map(taskMapper::toDto).toList();
    }

    /**
     * Recalculate and update the status of a sprint based on its tasks.
     * Rule: any task IN_PROGRESS or READY_FOR_TEST → ACTIVE ; all tasks DONE → COMPLETED ;
     * otherwise (no in-progress task, mix of NEW/TODO, or no task at all) → PLANNED as default.
     * A manually CANCELLED sprint is never overwritten, and a COMPLETED sprint is never downgraded
     * back to PLANNED (it may however return to ACTIVE if a task is put back in progress).
     *
     * @param sprintId the id of the sprint.
     */
    @Transactional
    public void recalculateStatus(Long sprintId) {
        LOG.debug("Request to recalculate status of Sprint : {}", sprintId);
        Sprint sprint = sprintRepository.findById(sprintId).orElseThrow(() -> new RuntimeException("Sprint not found"));
        if (sprint.getStatus() == SprintStatus.CANCELLED) {
            return;
        }
        List<Task> tasks = taskRepository.findBySprintId(sprintId);
        boolean hasInProgress = tasks.stream().anyMatch(t -> isInProgress(t.getStatus()));
        boolean allDone = !tasks.isEmpty() && tasks.stream().allMatch(t -> t.getStatus() == TaskStatus.DONE);

        SprintStatus newStatus;
        if (hasInProgress) {
            newStatus = SprintStatus.ACTIVE;
        } else if (allDone) {
            newStatus = SprintStatus.COMPLETED;
        } else if (sprint.getStatus() == SprintStatus.COMPLETED) {
            newStatus = SprintStatus.COMPLETED;
        } else {
            newStatus = SprintStatus.PLANNED;
        }

        if (newStatus != sprint.getStatus()) {
            sprint.setStatus(newStatus);
            sprint = sprintRepository.save(sprint);
            Long projectId = sprint.getProject() != null ? sprint.getProject().getId() : null;
            entityEventSseService.sendEvent(
                new EntityChangeEvent(EntityEventType.ENTITY_SPRINT, EntityEventType.UPDATED, sprint.getId(), projectId)
            );
        }
    }

    private static boolean isInProgress(TaskStatus status) {
        return status == TaskStatus.IN_PROGRESS || status == TaskStatus.READY_FOR_TEST;
    }

    private void validateSingleActiveSprint(SprintDTO sprintDTO) {
        if (sprintDTO.getStatus() == SprintStatus.ACTIVE && sprintDTO.getProject() != null && sprintDTO.getProject().getId() != null) {
            Optional<Sprint> existingActive = sprintRepository.findFirstByProjectIdAndStatusOrderByIdDesc(
                sprintDTO.getProject().getId(),
                SprintStatus.ACTIVE
            );
            existingActive.ifPresent(s -> {
                if (!s.getId().equals(sprintDTO.getId())) {
                    throw new BadRequestAlertException("A project can only have one active sprint at a time", "sprint", "activeexists");
                }
            });
        }
    }

    private void notifyProjectMembers(Sprint sprint, User currentUser, String message) {
        try {
            List<ProjectMember> members = projectMemberService.getMembersByProjectId(sprint.getProject().getId());
            for (ProjectMember member : members) {
                if (member.getUser() != null && !member.getUser().getId().equals(resolveCurrentUser().getId())) {
                    com.gestiontaches.service.dto.NotificationDTO notification = new com.gestiontaches.service.dto.NotificationDTO();
                    notification.setMessage(message);
                    notification.setUserId(member.getUser().getId());
                    notification.setIsRead(false);
                    notification.setCreatedAt(Instant.now());
                    notificationService.save(notification);
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to send sprint notification: {}", e.getMessage());
        }
    }

    private User resolveCurrentUser() {
        Long userId = projectPermissionService.resolveCurrentUserId();
        return userRepository
            .findById(userId)
            .orElseThrow(() -> new BadRequestAlertException("Current user not found", "sprint", "usernotfound"));
    }

    public Page<SprintDTO> findAllWithEagerRelationships(Pageable pageable) {
        return sprintRepository.findAllWithEagerRelationships(pageable).map(sprintMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<SprintDTO> findOne(Long id) {
        LOG.debug("Request to get Sprint : {}", id);
        return sprintRepository.findOneWithEagerRelationships(id).map(sprintMapper::toDto);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete Sprint : {}", id);
        Sprint sprint = sprintRepository
            .findById(id)
            .orElseThrow(() -> new BadRequestAlertException("Sprint not found", "sprint", "idnotfound"));
        projectPermissionService.requireProjectRole(sprint.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
        Long projectId = sprint.getProject().getId();
        sprintRepository.deleteById(id);
        entityEventSseService.sendEvent(new EntityChangeEvent(EntityEventType.ENTITY_SPRINT, EntityEventType.DELETED, id, projectId));
    }
}
