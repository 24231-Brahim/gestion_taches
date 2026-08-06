package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.MessageReaction;
import com.gestiontaches.service.dto.MessageReactionDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link MessageReaction} and its DTO {@link MessageReactionDTO}.
 */
@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface MessageReactionMapper extends EntityMapper<MessageReactionDTO, MessageReaction> {
    @Mapping(target = "messageId", source = "message.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userLogin", source = "user.login")
    @Override
    MessageReactionDTO toDto(MessageReaction messageReaction);
}
