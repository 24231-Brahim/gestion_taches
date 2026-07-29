package com.gestiontaches.repository;

import com.gestiontaches.domain.TaskHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT th FROM TaskHistory th LEFT JOIN FETCH th.task LEFT JOIN FETCH th.user WHERE th.user.login = :login ORDER BY th.createdAt DESC"
    )
    java.util.List<TaskHistory> findByUserLoginOrderByCreatedAtDesc(
        @org.springframework.data.repository.query.Param("login") String login,
        org.springframework.data.domain.Pageable pageable
    );
}
