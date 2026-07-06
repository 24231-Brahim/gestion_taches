package com.gestiontaches.repository;

import com.gestiontaches.domain.Issue;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Issue entity.
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {
    default Optional<Issue> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Issue> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Issue> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select issue from Issue issue left join fetch issue.sprint left join fetch issue.epic left join fetch issue.project left join fetch issue.assignee left join fetch issue.createdBy",
        countQuery = "select count(issue) from Issue issue"
    )
    Page<Issue> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select issue from Issue issue left join fetch issue.sprint left join fetch issue.epic left join fetch issue.project left join fetch issue.assignee left join fetch issue.createdBy"
    )
    List<Issue> findAllWithToOneRelationships();

    @Query(
        "select issue from Issue issue left join fetch issue.sprint left join fetch issue.epic left join fetch issue.project left join fetch issue.assignee left join fetch issue.createdBy where issue.id =:id"
    )
    Optional<Issue> findOneWithToOneRelationships(@Param("id") Long id);
}
