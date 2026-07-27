package com.gestiontaches.repository;

import com.gestiontaches.domain.TaskTransition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskTransitionRepository extends JpaRepository<TaskTransition, Long> {
    List<TaskTransition> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    @Query(
        "SELECT t.createdAt, t.fromStatus, t.toStatus FROM TaskTransition t " +
            "WHERE t.task.sprint.id = :sprintId " +
            "ORDER BY t.createdAt ASC"
    )
    List<Object[]> findTransitionsBySprintId(@Param("sprintId") Long sprintId);

    @Query(
        "SELECT t.createdAt, t.fromStatus, t.toStatus FROM TaskTransition t " +
            "WHERE t.task.epic.id = :epicId " +
            "ORDER BY t.createdAt ASC"
    )
    List<Object[]> findTransitionsByEpicId(@Param("epicId") Long epicId);

    @Query("SELECT COALESCE(SUM(t.timeSpentInSeconds), 0) FROM TaskTransition t")
    Long sumTimeSpentGlobal();

    @Query("SELECT COALESCE(SUM(t.timeSpentInSeconds), 0) FROM TaskTransition t WHERE t.task.project.id = :projectId")
    Long sumTimeSpentByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COALESCE(SUM(t.timeSpentInSeconds), 0) FROM TaskTransition t WHERE t.user.id = :userId")
    Long sumTimeSpentByUserId(@Param("userId") Long userId);

    @Query(
        "SELECT t.user.login, COALESCE(SUM(t.timeSpentInSeconds), 0) FROM TaskTransition t WHERE t.user IS NOT NULL GROUP BY t.user.login ORDER BY SUM(t.timeSpentInSeconds) DESC"
    )
    List<Object[]> sumTimeSpentGroupByUser();

    @Query(
        "SELECT t.task.project.name, COALESCE(SUM(t.timeSpentInSeconds), 0) FROM TaskTransition t WHERE t.task.project IS NOT NULL GROUP BY t.task.project.name ORDER BY SUM(t.timeSpentInSeconds) DESC"
    )
    List<Object[]> sumTimeSpentGroupByProject();

    @Query("SELECT COALESCE(SUM(t.timeSpentInSeconds), 0) FROM TaskTransition t WHERE t.task.project.id IN :projectIds")
    Long sumTimeSpentByProjectIds(@Param("projectIds") java.util.Collection<Long> projectIds);

    @Query(
        "SELECT t.user.login, COALESCE(SUM(t.timeSpentInSeconds), 0) FROM TaskTransition t WHERE t.user IS NOT NULL AND t.task.project.id IN :projectIds GROUP BY t.user.login ORDER BY SUM(t.timeSpentInSeconds) DESC"
    )
    List<Object[]> sumTimeSpentGroupByUserForProjects(@Param("projectIds") java.util.Collection<Long> projectIds);

    @Query(
        "SELECT t.task.project.name, COALESCE(SUM(t.timeSpentInSeconds), 0) FROM TaskTransition t WHERE t.task.project IS NOT NULL AND t.task.project.id IN :projectIds GROUP BY t.task.project.name ORDER BY SUM(t.timeSpentInSeconds) DESC"
    )
    List<Object[]> sumTimeSpentGroupByProjectForProjects(@Param("projectIds") java.util.Collection<Long> projectIds);
}
