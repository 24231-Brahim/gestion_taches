package com.gestiontaches.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.NotificationRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.mapper.NotificationMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User admin1;
    private User admin2;
    private Task task;

    @BeforeEach
    void setUp() {
        admin1 = new User();
        admin1.setId(10L);
        admin1.setLogin("admin1");

        admin2 = new User();
        admin2.setId(11L);
        admin2.setLogin("admin2");

        task = new Task();
        task.setId(100L);
        task.setTitle("Fix login bug");
    }

    @Test
    void notifyAdminsOfTaskMovedToBacklog_shouldCreateNotificationForEachAdmin() {
        when(userRepository.findAllActivatedByAuthorityNames(List.of(AuthoritiesConstants.ADMIN))).thenReturn(List.of(admin1, admin2));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyAdminsOfTaskMovedToBacklog(task, "Sprint Alpha");

        verify(notificationRepository, times(2)).save(any());

        ArgumentCaptor<com.gestiontaches.domain.Notification> captor = ArgumentCaptor.forClass(com.gestiontaches.domain.Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<com.gestiontaches.domain.Notification> saved = captor.getAllValues();

        assertThat(saved.get(0).getMessage()).contains("Fix login bug");
        assertThat(saved.get(0).getMessage()).contains("TASK_MOVED_TO_BACKLOG");
        assertThat(saved.get(0).getMessage()).contains("Sprint Alpha");
        assertThat(saved.get(0).getMessage()).contains("Backlog");
        assertThat(saved.get(0).getTask()).isEqualTo(task);
        assertThat(saved.get(0).getTaskTitle()).isEqualTo("Fix login bug");
        assertThat(saved.get(0).getUser()).isEqualTo(admin1);
        assertThat(saved.get(0).getIsRead()).isFalse();

        assertThat(saved.get(1).getMessage()).contains("Fix login bug");
        assertThat(saved.get(1).getTask()).isEqualTo(task);
        assertThat(saved.get(1).getTaskTitle()).isEqualTo("Fix login bug");
        assertThat(saved.get(1).getUser()).isEqualTo(admin2);
    }

    @Test
    void notifyAdminsOfTaskMovedToBacklog_shouldNotCreateNotificationWhenNoAdmins() {
        when(userRepository.findAllActivatedByAuthorityNames(List.of(AuthoritiesConstants.ADMIN))).thenReturn(List.of());

        notificationService.notifyAdminsOfTaskMovedToBacklog(task, "Sprint Alpha");

        verify(notificationRepository, never()).save(any());
    }
}
