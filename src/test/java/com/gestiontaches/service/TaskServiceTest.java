package com.gestiontaches.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gestiontaches.domain.Epic;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.Sprint;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.mapper.TaskMapper;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Verifies that every task mutation triggers the automatic status recalculation of the parent
 * sprint(s) and epic(s) (Task 5 - automatic Sprint/Epic status).
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectPermissionService projectPermissionService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SprintService sprintService;

    @Mock
    private EpicService epicService;

    @InjectMocks
    private TaskService taskService;

    private Project project;
    private Sprint sprint;
    private Epic epic;
    private User currentUser;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        sprint = new Sprint();
        sprint.setId(10L);
        sprint.setName("Sprint 1");
        sprint.setProject(project);

        epic = new Epic();
        epic.setId(20L);
        epic.setTitle("Epic 1");
        epic.setProject(project);

        currentUser = new User();
        currentUser.setId(5L);
        currentUser.setLogin("admin");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void partialUpdate_recalculatesParentSprintAndEpic() {
        Task existing = taskInParents();
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(projectPermissionService.getCurrentUserRole(1L)).thenReturn(ProjectRole.OWNER);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        TaskDTO dto = new TaskDTO();
        dto.setId(10L);

        taskService.partialUpdate(dto);

        verify(sprintService).recalculateStatus(10L);
        verify(epicService).recalculateStatus(20L);
    }

    @Test
    void partialUpdate_taskMovedBetweenSprints_recalculatesOldAndNewSprint() {
        Task existing = taskInParents();
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(projectPermissionService.getCurrentUserRole(1L)).thenReturn(ProjectRole.OWNER);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Sprint newSprint = new Sprint();
        newSprint.setId(11L);
        newSprint.setName("Sprint 2");
        newSprint.setProject(project);
        doAnswer(inv -> {
            Task target = inv.getArgument(0);
            target.setSprint(newSprint);
            return null;
        })
            .when(taskMapper)
            .partialUpdate(any(Task.class), any(TaskDTO.class));

        TaskDTO dto = new TaskDTO();
        dto.setId(10L);

        taskService.partialUpdate(dto);

        verify(sprintService).recalculateStatus(10L);
        verify(sprintService).recalculateStatus(11L);
    }

    @Test
    void partialUpdate_taskWithoutParents_doesNotRecalculate() {
        Task existing = new Task();
        existing.setId(10L);
        existing.setStatus(TaskStatus.NEW);
        existing.setProject(project);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(projectPermissionService.getCurrentUserRole(1L)).thenReturn(ProjectRole.OWNER);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskDTO dto = new TaskDTO();
        dto.setId(10L);

        taskService.partialUpdate(dto);

        verify(sprintService, never()).recalculateStatus(any(Long.class));
        verify(epicService, never()).recalculateStatus(any(Long.class));
    }

    @Test
    void delete_recalculatesParentStatuses() {
        Task existing = taskInParents();
        when(taskRepository.findById(10L)).thenReturn(Optional.of(existing));

        taskService.delete(10L);

        verify(taskRepository).deleteById(10L);
        verify(sprintService).recalculateStatus(10L);
        verify(epicService).recalculateStatus(20L);
    }

    @Test
    void save_taskInSprintAndEpic_recalculatesBoth() {
        Task task = taskInParents();
        task.setId(10L);
        when(taskMapper.toEntity(any(TaskDTO.class))).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(Task.class))).thenReturn(new TaskDTO());

        taskService.save(new TaskDTO());

        verify(sprintService).recalculateStatus(10L);
        verify(epicService).recalculateStatus(20L);
    }

    @Test
    void save_newTask_defaultsStatusToNew() {
        Task task = new Task();
        task.setProject(project);
        authenticateAs("admin");
        when(taskMapper.toEntity(any(TaskDTO.class))).thenReturn(task);
        when(userRepository.findOneByLogin("admin")).thenReturn(Optional.of(currentUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(Task.class))).thenReturn(new TaskDTO());

        taskService.save(new TaskDTO());

        assertThat(task.getStatus()).isEqualTo(TaskStatus.NEW);
        assertThat(task.getCreatedBy()).isEqualTo(currentUser);
    }

    @Test
    void createForProject_recalculatesParentStatuses() {
        Task task = taskInParents();
        task.setId(10L);
        task.setStatus(TaskStatus.NEW);
        authenticateAs("admin");
        when(taskMapper.toEntity(any(TaskDTO.class))).thenReturn(task);
        when(userRepository.findOneByLogin("admin")).thenReturn(Optional.of(currentUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toDto(any(Task.class))).thenReturn(new TaskDTO());
        when(projectPermissionService.getCurrentUserRole(1L)).thenReturn(ProjectRole.OWNER);

        TaskDTO result = taskService.createForProject(new TaskDTO(), 1L);

        verify(projectPermissionService).getCurrentUserRole(1L);
        assertThat(result).isNotNull();
        assertThat(task.getCreatedBy()).isEqualTo(currentUser);
        verify(sprintService).recalculateStatus(10L);
        verify(epicService).recalculateStatus(20L);
    }

    private Task taskInParents() {
        Task task = new Task();
        task.setId(10L);
        task.setStatus(TaskStatus.NEW);
        task.setProject(project);
        task.setSprint(sprint);
        task.setEpic(epic);
        return task;
    }

    private void authenticateAs(String login) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login, "credentials"));
    }
}
