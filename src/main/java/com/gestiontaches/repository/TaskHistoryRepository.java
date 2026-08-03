package com.gestiontaches.repository;

import com.gestiontaches.domain.TaskHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    List<TaskHistory> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    @Query(
        "SELECT th FROM TaskHistory th LEFT JOIN FETCH th.task LEFT JOIN FETCH th.user WHERE th.user.login = :login ORDER BY th.createdAt DESC"
    )
    List<TaskHistory> findByUserLoginOrderByCreatedAtDesc(@Param("login") String login, org.springframework.data.domain.Pageable pageable);
}
