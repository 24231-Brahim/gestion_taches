package com.gestiontaches.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gestiontaches.domain.Epic;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.enumeration.EpicStatus;
import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.EpicRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.service.mapper.EpicMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EpicServiceTest {

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private EpicMapper epicMapper;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectPermissionService projectPermissionService;

    @Mock
    private EntityEventSseService entityEventSseService;

    @InjectMocks
    private EpicService epicService;

    private Epic epic;
    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        epic = new Epic();
        epic.setId(10L);
        epic.setTitle("Epic 1");
        epic.setStatus(EpicStatus.IN_PROGRESS);
        epic.setProject(project);
    }

    @Test
    void recomputeStatus_allTasksDone_marksEpicDone() {
        when(epicRepository.findById(10L)).thenReturn(Optional.of(epic));

        Task doneTask = new Task();
        doneTask.setId(100L);
        doneTask.setStatus(TaskStatus.DONE);

        Task cancelledTask = new Task();
        cancelledTask.setId(101L);
        cancelledTask.setStatus(TaskStatus.CANCELLED);

        when(taskRepository.findByEpicId(10L)).thenReturn(List.of(doneTask, cancelledTask));
        when(epicRepository.save(any(Epic.class))).thenAnswer(inv -> inv.getArgument(0));

        epicService.recomputeStatus(10L);

        assertThat(epic.getStatus()).isEqualTo(EpicStatus.DONE);
        verify(entityEventSseService).sendEvent(any());
    }

    @Test
    void recomputeStatus_noTasks_keepsCurrentStatus() {
        epic.setStatus(EpicStatus.TODO);
        when(epicRepository.findById(10L)).thenReturn(Optional.of(epic));
        when(taskRepository.findByEpicId(10L)).thenReturn(List.of());

        epicService.recomputeStatus(10L);

        assertThat(epic.getStatus()).isEqualTo(EpicStatus.TODO);
        verify(epicRepository, never()).save(any());
    }

    @Test
    void recomputeStatus_cancelledEpic_isNotOverwritten() {
        epic.setStatus(EpicStatus.CANCELLED);
        when(epicRepository.findById(10L)).thenReturn(Optional.of(epic));

        epicService.recomputeStatus(10L);

        assertThat(epic.getStatus()).isEqualTo(EpicStatus.CANCELLED);
        verify(epicRepository, never()).save(any());
        verify(taskRepository, never()).findByEpicId(10L);
    }

    @Test
    void recomputeStatus_activeTasks_marksEpicInProgress() {
        epic.setStatus(EpicStatus.TODO);
        when(epicRepository.findById(10L)).thenReturn(Optional.of(epic));

        Task inProgressTask = new Task();
        inProgressTask.setId(200L);
        inProgressTask.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findByEpicId(10L)).thenReturn(List.of(inProgressTask));
        when(epicRepository.save(any(Epic.class))).thenAnswer(inv -> inv.getArgument(0));

        epicService.recomputeStatus(10L);

        assertThat(epic.getStatus()).isEqualTo(EpicStatus.IN_PROGRESS);
        verify(entityEventSseService).sendEvent(any());
    }

    @Test
    void recomputeStatus_noActiveTasksButNotAllDone_marksEpicTodo() {
        epic.setStatus(EpicStatus.IN_PROGRESS);
        when(epicRepository.findById(10L)).thenReturn(Optional.of(epic));

        Task todoTask = new Task();
        todoTask.setId(300L);
        todoTask.setStatus(TaskStatus.TODO);

        when(taskRepository.findByEpicId(10L)).thenReturn(List.of(todoTask));
        when(epicRepository.save(any(Epic.class))).thenAnswer(inv -> inv.getArgument(0));

        epicService.recomputeStatus(10L);

        assertThat(epic.getStatus()).isEqualTo(EpicStatus.TODO);
        verify(entityEventSseService).sendEvent(any());
    }
}
