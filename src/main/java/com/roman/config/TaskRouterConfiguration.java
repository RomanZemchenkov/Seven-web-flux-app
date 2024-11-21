package com.roman.config;

import com.roman.service.TaskService;
import com.roman.web.controller.TaskHandler;
import com.roman.web.handler.ErrorHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;

@Configuration
public class TaskRouterConfiguration {

    @Bean
    public RouterFunction<ServerResponse> taskRoute(@Qualifier("taskHandler") TaskHandler taskHandler){
        return RouterFunctions
                .nest(
                        path("/api/tasks"),
                        RouterFunctions.route()
                                .GET(path(""),taskHandler::findAll)
                                .GET(path("/{id}"),taskHandler::findById)
                                .POST(path(""),taskHandler::createNewTask)
                                .PATCH(path("/{id}"),taskHandler::updateTask)
                                .PATCH(path("/{id}/observers"),taskHandler::addNewObserver)
                                .DELETE(path("/{id}"),taskHandler::delete)
                                .build());
    }

    @Bean("taskHandler")
    public TaskHandler taskHandler(TaskService taskService, ErrorHandler errorHandler){
        return new TaskHandler(taskService, errorHandler);
    }
}
