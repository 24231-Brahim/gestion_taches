package com.gestiontaches.repository;

import com.gestiontaches.domain.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @EntityGraph(attributePaths = { "task", "user" })
    List<Notification> findByUser_idOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = { "task", "user" })
    Page<Notification> findByUser_idOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = { "task", "user" })
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByUser_idAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    boolean existsByTaskIdAndMessageContaining(Long taskId, String messagePart);

    int deleteByCreatedAtBefore(java.time.Instant limit);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.task.id = :taskId")
    int deleteByTaskId(@Param("taskId") Long taskId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId OR n.relatedUserId = :userId")
    int deleteByUser_IdOrRelatedUserId(@Param("userId") Long userId, @Param("relatedUserId") Long relatedUserId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.task IS NOT NULL AND n.task.id IN (SELECT t.id FROM Task t WHERE t.project.id = :projectId)")
    int deleteByTaskProjectId(@Param("projectId") Long projectId);
}
