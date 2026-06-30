package com.gestiontaches.service;

import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.ProjectDTO;
import com.gestiontaches.service.dto.ProjectMemberDTO;
import com.gestiontaches.service.mapper.ProjectMapper;
import com.gestiontaches.service.mapper.ProjectMemberMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.gestiontaches.domain.Project}.
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;

    private final ProjectMapper projectMapper;

    private final UserRepository userRepository;

    private final ProjectMemberRepository projectMemberRepository;

    private final ProjectMemberMapper projectMemberMapper;

    public ProjectService(
        ProjectRepository projectRepository,
        ProjectMapper projectMapper,
        UserRepository userRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectMemberMapper projectMemberMapper
    ) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectMemberMapper = projectMemberMapper;
    }

    /**
     * Save a project.
     *
     * @param projectDTO the entity to save.
     * @return the persisted entity.
     */
    public ProjectDTO save(ProjectDTO projectDTO) {
        LOG.debug("Request to save Project : {}", projectDTO);
        Project project = projectMapper.toEntity(projectDTO);
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        User owner = userRepository.findOneByLogin(currentLogin).orElseThrow(() -> new RuntimeException("User not found: " + currentLogin));
        project.setOwner(owner);
        project = projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    /**
     * Update a project.
     *
     * @param projectDTO the entity to save.
     * @return the persisted entity.
     */
    public ProjectDTO update(ProjectDTO projectDTO) {
        LOG.debug("Request to update Project : {}", projectDTO);
        return projectRepository
            .findById(projectDTO.getId())
            .map(existingProject -> {
                projectMapper.partialUpdate(existingProject, projectDTO);
                return existingProject;
            })
            .map(projectRepository::save)
            .map(projectMapper::toDto)
            .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    /**
     * Partially update a project.
     *
     * @param projectDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ProjectDTO> partialUpdate(ProjectDTO projectDTO) {
        LOG.debug("Request to partially update Project : {}", projectDTO);

        return projectRepository
            .findById(projectDTO.getId())
            .map(existingProject -> {
                projectMapper.partialUpdate(existingProject, projectDTO);

                return existingProject;
            })
            .map(projectRepository::save)
            .map(projectMapper::toDto);
    }

    /**
     * Get all the projects.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ProjectDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Projects");
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return projectRepository.findAll(pageable).map(projectMapper::toDto);
        }
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        return projectRepository.findByOwnerLogin(login, pageable).map(projectMapper::toDto);
    }

    /**
     * Get one project by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ProjectDTO> findOne(Long id) {
        LOG.debug("Request to get Project : {}", id);
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return projectRepository.findById(id).map(projectMapper::toDto);
        }
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        return projectRepository.findByIdAndOwnerLogin(id, login).map(projectMapper::toDto);
    }

    /**
     * Delete the project by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Project : {}", id);
        checkOwnership(id);
        projectRepository.deleteById(id);
    }

    public Set<ProjectMemberDTO> getMembers(Long projectId) {
        LOG.debug("Request to get members of Project : {}", projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        checkOwnership(project);
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        return members.stream().map(projectMemberMapper::toDto).collect(Collectors.toSet());
    }

    public void addMember(Long projectId, Long userId) {
        LOG.debug("Request to add user {} to Project {}", userId, projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        checkOwnership(project);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (projectMemberRepository.findByProjectIdAndUserId(projectId, userId).isPresent()) {
            throw new RuntimeException("User is already a member of this project");
        }
        ProjectMember member = new ProjectMember().project(project).user(user).role("MEMBER").joinedAt(Instant.now());
        projectMemberRepository.save(member);
    }

    public void removeMember(Long projectId, Long userId) {
        LOG.debug("Request to remove user {} from Project {}", userId, projectId);
        checkOwnership(projectId);
        ProjectMember member = projectMemberRepository
            .findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        projectMemberRepository.delete(member);
    }

    public void updateMemberRole(Long projectId, Long userId, ProjectMemberDTO memberDTO) {
        LOG.debug("Request to update role of user {} in Project {}", userId, projectId);
        checkOwnership(projectId);
        ProjectMember member = projectMemberRepository
            .findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setRole(memberDTO.getRole());
        projectMemberRepository.save(member);
    }

    private void checkOwnership(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        checkOwnership(project);
    }

    private void checkOwnership(Project project) {
        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
            if (!project.getOwner().getLogin().equals(login)) {
                throw new RuntimeException("Access denied: you do not own this project");
            }
        }
    }
}
