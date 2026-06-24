package com.gestiontaches.service;

import com.gestiontaches.domain.*; // for static metamodels
import com.gestiontaches.domain.Issue;
import com.gestiontaches.repository.IssueRepository;
import com.gestiontaches.service.criteria.IssueCriteria;
import com.gestiontaches.service.dto.IssueDTO;
import com.gestiontaches.service.mapper.IssueMapper;
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
 * Service for executing complex queries for {@link Issue} entities in the database.
 * The main input is a {@link IssueCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link IssueDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class IssueQueryService extends QueryService<Issue> {

    private static final Logger LOG = LoggerFactory.getLogger(IssueQueryService.class);

    private final IssueRepository issueRepository;

    private final IssueMapper issueMapper;

    public IssueQueryService(IssueRepository issueRepository, IssueMapper issueMapper) {
        this.issueRepository = issueRepository;
        this.issueMapper = issueMapper;
    }

    /**
     * Return a {@link Page} of {@link IssueDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<IssueDTO> findByCriteria(IssueCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Issue> specification = createSpecification(criteria);
        return issueRepository.findAll(specification, page).map(issueMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(IssueCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Issue> specification = createSpecification(criteria);
        return issueRepository.count(specification);
    }

    /**
     * Function to convert {@link IssueCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Issue> createSpecification(IssueCriteria criteria) {
        Specification<Issue> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Issue_.sprint, JoinType.LEFT);
                root.fetch(Issue_.epic, JoinType.LEFT);
                root.fetch(Issue_.project, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Issue_.id),
                    buildStringSpecification(criteria.getTitle(), Issue_.title),
                    buildStringSpecification(criteria.getDescription(), Issue_.description),
                    buildSpecification(criteria.getType(), Issue_.type),
                    buildSpecification(criteria.getStatus(), Issue_.status),
                    buildSpecification(criteria.getPriority(), Issue_.priority),
                    buildRangeSpecification(criteria.getCreatedAt(), Issue_.createdAt),
                    buildRangeSpecification(criteria.getUpdatedAt(), Issue_.updatedAt),
                    buildSpecification(criteria.getCommentsId(), root -> root.join(Issue_.commentses, JoinType.LEFT).get(Comment_.id)),
                    buildSpecification(criteria.getAttachmentsId(), root ->
                        root.join(Issue_.attachmentses, JoinType.LEFT).get(Attachment_.id)
                    ),
                    buildSpecification(criteria.getHistoryId(), root -> root.join(Issue_.histories, JoinType.LEFT).get(ActionHistory_.id)),
                    buildSpecification(criteria.getSprintId(), root -> root.join(Issue_.sprint, JoinType.LEFT).get(Sprint_.id)),
                    buildSpecification(criteria.getEpicId(), root -> root.join(Issue_.epic, JoinType.LEFT).get(Epic_.id)),
                    buildSpecification(criteria.getProjectId(), root -> root.join(Issue_.project, JoinType.LEFT).get(Project_.id))
                )
            );
        }
        return specification;
    }
}
