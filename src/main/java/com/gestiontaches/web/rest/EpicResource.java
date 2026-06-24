package com.gestiontaches.web.rest;

import com.gestiontaches.repository.EpicRepository;
import com.gestiontaches.service.EpicQueryService;
import com.gestiontaches.service.EpicService;
import com.gestiontaches.service.criteria.EpicCriteria;
import com.gestiontaches.service.dto.EpicDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.gestiontaches.domain.Epic}.
 */
@RestController
@RequestMapping("/api/epics")
public class EpicResource {

    private static final Logger LOG = LoggerFactory.getLogger(EpicResource.class);

    private static final String ENTITY_NAME = "epic";

    @Value("${jhipster.clientApp.name:gestionTaches}")
    private String applicationName;

    private final EpicService epicService;

    private final EpicRepository epicRepository;

    private final EpicQueryService epicQueryService;

    public EpicResource(EpicService epicService, EpicRepository epicRepository, EpicQueryService epicQueryService) {
        this.epicService = epicService;
        this.epicRepository = epicRepository;
        this.epicQueryService = epicQueryService;
    }

    /**
     * {@code POST  /epics} : Create a new epic.
     *
     * @param epicDTO the epicDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new epicDTO, or with status {@code 400 (Bad Request)} if the epic has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<EpicDTO> createEpic(@Valid @RequestBody EpicDTO epicDTO) throws URISyntaxException {
        LOG.debug("REST request to save Epic : {}", epicDTO);
        if (epicDTO.getId() != null) {
            throw new BadRequestAlertException("A new epic cannot already have an ID", ENTITY_NAME, "idexists");
        }
        epicDTO = epicService.save(epicDTO);
        return ResponseEntity.created(new URI("/api/epics/" + epicDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, epicDTO.getId().toString()))
            .body(epicDTO);
    }

    /**
     * {@code PUT  /epics/:id} : Updates an existing epic.
     *
     * @param id the id of the epicDTO to save.
     * @param epicDTO the epicDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated epicDTO,
     * or with status {@code 400 (Bad Request)} if the epicDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the epicDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EpicDTO> updateEpic(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EpicDTO epicDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Epic : {}, {}", id, epicDTO);
        if (epicDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, epicDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!epicRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        epicDTO = epicService.update(epicDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, epicDTO.getId().toString()))
            .body(epicDTO);
    }

    /**
     * {@code PATCH  /epics/:id} : Partial updates given fields of an existing epic, field will ignore if it is null
     *
     * @param id the id of the epicDTO to save.
     * @param epicDTO the epicDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated epicDTO,
     * or with status {@code 400 (Bad Request)} if the epicDTO is not valid,
     * or with status {@code 404 (Not Found)} if the epicDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the epicDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EpicDTO> partialUpdateEpic(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EpicDTO epicDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Epic partially : {}, {}", id, epicDTO);
        if (epicDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, epicDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!epicRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EpicDTO> result = epicService.partialUpdate(epicDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, epicDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /epics} : get all the Epics.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Epics in body.
     */
    @GetMapping("")
    public ResponseEntity<List<EpicDTO>> getAllEpics(
        EpicCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Epics by criteria: {}", criteria);

        Page<EpicDTO> page = epicQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /epics/count} : count all the epics.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countEpics(EpicCriteria criteria) {
        LOG.debug("REST request to count Epics by criteria: {}", criteria);
        return ResponseEntity.ok().body(epicQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /epics/:id} : get the "id" epic.
     *
     * @param id the id of the epicDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the epicDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EpicDTO> getEpic(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Epic : {}", id);
        Optional<EpicDTO> epicDTO = epicService.findOne(id);
        return ResponseUtil.wrapOrNotFound(epicDTO);
    }

    /**
     * {@code DELETE  /epics/:id} : delete the "id" epic.
     *
     * @param id the id of the epicDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEpic(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Epic : {}", id);
        epicService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
