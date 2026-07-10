package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Notification;
import com.gestiontaches.service.dto.NotificationDTO;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public Notification toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }

        Notification entity = new Notification();
        entity.setId(dto.getId());
        entity.setMessage(dto.getMessage());
        entity.setIssueId(dto.getIssueId());
        entity.setIssueTitle(dto.getIssueTitle());
        entity.setUserId(dto.getUserId());
        entity.setIsRead(dto.getIsRead());
        entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }

    @Override
    public NotificationDTO toDto(Notification entity) {
        if (entity == null) {
            return null;
        }

        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setMessage(entity.getMessage());
        dto.setIssueId(entity.getIssueId());
        dto.setIssueTitle(entity.getIssueTitle());
        dto.setUserId(entity.getUserId());
        dto.setIsRead(entity.getIsRead());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    @Override
    public List<Notification> toEntity(List<NotificationDTO> dtoList) {
        if (dtoList == null) {
            return null;
        }
        return dtoList.stream().map(this::toEntity).toList();
    }

    @Override
    public List<NotificationDTO> toDto(List<Notification> entityList) {
        if (entityList == null) {
            return null;
        }
        return entityList.stream().map(this::toDto).toList();
    }

    @Override
    public void partialUpdate(Notification entity, NotificationDTO dto) {
        if (entity == null || dto == null) {
            return;
        }
        if (dto.getMessage() != null) {
            entity.setMessage(dto.getMessage());
        }
        if (dto.getIssueId() != null) {
            entity.setIssueId(dto.getIssueId());
        }
        if (dto.getIssueTitle() != null) {
            entity.setIssueTitle(dto.getIssueTitle());
        }
        if (dto.getUserId() != null) {
            entity.setUserId(dto.getUserId());
        }
        if (dto.getIsRead() != null) {
            entity.setIsRead(dto.getIsRead());
        }
        if (dto.getCreatedAt() != null) {
            entity.setCreatedAt(dto.getCreatedAt());
        }
    }
}
