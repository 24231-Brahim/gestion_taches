package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.ChatMessage;
import com.gestiontaches.service.dto.ChatMessageDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ChatMessage} and its DTO {@link ChatMessageDTO}.
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface ChatMessageMapper extends EntityMapper<ChatMessageDTO, ChatMessage> {
    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "sender", source = "sender", qualifiedByName = "login")
    @Mapping(target = "parentMessageId", source = "parentMessage.id")
    @Override
    ChatMessageDTO toDto(ChatMessage chatMessage);
}
