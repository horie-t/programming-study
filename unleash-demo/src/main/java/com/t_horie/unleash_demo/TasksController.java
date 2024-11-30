package com.t_horie.unleash_demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TasksController {
    private final GetTasksUsecase getTasksUsecase;

    public TasksController(GetTasksUsecase getTasksUsecase) {
        this.getTasksUsecase = getTasksUsecase;
    }

    @GetMapping
    public TasksResponse getTasks() {
        return new TasksResponse(getTasksUsecase.getTasks());
    }
}
