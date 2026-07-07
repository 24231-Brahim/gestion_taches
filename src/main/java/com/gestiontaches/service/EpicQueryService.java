package com.gestiontaches.service;

import com.gestiontaches.domain.*; // for static metamodels
import com.gestiontaches.domain.Epic;
import com.gestiontaches.repository.EpicRepository;
import com.gestiontaches.service.criteria.EpicCriteria;
import com.gestiontaches.service.dto.EpicDTO;
import com.gestiontaches.service.mapper.EpicMapper;
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
 * Service for executing complex queries for {@link Epic} entities in the database.
 * The main input is a {@link EpicCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link EpicDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class EpicQueryService extends QueryService<Epic> {

    private static final Logger LOG = LoggerFactory.getLogger(EpicQueryService.class);

    private final EpicRepository epicRepository;

    private final EpicMapper epicMapper;

    public EpicQueryService(EpicRepository epicRepository, EpicMapper epicMapper) {
        this.epicRepository = epicRepository;
        this.epicMapper = epicMapper;
    }

    /**
     * Return a {@link Page} of {@link EpicDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<EpicDTO> findByCriteria(EpicCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Epic> specification = createSpecification(criteria);
        return epicRepository.findAll(specification, page).map(epicMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(EpicCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Epic> specification = createSpecification(criteria);
        return epicRepository.count(specification);
    }

    /**
     * Function to convert {@link EpicCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Epic> createSpecification(EpicCriteria criteria) {
        Specification<Epic> specification = Specification.unrestricted();
        specification = specification.and((root, query, builder) -> {
            if (Long.class != query.getResultType()) {
                root.fetch(Epic_.project, JoinType.LEFT);
            }
            return null;
        });
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = specification.and(
                Specification.allOf(
                    Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : Specification.unrestricted(),
                    buildRangeSpecification(criteria.getId(), Epic_.id),
                    buildStringSpecification(criteria.getTitle(), Epic_.title),
                    buildStringSpecification(criteria.getDescription(), Epic_.description),
                    buildSpecification(criteria.getStatus(), Epic_.status),
                    buildSpecification(criteria.getPriority(), Epic_.priority),
                    buildRangeSpecification(criteria.getCreatedAt(), Epic_.createdAt),
                    buildRangeSpecification(criteria.getUpdatedAt(), Epic_.updatedAt),
                    buildSpecification(criteria.getProjectId(), root -> root.join(Epic_.project, JoinType.LEFT).get(Project_.id))
                )
            );
        }
        return specification;
    }
}
