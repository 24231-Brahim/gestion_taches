package com.gestiontaches.web.rest;

import com.gestiontaches.repository.SprintRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.SprintQueryService;
import com.gestiontaches.service.SprintService;
import com.gestiontaches.service.criteria.SprintCriteria;
import com.gestiontaches.service.dto.SprintDTO;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.dto.VelocityReportDTO;
import com.gestiontaches.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api")
public class SprintResource {

    private static final Logger LOG = LoggerFactory.getLogger(SprintResource.class);

    private static final String ENTITY_NAME = "sprint";

    @Value("${jhipster.clientApp.name:gestionTaches}")
    private String applicationName;

    private final SprintService sprintService;

    private final SprintRepository sprintRepository;

    private final SprintQueryService sprintQueryService;

    public SprintResource(SprintService sprintService, SprintRepository sprintRepository, SprintQueryService sprintQueryService) {
        this.sprintService = sprintService;
        this.sprintRepository = sprintRepository;
        this.sprintQueryService = sprintQueryService;
    }

    @PostMapping("/sprints")
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.USER +
            "')"
    )
    public ResponseEntity<SprintDTO> createSprint(@Valid @RequestBody SprintDTO sprintDTO) throws URISyntaxException {
        LOG.debug("REST request to save Sprint : {}", sprintDTO);
        if (sprintDTO.getId() != null) {
            throw new BadRequestAlertException("A new sprint cannot already have an ID", ENTITY_NAME, "idexists");
        }
        sprintDTO = sprintService.save(sprintDTO);
        return ResponseEntity.created(new URI("/api/sprints/" + sprintDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, sprintDTO.getId().toString()))
            .body(sprintDTO);
    }

    @PutMapping("/sprints/{id}")
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.USER +
            "')"
    )
    public ResponseEntity<SprintDTO> updateSprint(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SprintDTO sprintDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Sprint : {}, {}", id, sprintDTO);
        if (sprintDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sprintDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!sprintRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        sprintDTO = sprintService.update(sprintDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, sprintDTO.getId().toString()))
            .body(sprintDTO);
    }

    @PatchMapping(value = "/sprints/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.USER +
            "')"
    )
    public ResponseEntity<SprintDTO> partialUpdateSprint(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SprintDTO sprintDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Sprint partially : {}, {}", id, sprintDTO);
        if (sprintDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sprintDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!sprintRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        Optional<SprintDTO> result = sprintService.partialUpdate(sprintDTO);
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, sprintDTO.getId().toString())
        );
    }

    @GetMapping("/sprints")
    public ResponseEntity<List<SprintDTO>> getAllSprints(
        SprintCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Sprints by criteria: {}", criteria);
        Page<SprintDTO> page = sprintQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/sprints/count")
    public ResponseEntity<Long> countSprints(SprintCriteria criteria) {
        LOG.debug("REST request to count Sprints by criteria: {}", criteria);
        return ResponseEntity.ok().body(sprintQueryService.countByCriteria(criteria));
    }

    @GetMapping("/sprints/{id}")
    public ResponseEntity<SprintDTO> getSprint(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Sprint : {}", id);
        Optional<SprintDTO> sprintDTO = sprintService.findOne(id);
        return ResponseUtil.wrapOrNotFound(sprintDTO);
    }

    @DeleteMapping("/sprints/{id}")
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.USER +
            "')"
    )
    public ResponseEntity<Void> deleteSprint(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Sprint : {}", id);
        sprintService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @PostMapping("/sprints/{id}/start")
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.USER +
            "')"
    )
    public ResponseEntity<SprintDTO> startSprint(@PathVariable("id") Long id) {
        LOG.debug("REST request to start Sprint : {}", id);
        SprintDTO sprintDTO = sprintService.startSprint(id);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, sprintDTO.getId().toString()))
            .body(sprintDTO);
    }

    @PostMapping("/sprints/{id}/close")
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.USER +
            "')"
    )
    public ResponseEntity<VelocityReportDTO> closeSprint(@PathVariable("id") Long id) {
        LOG.debug("REST request to close Sprint : {}", id);
        VelocityReportDTO report = sprintService.closeSprint(id);
        return ResponseEntity.ok().body(report);
    }

    @GetMapping("/projects/{projectId}/backlog")
    public ResponseEntity<List<TaskDTO>> getBacklog(@PathVariable("projectId") Long projectId) {
        LOG.debug("REST request to get backlog for project : {}", projectId);
        List<TaskDTO> tasks = sprintService.getBacklogTasks(projectId);
        return ResponseEntity.ok().body(tasks);
    }
}
