package com.gestiontaches.service.mapper;

import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.service.dto.ProjectMemberDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper extends EntityMapper<ProjectMemberDTO, ProjectMember> {
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userLogin", source = "user.login")
    ProjectMemberDTO toDto(ProjectMember projectMember);
}
