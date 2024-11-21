package com.roman.web.controller;

import com.roman.service.TaskService;
import com.roman.service.dto.task.CreateTaskDto;
import com.roman.service.dto.task.ShowTaskDto;
import com.roman.service.dto.task.UpdateTaskDto;
import com.roman.web.Response;
import com.roman.web.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class TaskHandler {

    private final TaskService taskService;
    private final ErrorHandler errorHandler;

    public Mono<ServerResponse> createNewTask(ServerRequest request){
        Mono<CreateTaskDto> newTask = request.bodyToMono(CreateTaskDto.class);
        return taskService.create(newTask)
                .flatMap(task -> ServerResponse
                            .status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(task))
                .onErrorResume(errorHandler::taskErrorHandler);
    }


    public Mono<ServerResponse> findById(ServerRequest request){
        String taskId = request.pathVariable("id");
        return taskService
                .findTaskById(taskId)
                .flatMap(task -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(task))
                .onErrorResume(errorHandler::taskErrorHandler);
    }

    public Mono<ServerResponse> findAll(ServerRequest request){
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskService.findAllTasks(), ShowTaskDto.class);
    }

    public Mono<ServerResponse> updateTask(ServerRequest request){
        String taskId = request.pathVariable("id");
        Mono<UpdateTaskDto> updateTask = request.bodyToMono(UpdateTaskDto.class);
        return taskService
                .updateTask(updateTask,taskId)
                .flatMap(task -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(task))
                .onErrorResume(errorHandler::taskErrorHandler);
    }

    public Mono<ServerResponse> addNewObserver(ServerRequest request){
        String taskId = request.pathVariable("id");
        Mono<String> observerId = request.bodyToMono(String.class);
        return observerId
                .flatMap(id -> taskService.addNewObserver(taskId,id))
                .flatMap(task -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(task))
                .onErrorResume(errorHandler::taskErrorHandler);
    }

    public Mono<ServerResponse> delete(ServerRequest request){
        String taskId = request.pathVariable("id");
        return taskService
                .deleteTask(taskId)
                .then(ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(new Response(true,"Задача удалена")))
                .onErrorResume(errorHandler::taskErrorHandler);
    }
}
