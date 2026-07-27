package com.gestiontaches.repository;

import com.gestiontaches.domain.Project;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Project entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByOwnerLogin(String login, Pageable pageable);
    Optional<Project> findByIdAndOwnerLogin(Long id, String login);

    @Query(
        "SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN p.projectMembers pm WHERE p.owner.login = ?1 OR pm.user.login = ?1"
    )
    Page<Project> findByOwnerLoginOrMemberLogin(String login, Pageable pageable);

    @Query(
        "SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN p.projectMembers pm WHERE p.id = ?1 AND (p.owner.login = ?2 OR pm.user.login = ?2)"
    )
    Optional<Project> findByIdAndOwnerLoginOrMemberLogin(Long id, String login);

    @Query(
        "SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN p.projectMembers pm WHERE p.owner.login = ?1 OR pm.user.login = ?1"
    )
    java.util.List<Project> findAllByOwnerLoginOrMemberLogin(String login);

    Optional<Project> findByKey(String key);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.owner")
    java.util.List<Project> findAllWithOwner();

    @Query(
        "SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN p.projectMembers pm WHERE (p.owner.login = :login OR pm.user.login = :login) AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.key) LIKE LOWER(CONCAT('%', :query, '%')))"
    )
    java.util.List<Project> searchByQuery(@Param("query") String query, @Param("login") String login);
}
