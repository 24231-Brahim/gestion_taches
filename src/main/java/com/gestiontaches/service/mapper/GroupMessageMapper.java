package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.GroupMessage;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.User;
import com.gestiontaches.service.dto.GroupMessageDTO;
import com.gestiontaches.service.dto.ProjectDTO;
import com.gestiontaches.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link GroupMessage} and its DTO {@link GroupMessageDTO}.
 */
@Mapper(componentModel = "spring")
public interface GroupMessageMapper extends EntityMapper<GroupMessageDTO, GroupMessage> {
    @Mapping(target = "sender", source = "sender", qualifiedByName = "userLogin")
    @Mapping(target = "recipient", source = "recipient", qualifiedByName = "userLogin")
    @Mapping(target = "project", source = "project", qualifiedByName = "projectId")
    GroupMessageDTO toDto(GroupMessage s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("projectId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    ProjectDTO toDtoProjectId(Project project);
}
