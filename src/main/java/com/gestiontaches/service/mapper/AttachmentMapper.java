package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Attachment;
import com.gestiontaches.domain.Task;
import com.gestiontaches.service.dto.AttachmentDTO;
import com.gestiontaches.service.dto.TaskDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Attachment} and its DTO {@link AttachmentDTO}.
 */
@Mapper(componentModel = "spring")
public interface AttachmentMapper extends EntityMapper<AttachmentDTO, Attachment> {
    @Mapping(target = "task", source = "task", qualifiedByName = "taskId")
    AttachmentDTO toDto(Attachment s);

    @Named("taskId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    TaskDTO toDtoTaskId(Task task);
}
