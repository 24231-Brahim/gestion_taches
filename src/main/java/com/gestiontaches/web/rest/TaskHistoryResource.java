package com.gestiontaches.web.rest;

import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.TaskHistoryService;
import com.gestiontaches.service.dto.TaskHistoryDTO;
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
 * REST controller for managing {@link com.gestiontaches.domain.TaskHistory}.
 */
@RestController
@RequestMapping("/api/task-histories")
public class TaskHistoryResource {

    private static final Logger LOG = LoggerFactory.getLogger(TaskHistoryResource.class);

    private static final String ENTITY_NAME = "taskHistory";

    @Value("${jhipster.clientApp.name:gestionTaches}")
    private String applicationName;

    private final TaskHistoryService taskHistoryService;

    public TaskHistoryResource(TaskHistoryService taskHistoryService) {
        this.taskHistoryService = taskHistoryService;
    }

    /**
     * {@code POST  /task-histories} : Create a new taskHistory.
     *
     * @param taskHistoryDTO the taskHistoryDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new taskHistoryDTO, or with status {@code 400 (Bad Request)} if the taskHistory has already an ID.
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
    public ResponseEntity<TaskHistoryDTO> createTaskHistory(@Valid @RequestBody TaskHistoryDTO taskHistoryDTO) throws URISyntaxException {
        LOG.debug("REST request to save TaskHistory : {}", taskHistoryDTO);
        if (taskHistoryDTO.getId() != null) {
            throw new BadRequestAlertException("A new taskHistory cannot already have an ID", ENTITY_NAME, "idexists");
        }
        taskHistoryDTO = taskHistoryService.save(taskHistoryDTO);
        return ResponseEntity.created(new URI("/api/task-histories/" + taskHistoryDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, taskHistoryDTO.getId().toString()))
            .body(taskHistoryDTO);
    }

    /**
     * {@code PUT  /task-histories/:id} : Updates an existing taskHistory.
     *
     * @param id the id of the taskHistoryDTO to save.
     * @param taskHistoryDTO the taskHistoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated taskHistoryDTO,
     * or with status {@code 400 (Bad Request)} if the taskHistoryDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the taskHistoryDTO couldn't be updated.
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
    public ResponseEntity<TaskHistoryDTO> updateTaskHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TaskHistoryDTO taskHistoryDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TaskHistory : {}, {}", id, taskHistoryDTO);
        if (taskHistoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taskHistoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        Optional<TaskHistoryDTO> result = taskHistoryService.partialUpdate(taskHistoryDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, taskHistoryDTO.getId().toString())
        );
    }

    /**
     * {@code PATCH  /task-histories/:id} : Partial updates given fields of an existing taskHistory, field will ignore if it is null
     *
     * @param id the id of the taskHistoryDTO to save.
     * @param taskHistoryDTO the taskHistoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated taskHistoryDTO,
     * or with status {@code 400 (Bad Request)} if the taskHistoryDTO is not valid,
     * or with status {@code 404 (Not Found)} if the taskHistoryDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the taskHistoryDTO couldn't be updated.
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
    public ResponseEntity<TaskHistoryDTO> partialUpdateTaskHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TaskHistoryDTO taskHistoryDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TaskHistory partially : {}, {}", id, taskHistoryDTO);
        if (taskHistoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taskHistoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        Optional<TaskHistoryDTO> result = taskHistoryService.partialUpdate(taskHistoryDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, taskHistoryDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /task-histories} : get all the taskHistories.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of taskHistories in body.
     */
    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<List<TaskHistoryDTO>> getAllTaskHistories(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of TaskHistories");
        Page<TaskHistoryDTO> page = taskHistoryService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /task-histories/by-task/:taskId} : get all taskHistories for a given task.
     *
     * @param taskId the id of the task.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of taskHistories in body.
     */
    @GetMapping("/by-task/{taskId}")
    public ResponseEntity<List<TaskHistoryDTO>> getTaskHistoriesByTask(@PathVariable("taskId") Long taskId) {
        LOG.debug("REST request to get TaskHistories for Task : {}", taskId);
        List<TaskHistoryDTO> taskHistories = taskHistoryService.findByTaskId(taskId);
        return ResponseEntity.ok(taskHistories);
    }

    /**
     * {@code GET  /task-histories/:id} : get the "id" taskHistory.
     *
     * @param id the id of the taskHistoryDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the taskHistoryDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskHistoryDTO> getTaskHistory(@PathVariable("id") Long id) {
        LOG.debug("REST request to get TaskHistory : {}", id);
        Optional<TaskHistoryDTO> taskHistoryDTO = taskHistoryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(taskHistoryDTO);
    }

    /**
     * {@code DELETE  /task-histories/:id} : delete the "id" taskHistory.
     *
     * @param id the id of the taskHistoryDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "')")
    public ResponseEntity<Void> deleteTaskHistory(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete TaskHistory : {}", id);
        taskHistoryService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
