package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Attachment;
import com.gestiontaches.domain.Issue;
import com.gestiontaches.service.dto.AttachmentDTO;
import com.gestiontaches.service.dto.IssueDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Attachment} and its DTO {@link AttachmentDTO}.
 */
@Mapper(componentModel = "spring")
public interface AttachmentMapper extends EntityMapper<AttachmentDTO, Attachment> {
    @Mapping(target = "issue", source = "issue", qualifiedByName = "issueId")
    AttachmentDTO toDto(Attachment s);

    @Named("issueId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    IssueDTO toDtoIssueId(Issue issue);
}
