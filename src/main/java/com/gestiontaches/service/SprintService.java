package com.gestiontaches.service;

import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.Sprint;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.TaskHistory;
import com.gestiontaches.domain.TaskTransition;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.domain.enumeration.SprintStatus;
import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.SprintRepository;
import com.gestiontaches.repository.TaskHistoryRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.TaskTransitionRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.service.dto.SprintDTO;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.dto.VelocityReportDTO;
import com.gestiontaches.service.mapper.SprintMapper;
import com.gestiontaches.service.mapper.TaskMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final TaskTransitionRepository taskTransitionRepository;
    private final NotificationService notificationService;
    private final ProjectMemberService projectMemberService;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public SprintService(
        SprintRepository sprintRepository,
        SprintMapper sprintMapper,
        ProjectPermissionService projectPermissionService,
        TaskRepository taskRepository,
        TaskHistoryRepository taskHistoryRepository,
        TaskTransitionRepository taskTransitionRepository,
        NotificationService notificationService,
        ProjectMemberService projectMemberService,
        UserRepository userRepository,
        TaskMapper taskMapper
    ) {
        this.sprintRepository = sprintRepository;
        this.sprintMapper = sprintMapper;
        this.projectPermissionService = projectPermissionService;
        this.taskRepository = taskRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.taskTransitionRepository = taskTransitionRepository;
        this.notificationService = notificationService;
        this.projectMemberService = projectMemberService;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

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

    public SprintDTO startSprint(Long sprintId) {
        LOG.debug("Request to start Sprint : {}", sprintId);
        Sprint sprint = sprintRepository.findById(sprintId).orElseThrow(() -> new RuntimeException("Sprint not found"));
        projectPermissionService.requireProjectRole(sprint.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new RuntimeException("Only a PLANNED sprint can be started");
        }

        Optional<Sprint> existingActive = sprintRepository.findByProjectIdAndStatus(sprint.getProject().getId(), SprintStatus.ACTIVE);
        if (existingActive.isPresent() && !existingActive.get().getId().equals(sprintId)) {
            throw new RuntimeException("A project can only have one active sprint at a time");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        sprint = sprintRepository.save(sprint);

        List<Task> tasksInProgress = taskRepository.findBySprintIdAndStatus(sprintId, TaskStatus.NEW);
        Instant now = Instant.now();
        User currentUser = resolveCurrentUser();

        for (Task task : tasksInProgress) {
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(TaskStatus.TODO);
            task.setUpdatedAt(now);
            taskRepository.save(task);

            TaskTransition transition = new TaskTransition();
            transition.setTask(task);
            transition.setFromStatus(oldStatus.name());
            transition.setToStatus(TaskStatus.TODO.name());
            transition.setUser(currentUser);
            transition.setCreatedAt(now);
            taskTransitionRepository.save(transition);
        }

        notifyProjectMembers(sprint, currentUser, "Le sprint \"" + sprint.getName() + "\" a démarré");

        return sprintMapper.toDto(sprint);
    }

    public VelocityReportDTO closeSprint(Long sprintId) {
        LOG.debug("Request to close Sprint : {}", sprintId);
        Sprint sprint = sprintRepository.findById(sprintId).orElseThrow(() -> new RuntimeException("Sprint not found"));
        projectPermissionService.requireProjectRole(sprint.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new RuntimeException("Only an ACTIVE sprint can be closed");
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
            }
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        sprint = sprintRepository.save(sprint);

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
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Current user not found"));
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
        Sprint sprint = sprintRepository.findById(id).orElseThrow(() -> new RuntimeException("Sprint not found"));
        projectPermissionService.requireProjectRole(sprint.getProject().getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
        sprintRepository.deleteById(id);
    }
}
