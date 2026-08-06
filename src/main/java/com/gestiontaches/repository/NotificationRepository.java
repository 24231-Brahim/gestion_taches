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
}
