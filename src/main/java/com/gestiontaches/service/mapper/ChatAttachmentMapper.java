package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.ChatAttachment;
import com.gestiontaches.service.dto.ChatAttachmentDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ChatAttachment} and its DTO {@link ChatAttachmentDTO}.
 */
@Mapper(componentModel = "spring")
public interface ChatAttachmentMapper extends EntityMapper<ChatAttachmentDTO, ChatAttachment> {
    @Mapping(target = "messageId", source = "message.id")
    @Mapping(target = "uploadedByLogin", source = "uploadedBy.login")
    @Override
    ChatAttachmentDTO toDto(ChatAttachment chatAttachment);
}
