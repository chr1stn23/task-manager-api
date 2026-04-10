package com.christian.taskmanager.repository;

import com.christian.taskmanager.dto.response.TaskSummaryDTO;
import com.christian.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndDeletedFalse(Long id);

    Optional<Task> findByIdAndDeletedTrue(Long id);

    @Query("""
                SELECT NEW com.christian.taskmanager.dto.response.TaskSummaryDTO(
                    COUNT(t),
                    COALESCE(SUM(CASE WHEN t.status = TaskStatus.TODO THEN 1 ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN t.status = TaskStatus.IN_PROGRESS THEN 1 ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN t.status = TaskStatus.DONE THEN 1 ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN t.priority = Priority.LOW THEN 1 ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN t.priority = Priority.MEDIUM THEN 1 ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN t.priority = Priority.HIGH THEN 1 ELSE 0 END), 0),
                    (
                        SELECT COALESCE(COUNT(t2), 0)
                        FROM Task t2
                        WHERE t2.user.id = :userId AND t2.deleted = TRUE
                    )
                )
                FROM Task t
                WHERE t.user.id = :userId AND t.deleted = FALSE
            """)
    TaskSummaryDTO getSummaryByUser(@Param("userId") Long userId);
}
