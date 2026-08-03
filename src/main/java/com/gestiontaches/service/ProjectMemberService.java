package com.gestiontaches.service;

import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.service.dto.ProjectMemberDTO;
import com.gestiontaches.service.mapper.ProjectMemberMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProjectMemberService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectMemberService.class);

    private final ProjectMemberRepository projectMemberRepository;

    private final ProjectMemberMapper projectMemberMapper;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository, ProjectMemberMapper projectMemberMapper) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectMemberMapper = projectMemberMapper;
    }

    public List<ProjectMember> getMembersByProjectId(Long projectId) {
        LOG.debug("Request to get members for project : {}", projectId);
        return projectMemberRepository.findByProjectId(projectId);
    }

    public Page<ProjectMemberDTO> findAllWithProjectAndUser(Pageable pageable) {
        LOG.debug("Request to get all project members with project and user information");
        return projectMemberRepository.findAllWithProjectAndUser(pageable).map(projectMemberMapper::toDto);
    }
}
