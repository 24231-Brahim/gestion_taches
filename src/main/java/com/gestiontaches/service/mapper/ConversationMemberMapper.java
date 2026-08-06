package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.User;
import com.gestiontaches.service.dto.ChatMemberDTO;
import com.gestiontaches.service.dto.ConversationMemberDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link com.gestiontaches.domain.ConversationMember} and its DTO
 * {@link ConversationMemberDTO}.
 */
@Mapper(componentModel = "spring")
public interface ConversationMemberMapper extends EntityMapper<ConversationMemberDTO, com.gestiontaches.domain.ConversationMember> {
    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userLogin", source = "user.login")
    @Override
    ConversationMemberDTO toDto(com.gestiontaches.domain.ConversationMember conversationMember);
}
