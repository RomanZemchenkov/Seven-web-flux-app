package com.roman.service.dto.task;

import com.roman.service.dto.user.ShowUserDto;
import lombok.Getter;
import lombok.ToString;

import java.util.List;


@Getter
@ToString
public class ShowTaskDto {

    private final String id;
    private final String name;
    private final String description;
    private final String createdAt;
    private final String updatedAt;
    private final String status;
    private final ShowUserDto author;
    private final ShowUserDto assignee;
    private final List<ShowUserDto> observers;

    public ShowTaskDto(String id, String name, String description, String createdAt, String updatedAt, String status, ShowUserDto author, ShowUserDto assignee, List<ShowUserDto> observers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.author = author;
        this.assignee = assignee;
        this.observers = observers;
    }
}
