package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.TaskTransition;
import com.gestiontaches.domain.User;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.dto.TaskTransitionDTO;
import com.gestiontaches.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TaskTransition} and its DTO {@link TaskTransitionDTO}.
 */
@Mapper(componentModel = "spring")
public interface TaskTransitionMapper extends EntityMapper<TaskTransitionDTO, TaskTransition> {
    @Mapping(target = "task", source = "task", qualifiedByName = "taskId")
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    TaskTransitionDTO toDto(TaskTransition s);

    @Named("taskId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TaskDTO toDtoTaskId(Task task);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
