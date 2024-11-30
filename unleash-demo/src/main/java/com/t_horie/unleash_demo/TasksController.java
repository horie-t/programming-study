package com.t_horie.unleash_demo;

import io.getunleash.UnleashContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TasksController {
    private final GetTasksUsecase getTasksUsecase;

    public TasksController(@Qualifier("getTaskServiceOld") GetTasksUsecase getTasksUsecase) {
        this.getTasksUsecase = getTasksUsecase;
    }

    @GetMapping
    public TasksResponse getTasks() {
        return new TasksResponse(getTasksUsecase.getTasks(UnleashContext.builder().build()));
    }
}
