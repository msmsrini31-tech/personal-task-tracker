package com.example.taskreminder.service;

import com.example.taskreminder.model.Repeat;
import com.example.taskreminder.model.Task;
import com.example.taskreminder.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> findAll() {
        return repository.findAllByOrderByReminderTimeAsc();
    }

    /** Active tasks scheduled for today, sorted by time. */
    public List<Task> today() {
        LocalDate today = LocalDate.now();
        return findAll().stream()
                .filter(t -> t.isActive() && t.getRepeatRule().appliesOn(today, t.getTaskDate()))
                .toList();
    }

    @Transactional
    public Task add(Task task) {
        if (task.getTaskDate() == null) {
            task.setTaskDate(LocalDate.now());
        }
        // Seconds are irrelevant for reminders
        task.setReminderTime(task.getReminderTime().withSecond(0).withNano(0));
        return repository.save(task);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void toggle(Long id) {
        repository.findById(id).ifPresent(t -> {
            t.setActive(!t.isActive());
            if (t.isActive() && t.getRepeatRule() == Repeat.ONCE) {
                t.setLastNotifiedOn(null); // re-enable a finished one-time task
            }
        });
    }

    public Task get(Long id) {
        return repository.findById(id).orElseThrow();
    }
}
