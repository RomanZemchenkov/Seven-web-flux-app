package com.roman.service.mapping;

import com.roman.dao.entity.Task;
import com.roman.service.dto.task.CreateTaskDto;
import com.roman.service.dto.task.ShowTaskDto;
import com.roman.service.dto.user.ShowUserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "createdAt", expression = "java(mapToInstant())")
    @Mapping(target = "updatedAt", expression = "java(mapToInstant())")
    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "assigneeId", source = "assigneeId")
    Task mapToTask(CreateTaskDto createTaskDto);

    default Instant mapToInstant(){
        return Instant.now();
    }

    @Mapping(target = "id", source = "task.id")
    @Mapping(target = "name", source = "task.name")
    @Mapping(target = "description", source = "task.description")
    @Mapping(target = "createdAt", expression = "java(mapToStringInstant(task.getCreatedAt()))")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "assignee", source = "assignee")
    @Mapping(target = "observers", source = "observers")
    ShowTaskDto mapToShow(Task task, ShowUserDto author, ShowUserDto assignee, List<ShowUserDto> observers);

    default String mapToStringInstant(Instant date){
        return date.toString();
    }
}
