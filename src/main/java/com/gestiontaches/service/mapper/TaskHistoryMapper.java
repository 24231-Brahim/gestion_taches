package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.TaskHistory;
import com.gestiontaches.domain.User;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.dto.TaskHistoryDTO;
import com.gestiontaches.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TaskHistory} and its DTO {@link TaskHistoryDTO}.
 */
@Mapper(componentModel = "spring")
public interface TaskHistoryMapper extends EntityMapper<TaskHistoryDTO, TaskHistory> {
    @Mapping(target = "task", source = "task", qualifiedByName = "taskId")
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    TaskHistoryDTO toDto(TaskHistory s);

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
