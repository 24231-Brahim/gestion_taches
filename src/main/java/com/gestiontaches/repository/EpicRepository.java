package com.gestiontaches.repository;

import com.gestiontaches.domain.Epic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Epic entity.
 */
@Repository
public interface EpicRepository extends JpaRepository<Epic, Long>, JpaSpecificationExecutor<Epic> {
    default Optional<Epic> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Epic> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Epic> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select epic from Epic epic left join fetch epic.project", countQuery = "select count(epic) from Epic epic")
    Page<Epic> findAllWithToOneRelationships(Pageable pageable);

    @Query("select epic from Epic epic left join fetch epic.project")
    List<Epic> findAllWithToOneRelationships();

    @Query("select epic from Epic epic left join fetch epic.project where epic.id =:id")
    Optional<Epic> findOneWithToOneRelationships(@Param("id") Long id);
}
