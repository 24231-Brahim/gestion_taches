package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Notification;
import com.gestiontaches.service.dto.NotificationDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface NotificationMapper extends EntityMapper<NotificationDTO, Notification> {}
