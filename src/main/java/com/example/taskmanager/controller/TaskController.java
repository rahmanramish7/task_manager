package com.example.taskmanager.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public Task createTask(@RequestBody Task task, Principal principal) {

        User user = userRepository
                .findByUsername(principal.getName())
                .orElseThrow();

        task.setUser(user);   // USER can create ONLY for himself
        return taskRepository.save(task);
    }

    @GetMapping
    public List<Task> getMyTasks(Principal principal) {

        User user = userRepository
                .findByUsername(principal.getName())
                .orElseThrow();

        return taskRepository.findByUser(user);
    }
}