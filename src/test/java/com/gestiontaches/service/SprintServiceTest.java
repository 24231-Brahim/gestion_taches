package com.gestiontaches.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.Sprint;
import com.gestiontaches.domain.Task;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SprintServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private SprintMapper sprintMapper;

    @Mock
    private ProjectPermissionService projectPermissionService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @Mock
    private TaskTransitionRepository taskTransitionRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private SprintService sprintService;

    private Sprint sprint;
    private SprintDTO sprintDTO;
    private Project project;
    private User currentUser;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        sprint = new Sprint();
        sprint.setId(10L);
        sprint.setName("Sprint 1");
        sprint.setStatus(SprintStatus.PLANNED);
        sprint.setProject(project);

        sprintDTO = new SprintDTO();
        sprintDTO.setId(10L);
        sprintDTO.setName("Sprint 1");
        sprintDTO.setStatus(SprintStatus.PLANNED);

        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setLogin("admin");
    }

    @Test
    void startSprint_shouldSetActiveStatusAndTransitionTasks() {
        when(sprintRepository.findById(10L)).thenReturn(Optional.of(sprint));
        when(projectPermissionService.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        Task task1 = new Task();
        task1.setId(100L);
        task1.setStatus(TaskStatus.NEW);
        task1.setSprint(sprint);

        Task task2 = new Task();
        task2.setId(101L);
        task2.setStatus(TaskStatus.NEW);
        task2.setSprint(sprint);

        when(taskRepository.findBySprintIdAndStatus(10L, TaskStatus.NEW)).thenReturn(List.of(task1, task2));
        when(sprintRepository.save(any(Sprint.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(projectMemberService.getMembersByProjectId(1L)).thenReturn(List.of());
        when(sprintMapper.toDto(any(Sprint.class))).thenAnswer(inv -> {
            Sprint s = inv.getArgument(0);
            SprintDTO dto = new SprintDTO();
            dto.setId(s.getId());
            dto.setName(s.getName());
            dto.setStatus(s.getStatus());
            return dto;
        });

        SprintDTO result = sprintService.startSprint(10L);

        assertThat(result.getStatus()).isEqualTo(SprintStatus.ACTIVE);
        verify(projectPermissionService).requireProjectRole(1L, ProjectRole.OWNER, ProjectRole.MANAGER);
        verify(taskRepository, times(2)).save(any(Task.class));
        verify(taskTransitionRepository, times(2)).save(any());
        assertThat(task1.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(task2.getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void startSprint_shouldThrowIfNotPlanned() {
        sprint.setStatus(SprintStatus.ACTIVE);
        when(sprintRepository.findById(10L)).thenReturn(Optional.of(sprint));

        assertThatThrownBy(() -> sprintService.startSprint(10L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Only a PLANNED sprint can be started");
    }

    @Test
    void startSprint_shouldThrowIfAnotherSprintIsActive() {
        Sprint activeSprint = new Sprint();
        activeSprint.setId(9L);
        activeSprint.setStatus(SprintStatus.ACTIVE);
        activeSprint.setProject(project);

        when(sprintRepository.findById(10L)).thenReturn(Optional.of(sprint));
        when(sprintRepository.findByProjectIdAndStatus(1L, SprintStatus.ACTIVE)).thenReturn(Optional.of(activeSprint));

        assertThatThrownBy(() -> sprintService.startSprint(10L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("A project can only have one active sprint at a time");
    }

    @Test
    void closeSprint_shouldMoveIncompleteTasksToBacklogAndReturnVelocity() {
        sprint.setStatus(SprintStatus.ACTIVE);
        when(sprintRepository.findById(10L)).thenReturn(Optional.of(sprint));
        when(projectPermissionService.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        Task doneTask = new Task();
        doneTask.setId(200L);
        doneTask.setStatus(TaskStatus.DONE);
        doneTask.setSprint(sprint);

        Task todoTask = new Task();
        todoTask.setId(201L);
        todoTask.setStatus(TaskStatus.TODO);
        todoTask.setSprint(sprint);

        Task inProgressTask = new Task();
        inProgressTask.setId(202L);
        inProgressTask.setStatus(TaskStatus.IN_PROGRESS);
        inProgressTask.setSprint(sprint);

        when(taskRepository.findBySprintId(10L)).thenReturn(List.of(doneTask, todoTask, inProgressTask));
        when(sprintRepository.save(any(Sprint.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        VelocityReportDTO report = sprintService.closeSprint(10L);

        assertThat(report.getTachesPrevues()).isEqualTo(3);
        assertThat(report.getTachesTerminees()).isEqualTo(1);
        assertThat(report.getPourcentage()).isEqualTo(33);
        assertThat(report.getTachesReportees()).isEqualTo(2);

        assertThat(sprint.getStatus()).isEqualTo(SprintStatus.COMPLETED);
        assertThat(todoTask.getSprint()).isNull();
        assertThat(inProgressTask.getSprint()).isNull();
        assertThat(doneTask.getSprint()).isNotNull();

        verify(taskHistoryRepository, times(2)).save(any());
    }

    @Test
    void closeSprint_shouldThrowIfNotActive() {
        sprint.setStatus(SprintStatus.PLANNED);
        when(sprintRepository.findById(10L)).thenReturn(Optional.of(sprint));

        assertThatThrownBy(() -> sprintService.closeSprint(10L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Only an ACTIVE sprint can be closed");
    }

    @Test
    void closeSprint_shouldReturn100PercentWhenAllDone() {
        sprint.setStatus(SprintStatus.ACTIVE);
        when(sprintRepository.findById(10L)).thenReturn(Optional.of(sprint));
        when(projectPermissionService.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        Task doneTask = new Task();
        doneTask.setId(300L);
        doneTask.setStatus(TaskStatus.DONE);
        doneTask.setSprint(sprint);

        when(taskRepository.findBySprintId(10L)).thenReturn(List.of(doneTask));
        when(sprintRepository.save(any(Sprint.class))).thenAnswer(inv -> inv.getArgument(0));

        VelocityReportDTO report = sprintService.closeSprint(10L);

        assertThat(report.getTachesPrevues()).isEqualTo(1);
        assertThat(report.getTachesTerminees()).isEqualTo(1);
        assertThat(report.getPourcentage()).isEqualTo(100);
        assertThat(report.getTachesReportees()).isEqualTo(0);
    }

    @Test
    void closeSprint_shouldReturnZeroPercentWhenNoneDone() {
        sprint.setStatus(SprintStatus.ACTIVE);
        when(sprintRepository.findById(10L)).thenReturn(Optional.of(sprint));
        when(projectPermissionService.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        Task todoTask = new Task();
        todoTask.setId(400L);
        todoTask.setStatus(TaskStatus.TODO);
        todoTask.setSprint(sprint);

        when(taskRepository.findBySprintId(10L)).thenReturn(List.of(todoTask));
        when(sprintRepository.save(any(Sprint.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        VelocityReportDTO report = sprintService.closeSprint(10L);

        assertThat(report.getTachesPrevues()).isEqualTo(1);
        assertThat(report.getTachesTerminees()).isEqualTo(0);
        assertThat(report.getPourcentage()).isEqualTo(0);
        assertThat(report.getTachesReportees()).isEqualTo(1);
    }

    @Test
    void getBacklogTasks_shouldReturnProjectTasksWithNullSprint() {
        Task backlogTask = new Task();
        backlogTask.setId(500L);
        backlogTask.setSprint(null);

        TaskDTO backlogDto = new TaskDTO();
        backlogDto.setId(500L);

        when(taskRepository.findByProjectIdAndSprintIsNullWithToOneRelationships(1L)).thenReturn(List.of(backlogTask));
        when(taskMapper.toDto(backlogTask)).thenReturn(backlogDto);

        List<TaskDTO> result = sprintService.getBacklogTasks(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(500L);
    }
}
