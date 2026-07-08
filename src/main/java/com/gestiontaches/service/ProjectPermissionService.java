package com.gestiontaches.service;

import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.security.SecurityUtils;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProjectPermissionService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectPermissionService.class);

    private final ProjectMemberRepository projectMemberRepository;

    private final UserRepository userRepository;

    public ProjectPermissionService(ProjectMemberRepository projectMemberRepository, UserRepository userRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    public ProjectMember getProjectMember(Long projectId) {
        Long userId = resolveCurrentUserId();
        return projectMemberRepository
            .findByProjectIdAndUserId(projectId, userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project"));
    }

    private Long resolveCurrentUserId() {
        return SecurityUtils.getCurrentUserId().orElseGet(() -> {
            String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user not found")
            );
            return userRepository
                .findOneByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Current user not found"))
                .getId();
        });
    }

    public ProjectRole getCurrentUserRole(Long projectId) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return ProjectRole.OWNER;
        }
        return getProjectMember(projectId).getRole();
    }

    public void requireProjectRole(Long projectId, ProjectRole... roles) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return;
        }
        ProjectRole currentRole = getCurrentUserRole(projectId);
        boolean matches = Arrays.stream(roles).anyMatch(r -> r == currentRole);
        if (!matches) {
            LOG.warn("Access denied: user has role {} but required roles are {}", currentRole, Arrays.toString(roles));
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Access denied: you need one of " + Arrays.toString(roles) + " roles for this action"
            );
        }
    }

    public void requireProjectRoleOrOwner(Long projectId, Long ownerUserId, ProjectRole... roles) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return;
        }
        Long currentUserId = resolveCurrentUserId();
        if (currentUserId.equals(ownerUserId)) {
            return;
        }
        requireProjectRole(projectId, roles);
    }
}
