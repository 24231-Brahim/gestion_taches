package com.gestiontaches.web.rest;

import com.gestiontaches.repository.SprintRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.SprintQueryService;
import com.gestiontaches.service.SprintService;
import com.gestiontaches.service.criteria.SprintCriteria;
import com.gestiontaches.service.dto.SprintDTO;
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

/**
 * REST controller for managing {@link com.gestiontaches.domain.Sprint}.
 */
@RestController
@RequestMapping("/api/sprints")
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

    /**
     * {@code POST  /sprints} : Create a new sprint.
     *
     * @param sprintDTO the sprintDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new sprintDTO, or with status {@code 400 (Bad Request)} if the sprint has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
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

    /**
     * {@code PUT  /sprints/:id} : Updates an existing sprint.
     *
     * @param id the id of the sprintDTO to save.
     * @param sprintDTO the sprintDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sprintDTO,
     * or with status {@code 400 (Bad Request)} if the sprintDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the sprintDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
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

    /**
     * {@code PATCH  /sprints/:id} : Partial updates given fields of an existing sprint, field will ignore if it is null
     *
     * @param id the id of the sprintDTO to save.
     * @param sprintDTO the sprintDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sprintDTO,
     * or with status {@code 400 (Bad Request)} if the sprintDTO is not valid,
     * or with status {@code 404 (Not Found)} if the sprintDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the sprintDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
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

    /**
     * {@code GET  /sprints} : get all the Sprints.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Sprints in body.
     */
    @GetMapping("")
    public ResponseEntity<List<SprintDTO>> getAllSprints(
        SprintCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Sprints by criteria: {}", criteria);

        Page<SprintDTO> page = sprintQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /sprints/count} : count all the sprints.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countSprints(SprintCriteria criteria) {
        LOG.debug("REST request to count Sprints by criteria: {}", criteria);
        return ResponseEntity.ok().body(sprintQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /sprints/:id} : get the "id" sprint.
     *
     * @param id the id of the sprintDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the sprintDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SprintDTO> getSprint(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Sprint : {}", id);
        Optional<SprintDTO> sprintDTO = sprintService.findOne(id);
        return ResponseUtil.wrapOrNotFound(sprintDTO);
    }

    /**
     * {@code DELETE  /sprints/:id} : delete the "id" sprint.
     *
     * @param id the id of the sprintDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
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
}
