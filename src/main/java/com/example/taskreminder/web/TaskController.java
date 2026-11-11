package com.example.taskreminder.web;

import com.example.taskreminder.model.Repeat;
import com.example.taskreminder.model.Task;
import com.example.taskreminder.service.NotificationService;
import com.example.taskreminder.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
public class TaskController {

    private final TaskService taskService;
    private final NotificationService notificationService;

    public TaskController(TaskService taskService, NotificationService notificationService) {
        this.taskService = taskService;
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public String index(Model model) {
        if (!model.containsAttribute("task")) {
            Task blank = new Task();
            blank.setReminderTime(LocalTime.now().plusMinutes(5).withSecond(0).withNano(0));
            blank.setTaskDate(LocalDate.now());
            model.addAttribute("task", blank);
        }
        populate(model);
        return "index";
    }

    @PostMapping("/tasks")
    public String add(@Valid @ModelAttribute("task") Task task, BindingResult result,
                      Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            populate(model);
            return "index";
        }
        taskService.add(task);
        redirect.addFlashAttribute("message", "Task \"" + task.getTitle() + "\" added for " + task.getReminderTime());
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        taskService.delete(id);
        redirect.addFlashAttribute("message", "Task deleted");
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        taskService.toggle(id);
        return "redirect:/";
    }

    @PostMapping("/tasks/{id}/test")
    public String test(@PathVariable Long id, RedirectAttributes redirect) {
        notificationService.notify(taskService.get(id));
        redirect.addFlashAttribute("message", "Test notification sent");
        return "redirect:/";
    }

    private void populate(Model model) {
        model.addAttribute("tasks", taskService.findAll());
        model.addAttribute("todayTasks", taskService.today());
        model.addAttribute("repeats", Repeat.values());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("now", LocalTime.now());
    }
}
