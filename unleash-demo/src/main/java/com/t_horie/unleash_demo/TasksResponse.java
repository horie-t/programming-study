package com.t_horie.unleash_demo;

import lombok.Data;

import java.util.List;

@Data
public class TasksResponse {
    private final List<Task> results;
}
