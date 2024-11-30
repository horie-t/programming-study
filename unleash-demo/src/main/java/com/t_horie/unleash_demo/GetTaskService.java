package com.t_horie.unleash_demo;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetTaskService implements GetTasksUsecase {
    @Override
    public List<Task> getTasks() {
        // 説明を簡略化するため固定のデータを返しています。
        return List.of(
                new Task("1", "task1", "description1"),
                new Task("2", "task2", "description2"),
                new Task("3", "task3", "description3")
        );
    }
}
