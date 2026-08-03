package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Attachment;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.User;
import com.gestiontaches.service.dto.AttachmentDTO;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Attachment} and its DTO {@link AttachmentDTO}.
 */
@Mapper(componentModel = "spring")
public interface AttachmentMapper extends EntityMapper<AttachmentDTO, Attachment> {
    @Mapping(target = "task", source = "task", qualifiedByName = "taskId")
    @Mapping(target = "uploadedBy", source = "uploadedBy", qualifiedByName = "userLogin")
    AttachmentDTO toDto(Attachment s);

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
