package com.gestiontaches.repository;

import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.enumeration.TaskStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    default Optional<Task> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Task> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Task> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select task from Task task left join fetch task.sprint left join fetch task.epic left join fetch task.project left join fetch task.assignee left join fetch task.createdBy",
        countQuery = "select count(task) from Task task"
    )
    Page<Task> findAllWithToOneRelationships(Pageable pageable);

    @Query(
        "select task from Task task left join fetch task.sprint left join fetch task.epic left join fetch task.project left join fetch task.assignee left join fetch task.createdBy"
    )
    List<Task> findAllWithToOneRelationships();

    @Query(
        "select task from Task task left join fetch task.sprint left join fetch task.epic left join fetch task.project left join fetch task.assignee left join fetch task.createdBy where task.id =:id"
    )
    Optional<Task> findOneWithToOneRelationships(@Param("id") Long id);

    @Query(
        "select task from Task task left join fetch task.sprint left join fetch task.epic left join fetch task.project left join fetch task.assignee left join fetch task.createdBy where task.project.id = :projectId"
    )
    List<Task> findAllByProjectIdWithToOneRelationships(@Param("projectId") Long projectId);

    List<Task> findBySprintId(Long sprintId);

    List<Task> findBySprintIdAndStatus(Long sprintId, TaskStatus status);

    List<Task> findByProjectIdAndSprintIsNull(Long projectId);

    @Query(
        "SELECT t FROM Task t LEFT JOIN FETCH t.sprint LEFT JOIN FETCH t.epic LEFT JOIN FETCH t.project LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.createdBy WHERE t.project.id = :projectId AND t.sprint IS NULL"
    )
    List<Task> findByProjectIdAndSprintIsNullWithToOneRelationships(@Param("projectId") Long projectId);

    long countByStatus(TaskStatus status);

    long countByStatusNotIn(java.util.Collection<TaskStatus> statuses);

    long countByProjectIdIn(java.util.Collection<Long> projectIds);

    long countByProjectIdInAndStatus(java.util.Collection<Long> projectIds, TaskStatus status);

    @Query(
        "SELECT COUNT(t) FROM Task t WHERE t.status NOT IN (com.gestiontaches.domain.enumeration.TaskStatus.DONE, com.gestiontaches.domain.enumeration.TaskStatus.CANCELLED) AND ((t.sprint IS NOT NULL AND t.sprint.endDate < :today) OR (t.sprint IS NULL AND t.createdAt < :cutoff))"
    )
    long countOverdueTasksGlobal(@Param("today") java.time.LocalDate today, @Param("cutoff") java.time.Instant cutoff);

    @Query(
        "SELECT COUNT(t) FROM Task t WHERE t.project.id IN :projectIds AND t.status NOT IN (com.gestiontaches.domain.enumeration.TaskStatus.DONE, com.gestiontaches.domain.enumeration.TaskStatus.CANCELLED) AND ((t.sprint IS NOT NULL AND t.sprint.endDate < :today) OR (t.sprint IS NULL AND t.createdAt < :cutoff))"
    )
    long countOverdueTasksByProjectIds(
        @Param("projectIds") java.util.Collection<Long> projectIds,
        @Param("today") java.time.LocalDate today,
        @Param("cutoff") java.time.Instant cutoff
    );

    @Query(
        "SELECT p.id, p.name, COUNT(t), SUM(CASE WHEN t.status = com.gestiontaches.domain.enumeration.TaskStatus.DONE THEN 1 ELSE 0 END) FROM Project p LEFT JOIN p.tasks t GROUP BY p.id, p.name ORDER BY p.name"
    )
    List<Object[]> countTasksGroupByProjectAll();

    @Query(
        "SELECT p.id, p.name, COUNT(t), SUM(CASE WHEN t.status = com.gestiontaches.domain.enumeration.TaskStatus.DONE THEN 1 ELSE 0 END) FROM Project p LEFT JOIN p.tasks t WHERE p.id IN :projectIds GROUP BY p.id, p.name ORDER BY p.name"
    )
    List<Object[]> countTasksGroupByProjectForProjects(@Param("projectIds") java.util.Collection<Long> projectIds);

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksGroupByStatus();

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id IN :projectIds GROUP BY t.status")
    List<Object[]> countTasksGroupByStatusForProjects(@Param("projectIds") java.util.Collection<Long> projectIds);

    @Query(
        "select t from Task t left join fetch t.sprint left join fetch t.epic left join fetch t.project left join fetch t.assignee left join fetch t.createdBy where t.assignee.login = :login"
    )
    List<Task> findByAssigneeLoginWithToOneRelationships(@Param("login") String login);
}
