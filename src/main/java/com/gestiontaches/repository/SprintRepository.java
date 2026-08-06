package com.gestiontaches.repository;

import com.gestiontaches.domain.Sprint;
import com.gestiontaches.domain.enumeration.SprintStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Sprint entity.
 */
@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long>, JpaSpecificationExecutor<Sprint> {
    default Optional<Sprint> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Sprint> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Sprint> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select sprint from Sprint sprint left join fetch sprint.project",
        countQuery = "select count(sprint) from Sprint sprint"
    )
    Page<Sprint> findAllWithToOneRelationships(Pageable pageable);

    @Query("select sprint from Sprint sprint left join fetch sprint.project")
    List<Sprint> findAllWithToOneRelationships();

    @Query("select sprint from Sprint sprint left join fetch sprint.project where sprint.id =:id")
    Optional<Sprint> findOneWithToOneRelationships(@Param("id") Long id);

    /**
     * Finds the most recently created sprint for the given project and status.
     *
     * <p>Uses {@code findFirst ... OrderByIdDesc} so that the generated SQL applies a
     * {@code LIMIT 1} and returns an {@link Optional}. Unlike an {@code Optional} derived
     * from a plain {@code findBy...} method, this never throws a
     * {@code NonUniqueResultException} when the database temporarily holds duplicate
     * rows for the same (project, status) pair (e.g. two ACTIVE sprints).
     */
    Optional<Sprint> findFirstByProjectIdAndStatusOrderByIdDesc(Long projectId, SprintStatus status);

    @Query(
        "SELECT s FROM Sprint s LEFT JOIN FETCH s.project WHERE s.project.id IN :projectIds AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.goal) LIKE LOWER(CONCAT('%', :query, '%')))"
    )
    List<Sprint> searchByQuery(@Param("query") String query, @Param("projectIds") java.util.Collection<Long> projectIds);
}
