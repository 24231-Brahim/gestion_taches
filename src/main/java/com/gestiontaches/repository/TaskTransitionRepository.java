package com.gestiontaches.repository;

import com.gestiontaches.domain.TaskTransition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskTransitionRepository extends JpaRepository<TaskTransition, Long> {
    List<TaskTransition> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}
