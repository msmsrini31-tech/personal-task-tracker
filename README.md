# Task Reminder

A Spring Boot web app for your Mac. You add your day-to-day tasks in the browser,
and at the time of each task it shows a macOS notification (plus an optional pop-up
that stays on screen until you click **Done**).

## Requirements
- Java 17 or newer: `brew install openjdk@21` (or install from https://adoptium.net)
- Maven: `brew install maven`

## Run
```bash
mvn spring-boot:run
```
Or build once and run the jar:
```bash
mvn package
java -jar target/task-reminder-1.0.0.jar
```
Your browser opens at http://localhost:8080. **Keep the app running.** Reminders
are only sent while it's running.

## Using it
- **Add task**: title, optional details, time, and how often it repeats
  (one time, every day, weekdays, weekends, or weekly).
- **Today**: today's reminders and whether each one has fired yet.
- **All tasks**: every task, with **Test** (show the notification now), **Pause/Resume** and **Delete**.

Tasks are saved in `~/.task-reminder/`, so they're still there after a restart.

## First run on macOS: allow notifications
The first notification comes from **Script Editor**. If you don't see a banner, open
**System Settings → Notifications → Script Editor**, turn on *Allow notifications*,
and choose the *Alerts* style. Click **Test** on a task to check.

## Settings (`src/main/resources/application.properties`)
| Setting | Default | Meaning |
|---|---|---|
| `server.port` | 8080 | Web port |
| `reminder.show-dialog` | true | Also show a pop-up that stays until you click Done |
| `reminder.sound` | Glass | macOS sound name (Ping, Hero, Submarine…) |
| `reminder.speak` | false | Read the reminder aloud |
| `reminder.grace-minutes` | 10 | If the Mac was asleep at the exact time, still remind within this many minutes |
| `reminder.open-browser` | true | Open the page when the app starts |

## Start automatically when you log in (optional)
```bash
mvn package
mkdir -p ~/Applications/TaskReminder
cp target/task-reminder-1.0.0.jar ~/Applications/TaskReminder/
```
Create `~/Library/LaunchAgents/com.example.taskreminder.plist`. Replace `YOUR_USER`,
and use the output of `/usr/libexec/java_home` + `/bin/java` as the Java path:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0"><dict>
  <key>Label</key><string>com.example.taskreminder</string>
  <key>ProgramArguments</key><array>
    <string>/path/to/java</string>
    <string>-Dreminder.open-browser=false</string>
    <string>-jar</string>
    <string>/Users/YOUR_USER/Applications/TaskReminder/task-reminder-1.0.0.jar</string>
  </array>
  <key>RunAtLoad</key><true/>
  <key>KeepAlive</key><true/>
  <key>StandardOutPath</key><string>/tmp/task-reminder.log</string>
  <key>StandardErrorPath</key><string>/tmp/task-reminder.log</string>
</dict></plist>
```
Then load it:
```bash
launchctl load ~/Library/LaunchAgents/com.example.taskreminder.plist
```
