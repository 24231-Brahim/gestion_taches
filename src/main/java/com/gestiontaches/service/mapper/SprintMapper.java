package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.Sprint;
import com.gestiontaches.service.dto.ProjectDTO;
import com.gestiontaches.service.dto.SprintDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Sprint} and its DTO {@link SprintDTO}.
 */
@Mapper(componentModel = "spring")
public interface SprintMapper extends EntityMapper<SprintDTO, Sprint> {
    @Mapping(target = "project", source = "project", qualifiedByName = "projectName")
    SprintDTO toDto(Sprint s);

    @Named("projectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "key", source = "key")
    ProjectDTO toDtoProjectName(Project project);
}
