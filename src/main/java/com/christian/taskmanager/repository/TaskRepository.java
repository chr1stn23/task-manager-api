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
                    SUM(CASE WHEN t.deleted = FALSE THEN 1 ELSE 0 END),
                    SUM(CASE WHEN t.status = TaskStatus.TODO AND t.deleted = FALSE THEN 1 ELSE 0 END),
                    SUM(CASE WHEN t.status = TaskStatus.IN_PROGRESS AND t.deleted = FALSE THEN 1 ELSE 0 END),
                    SUM(CASE WHEN t.status = TaskStatus.DONE AND t.deleted = FALSE THEN 1 ELSE 0 END),
                    SUM(CASE WHEN t.priority = Priority.LOW AND t.deleted = FALSE THEN 1 ELSE 0 END),
                    SUM(CASE WHEN t.priority = Priority.MEDIUM AND t.deleted = FALSE THEN 1 ELSE 0 END),
                    SUM(CASE WHEN t.priority = Priority.HIGH AND t.deleted = FALSE THEN 1 ELSE 0 END),
                    SUM(CASE WHEN t.deleted = TRUE THEN 1 ELSE 0 END)
                )
                FROM Task t
                WHERE t.user.id = :userId
            """)
    TaskSummaryDTO getSummaryByUser(@Param("userId") Long userId);
}
