package com.christian.taskmanager.repository;

import com.christian.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndDeletedFalse(Long id);

    Optional<Task> findByIdAndDeletedTrue(Long id);
}
