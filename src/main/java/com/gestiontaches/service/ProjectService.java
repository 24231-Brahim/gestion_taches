package com.gestiontaches.service;

import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.domain.enumeration.SprintStatus;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.repository.SprintRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.ProjectCardStatsDTO;
import com.gestiontaches.service.dto.ProjectDTO;
import com.gestiontaches.service.dto.ProjectMemberDTO;
import com.gestiontaches.service.mapper.ProjectMapper;
import com.gestiontaches.service.mapper.ProjectMemberMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final TaskRepository taskRepository;

    private final SprintRepository sprintRepository;

    private final NotificationService notificationService;

    public ProjectService(
        ProjectRepository projectRepository,
        ProjectMapper projectMapper,
        UserRepository userRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectMemberMapper projectMemberMapper,
        ProjectPermissionService projectPermissionService,
        TaskRepository taskRepository,
        SprintRepository sprintRepository,
        NotificationService notificationService
    ) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectMemberMapper = projectMemberMapper;
        this.projectPermissionService = projectPermissionService;
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
        this.notificationService = notificationService;
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
     * @param mineOnly when true, always scope to the current user's own/member projects even if
     *                 they hold ADMIN/PROJET_MANAGER authority (the "Mes projets" toggle).
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ProjectDTO> findAll(Pageable pageable, boolean mineOnly) {
        LOG.debug("Request to get all Projects");
        if (!mineOnly && projectPermissionService.hasGlobalProjectAccess()) {
            return projectRepository.findAll(pageable).map(projectMapper::toDto);
        }
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        return projectRepository.findByOwnerLoginOrMemberLogin(login, pageable).map(projectMapper::toDto);
    }

    /**
     * Get task-progress and active-sprint stats for every project visible to the current user,
     * for rendering the Projects card grid without one round-trip per card.
     *
     * @return the list of per-project stats.
     */
    @Transactional(readOnly = true)
    public List<ProjectCardStatsDTO> getProjectCardStats() {
        List<Project> visibleProjects;
        if (projectPermissionService.hasGlobalProjectAccess()) {
            visibleProjects = projectRepository.findAll();
        } else {
            String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
            visibleProjects = projectRepository.findByOwnerLoginOrMemberLogin(login, Pageable.unpaged()).getContent();
        }

        Map<Long, long[]> taskCountsByProjectId = new HashMap<>();
        for (Object[] row : taskRepository.countTasksGroupByProject()) {
            Long projectId = ((Number) row[0]).longValue();
            long total = ((Number) row[2]).longValue();
            long done = ((Number) row[3]).longValue();
            taskCountsByProjectId.put(projectId, new long[] { total, done });
        }

        return visibleProjects
            .stream()
            .map(project -> {
                ProjectCardStatsDTO dto = new ProjectCardStatsDTO();
                dto.setProjectId(project.getId());
                long[] counts = taskCountsByProjectId.getOrDefault(project.getId(), new long[] { 0, 0 });
                dto.setTotalTasks(counts[0]);
                dto.setDoneTasks(counts[1]);
                sprintRepository.findFirstByProjectIdAndStatusOrderByIdDesc(project.getId(), SprintStatus.ACTIVE).ifPresent(sprint -> {
                    dto.setActiveSprintId(sprint.getId());
                    dto.setActiveSprintName(sprint.getName());
                });
                return dto;
            })
            .toList();
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
        if (projectPermissionService.hasGlobalProjectAccess()) {
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
        Project project = projectRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        List<ProjectMember> members = projectMemberRepository.findByProjectId(id);
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);

        String message = "Le projet " + project.getName() + " a été supprimé";
        for (ProjectMember member : members) {
            User recipient = member.getUser();
            if (currentLogin == null || !currentLogin.equals(recipient.getLogin())) {
                notificationService.createNotification(recipient, message, "Projet: " + project.getName(), null);
            }
        }

        projectMemberRepository.deleteAll(members);
        projectRepository.delete(project);
    }

    public Set<ProjectMemberDTO> getMembers(Long projectId) {
        LOG.debug("Request to get members of Project : {}", projectId);
        // Viewing the member list is a read operation available to any project member (any role),
        // not just OWNER/MANAGER — those stricter roles are still required to add/remove/re-role
        // members (see addMember/removeMember/updateMemberRole below).
        projectPermissionService.requireProjectAccess(projectId);
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

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null || !currentLogin.equals(user.getLogin())) {
            notificationService.createNotification(
                user,
                "Vous avez été ajouté au projet " + project.getName(),
                "Projet: " + project.getName(),
                null
            );
        }
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

        if (projectPermissionService.hasGlobalProjectAccess()) {
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
