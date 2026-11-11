package com.example.taskreminder.service;

import com.example.taskreminder.model.Repeat;
import com.example.taskreminder.model.Task;
import com.example.taskreminder.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Checks every 30 seconds for tasks that are due and fires a desktop notification. */
@Component
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final TaskRepository repository;
    private final NotificationService notifier;

    @Value("${reminder.grace-minutes:10}")
    private long graceMinutes;

    public ReminderScheduler(TaskRepository repository, NotificationService notifier) {
        this.repository = repository;
        this.notifier = notifier;
    }

    @Scheduled(cron = "*/30 * * * * *")
    @Transactional
    public void checkDueTasks() {
        checkDueTasks(LocalDateTime.now());
    }

    /** Package-visible with an explicit clock so it can be tested. */
    @Transactional
    public int checkDueTasks(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalTime time = now.toLocalTime();
        int fired = 0;

        for (Task task : repository.findByActiveTrue()) {
            if (!task.isPendingOn(today)) {
                continue;
            }
            LocalTime due = task.getReminderTime();
            long minutesLate = Duration.between(due, time).toMinutes();
            // Due now, or missed by at most graceMinutes (e.g. the Mac was asleep)
            if (!time.isBefore(due) && minutesLate <= graceMinutes) {
                notifier.notify(task);
                task.setLastNotifiedOn(today);
                if (task.getRepeatRule() == Repeat.ONCE) {
                    task.setActive(false); // one-time task is done
                }
                fired++;
            }
        }
        if (fired > 0) {
            log.info("Sent {} reminder(s) at {}", fired, time.withNano(0));
        }
        return fired;
    }
}
