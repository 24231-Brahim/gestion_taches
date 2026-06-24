package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Epic;
import com.gestiontaches.domain.Project;
import com.gestiontaches.service.dto.EpicDTO;
import com.gestiontaches.service.dto.ProjectDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Epic} and its DTO {@link EpicDTO}.
 */
@Mapper(componentModel = "spring")
public interface EpicMapper extends EntityMapper<EpicDTO, Epic> {
    @Mapping(target = "project", source = "project", qualifiedByName = "projectName")
    EpicDTO toDto(Epic s);

    @Named("projectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProjectDTO toDtoProjectName(Project project);
}
