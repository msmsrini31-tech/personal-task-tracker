package com.example.taskreminder.service;

import com.example.taskreminder.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows a desktop notification on the machine running the app.
 * macOS: banner via osascript (+ optional pop-up dialog and speech).
 * Linux: notify-send. Windows: PowerShell balloon tip.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final String OS = System.getProperty("os.name", "").toLowerCase();

    @Value("${reminder.show-dialog:true}")
    private boolean showDialog;

    @Value("${reminder.sound:Glass}")
    private String sound;

    @Value("${reminder.speak:false}")
    private boolean speak;

    public void notify(Task task) {
        String title = "Reminder: " + task.getTitle();
        String body = (task.getDescription() == null || task.getDescription().isBlank())
                ? "It's " + task.getReminderTime() + " - time for: " + task.getTitle()
                : task.getDescription();
        log.info("NOTIFY [{}] {} - {}", task.getReminderTime(), task.getTitle(), body);

        try {
            if (OS.contains("mac")) {
                notifyMac(title, body, task.getTitle());
            } else if (OS.contains("win")) {
                notifyWindows(title, body);
            } else {
                run(List.of("notify-send", "-u", "critical", title, body));
            }
        } catch (IOException e) {
            log.warn("Could not show desktop notification: {}", e.getMessage());
        }
    }

    private void notifyMac(String title, String body, String spokenText) throws IOException {
        // Arguments are passed via argv, so quotes in task text need no escaping.
        run(List.of("osascript",
                "-e", "on run argv",
                "-e", "display notification (item 2 of argv) with title (item 1 of argv) sound name (item 3 of argv)",
                "-e", "end run",
                title, body, sound));

        if (showDialog) {
            // A dialog stays on screen until you click "Done" (gives up after 30 minutes).
            run(List.of("osascript",
                    "-e", "on run argv",
                    "-e", "activate",
                    "-e", "display dialog (item 2 of argv) with title (item 1 of argv) "
                            + "buttons {\"Done\"} default button \"Done\" with icon note giving up after 1800",
                    "-e", "end run",
                    title, body));
        }
        if (speak) {
            run(List.of("say", "Reminder: " + spokenText));
        }
    }

    private void notifyWindows(String title, String body) throws IOException {
        String ps = "Add-Type -AssemblyName System.Windows.Forms;"
                + "$n=New-Object System.Windows.Forms.NotifyIcon;"
                + "$n.Icon=[System.Drawing.SystemIcons]::Information;$n.Visible=$true;"
                + "$n.ShowBalloonTip(10000,$env:R_TITLE,$env:R_BODY,'Info');Start-Sleep 11;$n.Dispose()";
        ProcessBuilder pb = new ProcessBuilder("powershell", "-NoProfile", "-Command", ps);
        pb.environment().put("R_TITLE", title);
        pb.environment().put("R_BODY", body);
        pb.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
    }

    /** Starts the command without waiting, so a dialog doesn't block other reminders. */
    private void run(List<String> command) throws IOException {
        new ProcessBuilder(new ArrayList<>(command))
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();
    }
}
