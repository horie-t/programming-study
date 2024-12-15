package com.t_horie.unleash_demo;

import io.getunleash.UnleashContext;
import org.unleash.features.annotation.Toggle;

import java.util.List;

public interface GetTasksUsecase {
    @Toggle(name = "support-due-date-time", alterBean = "getTasksServiceNew")
    List<Task> getTasks(UnleashContext context);
}
