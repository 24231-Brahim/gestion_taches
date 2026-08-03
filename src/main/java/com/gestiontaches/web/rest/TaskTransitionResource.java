package com.gestiontaches.web.rest;

import com.gestiontaches.repository.TaskTransitionRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.TaskTransitionService;
import com.gestiontaches.service.dto.TaskTransitionDTO;
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
 * REST controller for managing {@link com.gestiontaches.domain.TaskTransition}.
 */
@RestController
@RequestMapping("/api/task-transitions")
public class TaskTransitionResource {

    private static final Logger LOG = LoggerFactory.getLogger(TaskTransitionResource.class);

    private static final String ENTITY_NAME = "taskTransition";

    @Value("${jhipster.clientApp.name:gestionTaches}")
    private String applicationName;

    private final TaskTransitionService taskTransitionService;

    private final TaskTransitionRepository taskTransitionRepository;

    public TaskTransitionResource(TaskTransitionService taskTransitionService, TaskTransitionRepository taskTransitionRepository) {
        this.taskTransitionService = taskTransitionService;
        this.taskTransitionRepository = taskTransitionRepository;
    }

    /**
     * {@code POST  /task-transitions} : Create a new taskTransition.
     *
     * @param taskTransitionDTO the taskTransitionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new taskTransitionDTO, or with status {@code 400 (Bad Request)} if the taskTransition has already an ID.
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
    public ResponseEntity<TaskTransitionDTO> createTaskTransition(@Valid @RequestBody TaskTransitionDTO taskTransitionDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save TaskTransition : {}", taskTransitionDTO);
        if (taskTransitionDTO.getId() != null) {
            throw new BadRequestAlertException("A new taskTransition cannot already have an ID", ENTITY_NAME, "idexists");
        }
        taskTransitionDTO = taskTransitionService.save(taskTransitionDTO);
        return ResponseEntity.created(new URI("/api/task-transitions/" + taskTransitionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, taskTransitionDTO.getId().toString()))
            .body(taskTransitionDTO);
    }

    /**
     * {@code PUT  /task-transitions/:id} : Updates an existing taskTransition.
     *
     * @param id the id of the taskTransitionDTO to save.
     * @param taskTransitionDTO the taskTransitionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated taskTransitionDTO,
     * or with status {@code 400 (Bad Request)} if the taskTransitionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the taskTransitionDTO couldn't be updated.
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
    public ResponseEntity<TaskTransitionDTO> updateTaskTransition(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TaskTransitionDTO taskTransitionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TaskTransition : {}, {}", id, taskTransitionDTO);
        if (taskTransitionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taskTransitionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!taskTransitionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        taskTransitionDTO = taskTransitionService.update(taskTransitionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, taskTransitionDTO.getId().toString()))
            .body(taskTransitionDTO);
    }

    /**
     * {@code PATCH  /task-transitions/:id} : Partial updates given fields of an existing taskTransition, field will ignore if it is null
     *
     * @param id the id of the taskTransitionDTO to save.
     * @param taskTransitionDTO the taskTransitionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated taskTransitionDTO,
     * or with status {@code 400 (Bad Request)} if the taskTransitionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the taskTransitionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the taskTransitionDTO couldn't be updated.
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
    public ResponseEntity<TaskTransitionDTO> partialUpdateTaskTransition(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TaskTransitionDTO taskTransitionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TaskTransition partially : {}, {}", id, taskTransitionDTO);
        if (taskTransitionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taskTransitionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!taskTransitionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TaskTransitionDTO> result = taskTransitionService.partialUpdate(taskTransitionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, taskTransitionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /task-transitions/by-task/:taskId} : get the TaskTransitions for a task.
     *
     * @param taskId the id of the task.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of TaskTransitions in body.
     */
    @GetMapping("/by-task/{taskId}")
    public ResponseEntity<List<TaskTransitionDTO>> getTaskTransitionsByTask(@PathVariable("taskId") Long taskId) {
        LOG.debug("REST request to get TaskTransitions for Task : {}", taskId);
        List<TaskTransitionDTO> transitions = taskTransitionService.findByTaskId(taskId);
        return ResponseEntity.ok(transitions);
    }

    /**
     * {@code GET  /task-transitions} : get all the TaskTransitions.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of TaskTransitions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TaskTransitionDTO>> getAllTaskTransitions(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get a page of TaskTransitions");
        Page<TaskTransitionDTO> page = taskTransitionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /task-transitions/:id} : get the "id" taskTransition.
     *
     * @param id the id of the taskTransitionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the taskTransitionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskTransitionDTO> getTaskTransition(@PathVariable("id") Long id) {
        LOG.debug("REST request to get TaskTransition : {}", id);
        Optional<TaskTransitionDTO> taskTransitionDTO = taskTransitionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(taskTransitionDTO);
    }

    /**
     * {@code DELETE  /task-transitions/:id} : delete the "id" taskTransition.
     *
     * @param id the id of the taskTransitionDTO to delete.
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
    public ResponseEntity<Void> deleteTaskTransition(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete TaskTransition : {}", id);
        taskTransitionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
