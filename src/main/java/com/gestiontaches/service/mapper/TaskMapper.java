package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Epic;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.Sprint;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.User;
import com.gestiontaches.service.dto.EpicDTO;
import com.gestiontaches.service.dto.ProjectDTO;
import com.gestiontaches.service.dto.SprintDTO;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Task} and its DTO {@link TaskDTO}.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper extends EntityMapper<TaskDTO, Task> {
    @Mapping(target = "sprint", source = "sprint", qualifiedByName = "sprintName")
    @Mapping(target = "epic", source = "epic", qualifiedByName = "epicTitle")
    @Mapping(target = "project", source = "project", qualifiedByName = "projectName")
    @Mapping(target = "assignee", source = "assignee", qualifiedByName = "userLogin")
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "userLogin")
    TaskDTO toDto(Task s);

    @Named("sprintName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    SprintDTO toDtoSprintName(Sprint sprint);

    @Named("epicTitle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    EpicDTO toDtoEpicTitle(Epic epic);

    @Named("projectName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    ProjectDTO toDtoProjectName(Project project);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
