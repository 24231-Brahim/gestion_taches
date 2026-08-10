package com.gestiontaches.repository;

import com.gestiontaches.domain.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Attachment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTaskIdOrderByUploadedAtDesc(Long taskId);

    @Modifying
    @Query("DELETE FROM Attachment a WHERE a.task.id IN (SELECT t.id FROM Task t WHERE t.project.id = :projectId)")
    int deleteByTaskProjectId(@Param("projectId") Long projectId);
}
