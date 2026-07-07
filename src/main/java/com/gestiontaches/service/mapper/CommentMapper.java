package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Comment;
import com.gestiontaches.domain.Issue;
import com.gestiontaches.domain.User;
import com.gestiontaches.service.dto.CommentDTO;
import com.gestiontaches.service.dto.IssueDTO;
import com.gestiontaches.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Comment} and its DTO {@link CommentDTO}.
 */
@Mapper(componentModel = "spring")
public interface CommentMapper extends EntityMapper<CommentDTO, Comment> {
    @Mapping(target = "issue", source = "issue", qualifiedByName = "issueId")
    @Mapping(target = "author", source = "author", qualifiedByName = "userLogin")
    CommentDTO toDto(Comment s);

    @Named("issueId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    IssueDTO toDtoIssueId(Issue issue);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
