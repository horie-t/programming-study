package com.t_horie.unleash_demo;

import io.getunleash.UnleashContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("getTasksServiceNew")
public class GetTaskServiceDue implements GetTasksUsecase {
    public List<Task> getTasks(UnleashContext context) {
        // 説明を簡略化するため固定のデータを返しています。
        return List.of(
                new TaskDue("1", "task1", "description1", "2021-01-01T00:00:00Z"),
                new TaskDue("2", "task2", "description2", "2021-01-02T00:00:00Z"),
                new TaskDue("3", "task3", "description3", "2021-01-03T00:00:00Z")
        );
    }
}
