package com.gestiontaches.web.rest;

import com.gestiontaches.repository.ActionHistoryRepository;
import com.gestiontaches.service.ActionHistoryService;
import com.gestiontaches.service.dto.ActionHistoryDTO;
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
 * REST controller for managing {@link com.gestiontaches.domain.ActionHistory}.
 */
@RestController
@RequestMapping("/api/action-histories")
public class ActionHistoryResource {

    private static final Logger LOG = LoggerFactory.getLogger(ActionHistoryResource.class);

    private static final String ENTITY_NAME = "actionHistory";

    @Value("${jhipster.clientApp.name:gestionTaches}")
    private String applicationName;

    private final ActionHistoryService actionHistoryService;

    private final ActionHistoryRepository actionHistoryRepository;

    public ActionHistoryResource(ActionHistoryService actionHistoryService, ActionHistoryRepository actionHistoryRepository) {
        this.actionHistoryService = actionHistoryService;
        this.actionHistoryRepository = actionHistoryRepository;
    }

    /**
     * {@code POST  /action-histories} : Create a new actionHistory.
     *
     * @param actionHistoryDTO the actionHistoryDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new actionHistoryDTO, or with status {@code 400 (Bad Request)} if the actionHistory has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ActionHistoryDTO> createActionHistory(@Valid @RequestBody ActionHistoryDTO actionHistoryDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save ActionHistory : {}", actionHistoryDTO);
        if (actionHistoryDTO.getId() != null) {
            throw new BadRequestAlertException("A new actionHistory cannot already have an ID", ENTITY_NAME, "idexists");
        }
        actionHistoryDTO = actionHistoryService.save(actionHistoryDTO);
        return ResponseEntity.created(new URI("/api/action-histories/" + actionHistoryDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, actionHistoryDTO.getId().toString()))
            .body(actionHistoryDTO);
    }

    /**
     * {@code PUT  /action-histories/:id} : Updates an existing actionHistory.
     *
     * @param id the id of the actionHistoryDTO to save.
     * @param actionHistoryDTO the actionHistoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated actionHistoryDTO,
     * or with status {@code 400 (Bad Request)} if the actionHistoryDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the actionHistoryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ActionHistoryDTO> updateActionHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ActionHistoryDTO actionHistoryDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update ActionHistory : {}, {}", id, actionHistoryDTO);
        if (actionHistoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, actionHistoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!actionHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        actionHistoryDTO = actionHistoryService.update(actionHistoryDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, actionHistoryDTO.getId().toString()))
            .body(actionHistoryDTO);
    }

    /**
     * {@code PATCH  /action-histories/:id} : Partial updates given fields of an existing actionHistory, field will ignore if it is null
     *
     * @param id the id of the actionHistoryDTO to save.
     * @param actionHistoryDTO the actionHistoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated actionHistoryDTO,
     * or with status {@code 400 (Bad Request)} if the actionHistoryDTO is not valid,
     * or with status {@code 404 (Not Found)} if the actionHistoryDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the actionHistoryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ActionHistoryDTO> partialUpdateActionHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ActionHistoryDTO actionHistoryDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ActionHistory partially : {}, {}", id, actionHistoryDTO);
        if (actionHistoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, actionHistoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!actionHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ActionHistoryDTO> result = actionHistoryService.partialUpdate(actionHistoryDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, actionHistoryDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /action-histories} : get all the Action Histories.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Action Histories in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ActionHistoryDTO>> getAllActionHistories(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of ActionHistories");
        Page<ActionHistoryDTO> page = actionHistoryService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /action-histories/:id} : get the "id" actionHistory.
     *
     * @param id the id of the actionHistoryDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the actionHistoryDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ActionHistoryDTO> getActionHistory(@PathVariable("id") Long id) {
        LOG.debug("REST request to get ActionHistory : {}", id);
        Optional<ActionHistoryDTO> actionHistoryDTO = actionHistoryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(actionHistoryDTO);
    }

    /**
     * {@code DELETE  /action-histories/:id} : delete the "id" actionHistory.
     *
     * @param id the id of the actionHistoryDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActionHistory(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete ActionHistory : {}", id);
        actionHistoryService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
