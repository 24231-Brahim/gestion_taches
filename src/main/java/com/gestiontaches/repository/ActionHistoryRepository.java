package com.gestiontaches.repository;

import com.gestiontaches.domain.ActionHistory;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the ActionHistory entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ActionHistoryRepository extends JpaRepository<ActionHistory, Long> {
    List<ActionHistory> findByIssueIdOrderByCreatedAtDesc(Long issueId);
}
