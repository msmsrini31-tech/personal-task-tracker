package com.example.taskreminder.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

public enum Repeat {
    ONCE("One time"),
    DAILY("Every day"),
    WEEKDAYS("Weekdays (Mon-Fri)"),
    WEEKENDS("Weekends (Sat-Sun)"),
    WEEKLY("Every week (same weekday)");

    private final String label;

    Repeat(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Whether a task with this repeat rule (and start/one-time date) applies on the given day. */
    public boolean appliesOn(LocalDate day, LocalDate taskDate) {
        if (taskDate != null && day.isBefore(taskDate)) {
            return false;
        }
        DayOfWeek dow = day.getDayOfWeek();
        boolean weekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
        return switch (this) {
            case ONCE -> day.equals(taskDate);
            case DAILY -> true;
            case WEEKDAYS -> !weekend;
            case WEEKENDS -> weekend;
            case WEEKLY -> taskDate == null || dow == taskDate.getDayOfWeek();
        };
    }
}
