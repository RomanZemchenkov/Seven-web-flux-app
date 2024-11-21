package com.roman.service.dto.task;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UpdateTaskDto {

    private final String name;
    private final String description;
    private final String assigneeId;

    public UpdateTaskDto(String name, String description, String assigneeId) {
        this.name = name;
        this.description = description;
        this.assigneeId = assigneeId;
    }
}
