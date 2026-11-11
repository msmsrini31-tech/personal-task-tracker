package com.example.taskreminder.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Please enter a title")
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Please choose a time")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime reminderTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Repeat repeatRule = Repeat.DAILY;

    /** Date for one-time tasks; start date for repeating tasks. */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate taskDate;

    private boolean active = true;

    /** Day the reminder last fired, so it fires only once per day. */
    private LocalDate lastNotifiedOn;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalTime getReminderTime() { return reminderTime; }
    public void setReminderTime(LocalTime reminderTime) { this.reminderTime = reminderTime; }
    public Repeat getRepeatRule() { return repeatRule; }
    public void setRepeatRule(Repeat repeatRule) { this.repeatRule = repeatRule; }
    public LocalDate getTaskDate() { return taskDate; }
    public void setTaskDate(LocalDate taskDate) { this.taskDate = taskDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDate getLastNotifiedOn() { return lastNotifiedOn; }
    public void setLastNotifiedOn(LocalDate lastNotifiedOn) { this.lastNotifiedOn = lastNotifiedOn; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** True if this task applies today and hasn't fired yet today. */
    public boolean isPendingOn(LocalDate day) {
        return active && repeatRule.appliesOn(day, taskDate) && !day.equals(lastNotifiedOn);
    }
}
