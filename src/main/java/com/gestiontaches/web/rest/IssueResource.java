package com.gestiontaches.web.rest;

import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.IssueRepository;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.IssueQueryService;
import com.gestiontaches.service.IssueService;
import com.gestiontaches.service.NotificationService;
import com.gestiontaches.service.UserService;
import com.gestiontaches.service.criteria.IssueCriteria;
import com.gestiontaches.service.dto.IssueDTO;
import com.gestiontaches.service.dto.NotificationDTO;
import com.gestiontaches.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.gestiontaches.domain.Issue}.
 */
@RestController
@RequestMapping("/api/issues")
public class IssueResource {

    private static final Logger LOG = LoggerFactory.getLogger(IssueResource.class);

    private static final String ENTITY_NAME = "issue";

    @Value("${jhipster.clientApp.name:gestionTaches}")
    private String applicationName;

    private final IssueService issueService;

    private final IssueRepository issueRepository;

    private final IssueQueryService issueQueryService;

    private final UserService userService;

    private final NotificationService notificationService;

    private final UserRepository userRepository;

    private final ProjectRepository projectRepository;

    public IssueResource(
        IssueService issueService,
        IssueRepository issueRepository,
        IssueQueryService issueQueryService,
        UserService userService,
        NotificationService notificationService,
        UserRepository userRepository,
        ProjectRepository projectRepository
    ) {
        this.issueService = issueService;
        this.issueRepository = issueRepository;
        this.issueQueryService = issueQueryService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * {@code POST  /projects/{projectId}/issues} : Create a new issue for a specific project.
     * Only the project owner (or ADMIN) can create issues.
     *
     * @param projectId the id of the project.
     * @param issueDTO the issueDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new issueDTO,
     *         or {@code 403 (Forbidden)} if the user is not the owner, or {@code 400 (Bad Request)} if the issue has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/projects/{projectId}/issues")
    public ResponseEntity<IssueDTO> createIssueForProject(@PathVariable("projectId") Long projectId, @Valid @RequestBody IssueDTO issueDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save Issue for Project {} : {}", projectId, issueDTO);
        if (issueDTO.getId() != null) {
            throw new BadRequestAlertException("A new issue cannot already have an ID", ENTITY_NAME, "idexists");
        }

        Project project = projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BadRequestAlertException("Project not found", "project", "idnotfound"));

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        if (!isAdmin && !project.getOwner().getLogin().equals(currentLogin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        issueDTO.setProject(new com.gestiontaches.service.dto.ProjectDTO());
        issueDTO.getProject().setId(projectId);
        issueDTO.getProject().setName(project.getName());

        issueDTO = issueService.createForProject(issueDTO, projectId);
        return ResponseEntity.created(new URI("/api/issues/" + issueDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, issueDTO.getId().toString()))
            .body(issueDTO);
    }

    /**
     * {@code POST  /issues} : Create a new issue.
     *
     * @param issueDTO the issueDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new issueDTO, or with status {@code 400 (Bad Request)} if the issue has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.DEVELOPER +
            "')"
    )
    public ResponseEntity<IssueDTO> createIssue(@Valid @RequestBody IssueDTO issueDTO) throws URISyntaxException {
        LOG.debug("REST request to save Issue : {}", issueDTO);
        if (issueDTO.getId() != null) {
            throw new BadRequestAlertException("A new issue cannot already have an ID", ENTITY_NAME, "idexists");
        }
        issueDTO = issueService.save(issueDTO);
        return ResponseEntity.created(new URI("/api/issues/" + issueDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, issueDTO.getId().toString()))
            .body(issueDTO);
    }

    /**
     * {@code PUT  /issues/:id} : Updates an existing issue.
     *
     * @param id the id of the issueDTO to save.
     * @param issueDTO the issueDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated issueDTO,
     * or with status {@code 400 (Bad Request)} if the issueDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the issueDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.DEVELOPER +
            "')"
    )
    public ResponseEntity<IssueDTO> updateIssue(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody IssueDTO issueDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Issue : {}, {}", id, issueDTO);
        if (issueDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, issueDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!issueRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        issueDTO = issueService.update(issueDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, issueDTO.getId().toString()))
            .body(issueDTO);
    }

    /**
     * {@code PATCH  /issues/:id} : Partial updates given fields of an existing issue, field will ignore if it is null
     *
     * @param id the id of the issueDTO to save.
     * @param issueDTO the issueDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated issueDTO,
     * or with status {@code 400 (Bad Request)} if the issueDTO is not valid,
     * or with status {@code 404 (Not Found)} if the issueDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the issueDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.DEVELOPER +
            "')"
    )
    public ResponseEntity<IssueDTO> partialUpdateIssue(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody IssueDTO issueDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Issue partially : {}, {}", id, issueDTO);
        if (issueDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, issueDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!issueRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<IssueDTO> result = issueService.partialUpdate(issueDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, issueDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /issues} : get all the Issues.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Issues in body.
     */
    @GetMapping("")
    public ResponseEntity<List<IssueDTO>> getAllIssues(
        IssueCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Issues by criteria: {}", criteria);

        Page<IssueDTO> page = issueQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /issues/count} : count all the issues.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countIssues(IssueCriteria criteria) {
        LOG.debug("REST request to count Issues by criteria: {}", criteria);
        return ResponseEntity.ok().body(issueQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /issues/:id} : get the "id" issue.
     *
     * @param id the id of the issueDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the issueDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<IssueDTO> getIssue(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Issue : {}", id);
        Optional<IssueDTO> issueDTO = issueService.findOne(id);
        return ResponseUtil.wrapOrNotFound(issueDTO);
    }

    /**
     * {@code PATCH  /issues/:id/assign} : Assign a user to an issue.
     *
     * @param id the id of the issue to assign.
     * @param body the request body containing userId.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated issueDTO.
     */
    @PatchMapping("/{id}/assign")
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.DEVELOPER +
            "')"
    )
    public ResponseEntity<IssueDTO> assignIssue(@PathVariable("id") Long id, @RequestBody Map<String, Long> body) {
        LOG.debug("REST request to assign user to Issue : {}", id);
        Long userId = body.get("userId");
        if (userId == null) {
            throw new BadRequestAlertException("userId is required", ENTITY_NAME, "userIdrequired");
        }
        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new BadRequestAlertException("User not found", ENTITY_NAME, "usernotfound"));

        boolean isAssignable = userService
            .getUserWithAuthoritiesByLogin(user.getLogin())
            .map(u ->
                u
                    .getAuthorities()
                    .stream()
                    .anyMatch(
                        a -> AuthoritiesConstants.DEVELOPER.equals(a.getName()) || AuthoritiesConstants.PROJET_MANAGER.equals(a.getName())
                    )
            )
            .orElse(false);
        if (!isAssignable) {
            throw new BadRequestAlertException("User must have DEVELOPER or PROJET_MANAGER role", ENTITY_NAME, "invalidrole");
        }

        IssueDTO result = issueService.assign(id, user);

        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse("System");
        NotificationDTO notification = new NotificationDTO();
        notification.setMessage(currentLogin + " vous a assigné à l'issue #" + id + " : " + result.getTitle());
        notification.setIssueId(id);
        notification.setIssueTitle(result.getTitle());
        notification.setUserId(userId);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());
        notificationService.save(notification);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .body(result);
    }

    /**
     * {@code DELETE  /issues/:id} : delete the "id" issue.
     *
     * @param id the id of the issueDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.DEVELOPER +
            "')"
    )
    public ResponseEntity<Void> deleteIssue(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Issue : {}", id);
        issueService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
