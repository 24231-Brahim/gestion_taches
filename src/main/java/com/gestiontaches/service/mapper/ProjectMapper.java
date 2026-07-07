package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.Project;
import com.gestiontaches.service.dto.ProjectDTO;
import com.gestiontaches.service.dto.ProjectMemberDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { ProjectMemberMapper.class })
public interface ProjectMapper extends EntityMapper<ProjectDTO, Project> {
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerLogin", source = "owner.login")
    @Mapping(target = "projectMembers", source = "projectMembers")
    ProjectDTO toDto(Project project);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "projectMembers", ignore = true)
    Project toEntity(ProjectDTO projectDTO);
}
