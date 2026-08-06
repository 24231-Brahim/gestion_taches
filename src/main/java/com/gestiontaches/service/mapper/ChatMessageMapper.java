package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.ChatMessage;
import com.gestiontaches.service.dto.ChatMessageDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ChatMessage} and its DTO {@link ChatMessageDTO}.
 */
@Mapper(componentModel = "spring", uses = { UserMapper.class, MessageReactionMapper.class, ChatAttachmentMapper.class })
public interface ChatMessageMapper extends EntityMapper<ChatMessageDTO, ChatMessage> {
    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "sender", source = "sender", qualifiedByName = "login")
    @Mapping(target = "parentMessageId", source = "parentMessage.id")
    @Mapping(target = "reactions", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Override
    ChatMessageDTO toDto(ChatMessage chatMessage);
}
