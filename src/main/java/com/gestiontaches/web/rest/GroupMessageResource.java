package com.gestiontaches.web.rest;

import com.gestiontaches.domain.User;
import com.gestiontaches.repository.ProjectMemberRepository;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.GroupMessageService;
import com.gestiontaches.service.UserService;
import com.gestiontaches.service.dto.GroupMessageDTO;
import com.gestiontaches.service.dto.ProjectDTO;
import com.gestiontaches.service.dto.UserDTO;
import com.gestiontaches.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing {@link com.gestiontaches.domain.GroupMessage}.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/group-messages")
public class GroupMessageResource {

    private static final Logger LOG = LoggerFactory.getLogger(GroupMessageResource.class);

    private static final String ENTITY_NAME = "groupMessage";

    @Value("${jhipster.clientApp.name:gestionTaches}")
    private String applicationName;

    private final GroupMessageService groupMessageService;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserService userService;

    public GroupMessageResource(
        GroupMessageService groupMessageService,
        ProjectMemberRepository projectMemberRepository,
        UserService userService
    ) {
        this.groupMessageService = groupMessageService;
        this.projectMemberRepository = projectMemberRepository;
        this.userService = userService;
    }

    /**
     * {@code POST  /projects/{projectId}/group-messages} : Create a new groupMessage.
     */
    @PostMapping("")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<GroupMessageDTO> createGroupMessage(
        @PathVariable("projectId") Long projectId,
        @Valid @RequestBody GroupMessageDTO groupMessageDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to save GroupMessage for project : {}", projectId);
        if (groupMessageDTO.getId() != null) {
            throw new BadRequestAlertException("A new groupMessage cannot already have an ID", ENTITY_NAME, "idexists");
        }

        User currentUser = userService
            .getUserWithAuthoritiesByLogin(
                SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
                    new BadRequestAlertException("User not found", ENTITY_NAME, "usernotfound")
                )
            )
            .orElseThrow(() -> new BadRequestAlertException("User not found", ENTITY_NAME, "usernotfound"));

        projectMemberRepository
            .findByProjectIdAndUserId(projectId, currentUser.getId())
            .orElseThrow(() -> new BadRequestAlertException("You are not a member of this project", ENTITY_NAME, "notprojectmember"));

        groupMessageDTO.setSender(new UserDTO(currentUser));
        groupMessageDTO.setProject(new ProjectDTO());
        groupMessageDTO.getProject().setId(projectId);
        if (groupMessageDTO.getCreatedAt() == null) {
            groupMessageDTO.setCreatedAt(Instant.now());
        }

        groupMessageDTO = groupMessageService.save(groupMessageDTO);
        return ResponseEntity.created(new URI("/api/projects/" + projectId + "/group-messages/" + groupMessageDTO.getId())).body(
            groupMessageDTO
        );
    }

    /**
     * {@code GET  /projects/{projectId}/group-messages} : get visible groupMessages.
     */
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<List<GroupMessageDTO>> getGroupMessages(@PathVariable("projectId") Long projectId) {
        LOG.debug("REST request to get GroupMessages for project : {}", projectId);

        User currentUser = userService
            .getUserWithAuthoritiesByLogin(
                SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
                    new BadRequestAlertException("User not found", ENTITY_NAME, "usernotfound")
                )
            )
            .orElseThrow(() -> new BadRequestAlertException("User not found", ENTITY_NAME, "usernotfound"));

        projectMemberRepository
            .findByProjectIdAndUserId(projectId, currentUser.getId())
            .orElseThrow(() -> new BadRequestAlertException("You are not a member of this project", ENTITY_NAME, "notprojectmember"));

        List<GroupMessageDTO> messages = groupMessageService.findVisibleMessages(projectId, currentUser.getId());
        return ResponseEntity.ok(messages);
    }
}
