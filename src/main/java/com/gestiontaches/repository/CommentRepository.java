package com.gestiontaches.repository;

import com.gestiontaches.domain.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Comment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.task.id IN (SELECT t.id FROM Task t WHERE t.project.id = :projectId)")
    int deleteByTaskProjectId(@Param("projectId") Long projectId);
}
