package com.gestiontaches.service;

import com.gestiontaches.domain.*; // for static metamodels
import com.gestiontaches.domain.Task;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.service.criteria.TaskCriteria;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.mapper.TaskMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Task} entities in the database.
 * The main input is a {@link TaskCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link TaskDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class TaskQueryService extends QueryService<Task> {

    private static final Logger LOG = LoggerFactory.getLogger(TaskQueryService.class);

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    public TaskQueryService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    /**
     * Return a {@link Page} of {@link TaskDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<TaskDTO> findByCriteria(TaskCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Task> specification = createSpecification(criteria);
        return taskRepository.findAll(specification, page).map(taskMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(TaskCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Task> specification = createSpecification(criteria);
        return taskRepository.count(specification);
    }

    /**
     * Function to convert {@link TaskCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Task> createSpecification(TaskCriteria criteria) {
        Specification<Task> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Task_.sprint, JoinType.LEFT);
                root.fetch(Task_.epic, JoinType.LEFT);
                root.fetch(Task_.project, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Task_.id),
                    buildStringSpecification(criteria.getTitle(), Task_.title),
                    buildStringSpecification(criteria.getDescription(), Task_.description),
                    buildSpecification(criteria.getStatus(), Task_.status),
                    buildSpecification(criteria.getPriority(), Task_.priority),
                    buildRangeSpecification(criteria.getCreatedAt(), Task_.createdAt),
                    buildRangeSpecification(criteria.getUpdatedAt(), Task_.updatedAt),
                    buildSpecification(criteria.getCommentsId(), root -> root.join(Task_.comments, JoinType.LEFT).get(Comment_.id)),
                    buildSpecification(criteria.getAttachmentsId(), root ->
                        root.join(Task_.attachments, JoinType.LEFT).get(Attachment_.id)
                    ),
                    buildSpecification(criteria.getSprintId(), root -> root.join(Task_.sprint, JoinType.LEFT).get(Sprint_.id)),
                    buildSpecification(criteria.getEpicId(), root -> root.join(Task_.epic, JoinType.LEFT).get(Epic_.id)),
                    buildSpecification(criteria.getProjectId(), root -> root.join(Task_.project, JoinType.LEFT).get(Project_.id))
                )
            );
        }
        return specification;
    }
}
