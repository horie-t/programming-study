package com.t_horie.unleash_demo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskDue extends Task {
    private String dueDateTime;

    public TaskDue(String id, String title, String description, String dueDateTime) {
        super(id, title, description);
        this.dueDateTime = dueDateTime;
    }
}
