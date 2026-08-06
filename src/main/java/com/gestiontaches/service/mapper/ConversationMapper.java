package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Conversation;
import com.gestiontaches.service.dto.ConversationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Conversation} and its DTO {@link ConversationDTO}.
 */
@Mapper(componentModel = "spring")
public interface ConversationMapper extends EntityMapper<ConversationDTO, Conversation> {
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "lastMessageAt", ignore = true)
    @Mapping(target = "lastMessagePreview", ignore = true)
    @Mapping(target = "unreadCount", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Override
    ConversationDTO toDto(Conversation conversation);
}
