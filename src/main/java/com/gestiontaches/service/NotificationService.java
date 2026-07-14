package com.gestiontaches.service;

import com.gestiontaches.domain.Notification;
import com.gestiontaches.repository.NotificationRepository;
import com.gestiontaches.service.dto.NotificationDTO;
import com.gestiontaches.service.mapper.NotificationMapper;
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

    public NotificationService(NotificationRepository notificationRepository, NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    public NotificationDTO save(NotificationDTO notificationDTO) {
        LOG.debug("Request to save Notification : {}", notificationDTO);
        Notification notification = notificationMapper.toEntity(notificationDTO);
        notification = notificationRepository.save(notification);
        return notificationMapper.toDto(notification);
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
}
