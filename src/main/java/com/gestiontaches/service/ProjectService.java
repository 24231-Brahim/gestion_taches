package com.gestiontaches.service;

import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.ProjectRole;
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

    private final ProjectPermissionService projectPermissionService;

    public ProjectService(
        ProjectRepository projectRepository,
        ProjectMapper projectMapper,
        UserRepository userRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectMemberMapper projectMemberMapper,
        ProjectPermissionService projectPermissionService
    ) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectMemberMapper = projectMemberMapper;
        this.projectPermissionService = projectPermissionService;
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
        ProjectMember member = new ProjectMember().project(project).user(owner).role(ProjectRole.OWNER).joinedAt(Instant.now());
        projectMemberRepository.save(member);
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
        projectPermissionService.requireProjectRole(projectDTO.getId(), ProjectRole.OWNER, ProjectRole.MANAGER);
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
        projectPermissionService.requireProjectRole(projectDTO.getId(), ProjectRole.OWNER, ProjectRole.MANAGER);

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
        return projectRepository.findByOwnerLoginOrMemberLogin(login, pageable).map(projectMapper::toDto);
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
        return projectRepository.findByIdAndOwnerLoginOrMemberLogin(id, login).map(projectMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<ProjectDTO> findOneByKey(String key) {
        LOG.debug("Request to get Project by key : {}", key);
        return projectRepository.findByKey(key).map(projectMapper::toDto);
    }

    /**
     * Delete the project by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Project : {}", id);
        projectPermissionService.requireProjectRole(id, ProjectRole.OWNER);
        projectRepository.deleteById(id);
    }

    public Set<ProjectMemberDTO> getMembers(Long projectId) {
        LOG.debug("Request to get members of Project : {}", projectId);
        projectPermissionService.requireProjectRole(projectId, ProjectRole.OWNER, ProjectRole.MANAGER);
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        return members.stream().map(projectMemberMapper::toDto).collect(Collectors.toSet());
    }

    public void addMember(Long projectId, Long userId) {
        LOG.debug("Request to add user {} to Project {}", userId, projectId);
        projectPermissionService.requireProjectRole(projectId, ProjectRole.OWNER, ProjectRole.MANAGER);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (projectMemberRepository.findByProjectIdAndUserId(projectId, userId).isPresent()) {
            throw new RuntimeException("User is already a member of this project");
        }
        ProjectMember member = new ProjectMember().project(project).user(user).role(ProjectRole.MEMBER).joinedAt(Instant.now());
        projectMemberRepository.save(member);
    }

    public void removeMember(Long projectId, Long userId) {
        LOG.debug("Request to remove user {} from Project {}", userId, projectId);
        projectPermissionService.requireProjectRole(projectId, ProjectRole.OWNER, ProjectRole.MANAGER);
        ProjectMember member = projectMemberRepository
            .findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        ProjectRole currentRole = projectPermissionService.getCurrentUserRole(projectId);
        if (currentRole == ProjectRole.MANAGER && member.getRole() == ProjectRole.OWNER) {
            throw new RuntimeException("Access denied: managers cannot remove the project owner");
        }
        if (member.getRole() == ProjectRole.OWNER) {
            long ownerCount = projectMemberRepository.countByProjectIdAndRole(projectId, ProjectRole.OWNER);
            if (ownerCount <= 1) {
                throw new RuntimeException("Cannot remove the last owner of the project");
            }
        }
        projectMemberRepository.delete(member);
    }

    public void updateMemberRole(Long projectId, Long userId, ProjectMemberDTO memberDTO) {
        LOG.debug("Request to update role of user {} in Project {}", userId, projectId);
        projectPermissionService.requireProjectRole(projectId, ProjectRole.OWNER);
        ProjectMember member = projectMemberRepository
            .findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        if (member.getRole() == ProjectRole.OWNER && memberDTO.getRole() != ProjectRole.OWNER) {
            long ownerCount = projectMemberRepository.countByProjectIdAndRole(projectId, ProjectRole.OWNER);
            if (ownerCount <= 1) {
                throw new RuntimeException("Cannot change role of the last owner of the project");
            }
        }
        member.setRole(memberDTO.getRole());
        projectMemberRepository.save(member);
    }

    public long getTotalMemberCount() {
        LOG.debug("Request to get total distinct member count");
        return projectMemberRepository.countDistinctUsers();
    }

    @Transactional(readOnly = true)
    public Set<ProjectMemberDTO> getCurrentUserMemberships() {
        LOG.debug("Request to get current user memberships");
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        User user = userRepository.findOneByLogin(login).orElseThrow(() -> new RuntimeException("User not found"));
        List<ProjectMember> members = projectMemberRepository.findByUserId(user.getId());
        Set<ProjectMemberDTO> memberships = members.stream().map(projectMemberMapper::toDto).collect(Collectors.toSet());

        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            Set<Long> memberProjectIds = memberships.stream().map(ProjectMemberDTO::getProjectId).collect(Collectors.toSet());
            List<Project> allProjects = projectRepository.findAll();
            for (Project project : allProjects) {
                if (!memberProjectIds.contains(project.getId())) {
                    ProjectMemberDTO synthetic = new ProjectMemberDTO();
                    synthetic.setProjectId(project.getId());
                    synthetic.setUserId(user.getId());
                    synthetic.setUserLogin(user.getLogin());
                    synthetic.setRole(ProjectRole.OWNER);
                    synthetic.setJoinedAt(Instant.now());
                    memberships.add(synthetic);
                }
            }
        }

        return memberships;
    }
}
