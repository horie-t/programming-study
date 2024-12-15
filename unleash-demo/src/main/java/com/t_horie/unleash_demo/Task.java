package com.t_horie.unleash_demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Task {
    private String id;
    private String title;
    private String description;
}
