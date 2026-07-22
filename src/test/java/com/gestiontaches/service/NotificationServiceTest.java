package com.gestiontaches.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.TaskHistory;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.NotificationRepository;
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

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationSseService notificationSseService;

    @InjectMocks
    private NotificationService notificationService;

    private User admin1;
    private User admin2;
    private Task task;
    private TaskHistory history;

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

        history = new TaskHistory();
        history.setId(1L);
        history.setTask(task);
        history.setAction("STATUS_CHANGED");
        history.setOldValue("TODO");
        history.setNewValue("IN_PROGRESS");
        history.setCreatedAt(java.time.Instant.now());
    }

    @Test
    void notifyAdminsOfTaskHistory_shouldCreateNotificationForEachAdmin() {
        when(userRepository.findAllActivatedByAuthorityNames(List.of(AuthoritiesConstants.ADMIN))).thenReturn(List.of(admin1, admin2));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.notifyAdminsOfTaskHistory(history);

        verify(notificationRepository, times(2)).save(any());

        ArgumentCaptor<com.gestiontaches.domain.Notification> captor = ArgumentCaptor.forClass(com.gestiontaches.domain.Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<com.gestiontaches.domain.Notification> saved = captor.getAllValues();

        assertThat(saved.get(0).getMessage()).contains("Fix login bug");
        assertThat(saved.get(0).getMessage()).contains("STATUS_CHANGED");
        assertThat(saved.get(0).getMessage()).contains("TODO → IN_PROGRESS");
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
    void notifyAdminsOfTaskHistory_shouldOmitArrowWhenNoOldOrNewValue() {
        when(userRepository.findAllActivatedByAuthorityNames(List.of(AuthoritiesConstants.ADMIN))).thenReturn(List.of(admin1));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        history.setOldValue(null);
        history.setNewValue(null);

        notificationService.notifyAdminsOfTaskHistory(history);

        ArgumentCaptor<com.gestiontaches.domain.Notification> captor = ArgumentCaptor.forClass(com.gestiontaches.domain.Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        com.gestiontaches.domain.Notification saved = captor.getValue();
        assertThat(saved.getMessage()).contains("STATUS_CHANGED");
        assertThat(saved.getMessage()).doesNotContain("→");
    }

    @Test
    void notifyAdminsOfTaskHistory_shouldNotCreateNotificationWhenNoAdmins() {
        when(userRepository.findAllActivatedByAuthorityNames(List.of(AuthoritiesConstants.ADMIN))).thenReturn(List.of());

        notificationService.notifyAdminsOfTaskHistory(history);

        verify(notificationRepository, never()).save(any());
    }
}
