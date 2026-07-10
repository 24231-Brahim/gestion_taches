package com.gestiontaches.repository;

import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.enumeration.SprintStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Project entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByOwnerLogin(String login, Pageable pageable);
    Optional<Project> findByIdAndOwnerLogin(Long id, String login);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.projectMembers pm WHERE p.owner.login = ?1 OR pm.user.login = ?1")
    Page<Project> findByOwnerLoginOrMemberLogin(String login, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.projectMembers pm WHERE p.id = ?1 AND (p.owner.login = ?2 OR pm.user.login = ?2)")
    Optional<Project> findByIdAndOwnerLoginOrMemberLogin(Long id, String login);

    @Query("SELECT COUNT(DISTINCT p) FROM Project p LEFT JOIN p.projectMembers pm WHERE p.owner.login = ?1 OR pm.user.login = ?1")
    long countByOwnerLoginOrMemberLogin(String login);

    @Query(
        "SELECT COUNT(DISTINCT p) FROM Project p LEFT JOIN p.sprintses s LEFT JOIN p.projectMembers pm WHERE (p.owner.login = ?1 OR pm.user.login = ?1) AND s.status = ?2"
    )
    long countActiveProjectsForUser(String login, SprintStatus status);

    Optional<Project> findByKey(String key);
}
