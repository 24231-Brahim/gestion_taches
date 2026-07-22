package com.gestiontaches.repository;

import com.gestiontaches.domain.ProjectMember;
import com.gestiontaches.domain.enumeration.ProjectRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@SuppressWarnings("unused")
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProjectId(Long projectId);
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectIdAndRole(Long projectId, ProjectRole role);

    @Query("SELECT COUNT(DISTINCT pm.user.id) FROM ProjectMember pm")
    long countDistinctUsers();

    @Query("SELECT pm FROM ProjectMember pm LEFT JOIN FETCH pm.project LEFT JOIN FETCH pm.user WHERE pm.user.id = :userId")
    List<ProjectMember> findByUserId(@Param("userId") Long userId);
}
