package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.UserPresence;
import com.gestiontaches.service.dto.UserPresenceDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link UserPresence} and its DTO {@link UserPresenceDTO}.
 */
@Mapper(componentModel = "spring")
public interface UserPresenceMapper extends EntityMapper<UserPresenceDTO, UserPresence> {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "online", ignore = true)
    @Mapping(target = "lastActiveAt", source = "lastActiveAt")
    @Override
    UserPresenceDTO toDto(UserPresence userPresence);
}
