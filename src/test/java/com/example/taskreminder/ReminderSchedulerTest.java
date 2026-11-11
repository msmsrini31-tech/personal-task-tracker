package com.example.taskreminder;

import com.example.taskreminder.model.Repeat;
import com.example.taskreminder.model.Task;
import com.example.taskreminder.repository.TaskRepository;
import com.example.taskreminder.service.NotificationService;
import com.example.taskreminder.service.ReminderScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:test",
        "reminder.open-browser=false"
})
class ReminderSchedulerTest {

    @Autowired TaskRepository repository;
    @Autowired ReminderScheduler scheduler;
    @MockitoBean NotificationService notifier;

    // 2026-11-11 is a Wednesday
    final LocalDate wed = LocalDate.of(2026, 11, 11);

    @BeforeEach
    void clean() { repository.deleteAll(); }

    private Task task(String title, String time, Repeat repeat, LocalDate date) {
        Task t = new Task();
        t.setTitle(title);
        t.setReminderTime(LocalTime.parse(time));
        t.setRepeatRule(repeat);
        t.setTaskDate(date);
        return repository.save(t);
    }

    @Test
    void firesOnceAtDueTimeAndNotAgainSameDay() {
        task("Standup", "09:00", Repeat.DAILY, wed);
        assertThat(scheduler.checkDueTasks(wed.atTime(8, 59))).isZero();
        assertThat(scheduler.checkDueTasks(wed.atTime(9, 0))).isEqualTo(1);
        assertThat(scheduler.checkDueTasks(wed.atTime(9, 0, 30))).isZero();
        assertThat(scheduler.checkDueTasks(wed.plusDays(1).atTime(9, 0))).isEqualTo(1);
        verify(notifier, times(2)).notify(any());
    }

    @Test
    void graceWindowCatchesUpAfterSleepButNotTooLate() {
        task("A", "09:00", Repeat.DAILY, wed);
        task("B", "07:00", Repeat.DAILY, wed);
        assertThat(scheduler.checkDueTasks(wed.atTime(9, 7))).isEqualTo(1); // only A
    }

    @Test
    void oneTimeTaskDeactivatesAfterFiring() {
        Task t = task("Dentist", "15:30", Repeat.ONCE, wed);
        assertThat(scheduler.checkDueTasks(wed.minusDays(1).atTime(15, 30))).isZero();
        assertThat(scheduler.checkDueTasks(wed.atTime(15, 30))).isEqualTo(1);
        assertThat(repository.findById(t.getId()).orElseThrow().isActive()).isFalse();
    }

    @Test
    void weekdayAndWeekendRules() {
        task("Work", "10:00", Repeat.WEEKDAYS, wed);
        task("Gym", "10:00", Repeat.WEEKENDS, wed);
        assertThat(scheduler.checkDueTasks(LocalDateTime.of(wed, LocalTime.of(10, 0)))).isEqualTo(1);
        assertThat(scheduler.checkDueTasks(wed.plusDays(3).atTime(10, 0))).isEqualTo(1); // Saturday: Gym only
    }
}
