package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.User;
import com.gestiontaches.service.dto.ProjectDTO;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Project} and its DTO {@link ProjectDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper extends EntityMapper<ProjectDTO, Project> {
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerLogin", source = "owner.login")
    @Mapping(target = "memberIds", source = "members", qualifiedByName = "userSetToIds")
    ProjectDTO toDto(Project project);

    @Named("userSetToIds")
    default Set<Long> userSetToIds(Set<User> users) {
        if (users == null) return Set.of();
        return users.stream().map(User::getId).collect(Collectors.toSet());
    }
}
