package com.example.taskreminder.repository;

import com.example.taskreminder.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOrderByReminderTimeAsc();
    List<Task> findByActiveTrue();
}
