package com.gestiontaches.service;

import com.gestiontaches.domain.Notification;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.TaskHistory;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.NotificationRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.dto.NotificationDTO;
import com.gestiontaches.service.mapper.NotificationMapper;
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
public class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final NotificationSseService notificationSseService;

    public NotificationService(
        NotificationRepository notificationRepository,
        NotificationMapper notificationMapper,
        UserRepository userRepository,
        NotificationSseService notificationSseService
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.userRepository = userRepository;
        this.notificationSseService = notificationSseService;
    }

    public NotificationDTO save(NotificationDTO notificationDTO) {
        LOG.debug("Request to save Notification : {}", notificationDTO);
        Notification notification = notificationMapper.toEntity(notificationDTO);
        notification = notificationRepository.save(notification);
        NotificationDTO saved = notificationMapper.toDto(notification);
        if (saved.getUserId() != null) {
            notificationSseService.sendNotification(saved.getUserId(), saved);
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> findByUserId(Long userId) {
        LOG.debug("Request to get Notifications for user : {}", userId);
        return notificationRepository.findByUser_idOrderByCreatedAtDesc(userId).stream().map(notificationMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<NotificationDTO> findByUserId(Long userId, Pageable pageable) {
        LOG.debug("Request to get paginated Notifications for user : {}", userId);
        return notificationRepository.findByUser_idOrderByCreatedAtDesc(userId, pageable).map(notificationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countUnreadByUserId(Long userId) {
        return notificationRepository.countByUser_idAndIsReadFalse(userId);
    }

    public Optional<NotificationDTO> partialUpdate(NotificationDTO notificationDTO) {
        LOG.debug("Request to partially update Notification : {}", notificationDTO);
        return notificationRepository
            .findById(notificationDTO.getId())
            .map(existing -> {
                if (notificationDTO.getIsRead() != null) {
                    existing.setIsRead(notificationDTO.getIsRead());
                }
                return existing;
            })
            .map(notificationRepository::save)
            .map(notificationMapper::toDto);
    }

    public int markAllAsRead(Long userId) {
        LOG.debug("Request to mark all notifications as read for user : {}", userId);
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    public void notifyAdminsOfNewUser(User newUser) {
        List<User> admins = userRepository.findAllActivatedByAuthorityNames(List.of(AuthoritiesConstants.ADMIN));
        String message = "New user registered: " + newUser.getLogin() + " (" + newUser.getEmail() + ")";
        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setMessage(message);
            notification.setUser(admin);
            notification.setIsRead(false);
            notification.setCreatedAt(Instant.now());
            notificationRepository.save(notification);
        }
        LOG.debug("Sent new user registration notification to {} admin(s)", admins.size());
    }

    public void notifyAdminsOfTaskHistory(TaskHistory history) {
        List<User> admins = userRepository.findAllActivatedByAuthorityNames(List.of(AuthoritiesConstants.ADMIN));
        Task task = history.getTask();
        String message = "Task \"" + task.getTitle() + "\" — " + history.getAction();
        if (history.getOldValue() != null || history.getNewValue() != null) {
            message +=
                " (" +
                (history.getOldValue() != null ? history.getOldValue() : "") +
                " → " +
                (history.getNewValue() != null ? history.getNewValue() : "") +
                ")";
        }
        for (User admin : admins) {
            Notification notification = new Notification();
            notification.setMessage(message);
            notification.setTask(task);
            notification.setTaskTitle(task.getTitle());
            notification.setUser(admin);
            notification.setIsRead(false);
            notification.setCreatedAt(Instant.now());
            notificationRepository.save(notification);
        }
        LOG.debug("Sent task history notification to {} admin(s) for task {}", admins.size(), task.getId());
    }
}
