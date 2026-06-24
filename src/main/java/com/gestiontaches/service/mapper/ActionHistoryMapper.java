package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.ActionHistory;
import com.gestiontaches.domain.Issue;
import com.gestiontaches.service.dto.ActionHistoryDTO;
import com.gestiontaches.service.dto.IssueDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link ActionHistory} and its DTO {@link ActionHistoryDTO}.
 */
@Mapper(componentModel = "spring")
public interface ActionHistoryMapper extends EntityMapper<ActionHistoryDTO, ActionHistory> {
    @Mapping(target = "issue", source = "issue", qualifiedByName = "issueId")
    ActionHistoryDTO toDto(ActionHistory s);

    @Named("issueId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    IssueDTO toDtoIssueId(Issue issue);
}
