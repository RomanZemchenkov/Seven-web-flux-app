package com.roman.service.dto.task;

import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class CreateTaskDto {

    private final String name;
    private final String description;
    private final String authorId;
    private final String assigneeId;


    public CreateTaskDto(String name, String description, String authorId, String assigneeId) {
        this.name = name;
        this.description = description;
        this.authorId = authorId;
        this.assigneeId = assigneeId;
    }
}
