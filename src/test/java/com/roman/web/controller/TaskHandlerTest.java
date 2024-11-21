package com.roman.web.controller;

import com.roman.dao.repository.MongoContainerInitializer;
import com.roman.service.TaskService;
import com.roman.service.UserService;
import com.roman.service.dto.task.CreateTaskDto;
import com.roman.service.dto.task.UpdateTaskDto;
import com.roman.service.dto.user.CreateUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@DirtiesContext
public class TaskHandlerTest extends MongoContainerInitializer {

    private final WebTestClient client;
    private final UserService userService;
    private final TaskService taskService;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public TaskHandlerTest(WebTestClient client, UserService userService, TaskService taskService, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.client = client;
        this.userService = userService;
        this.taskService = taskService;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @BeforeEach
    void setUp(){
        reactiveMongoTemplate.dropCollection("userDoc").subscribe();
        reactiveMongoTemplate.dropCollection("taskDoc").subscribe();
        reactiveMongoTemplate.dropCollection("db_sequence").subscribe();
    }

    @Test
    @DisplayName("Test for /api/tasks POST add new task")
    void addNewTask(){
        userCreateFactory(2);
        CreateTaskDto task = new CreateTaskDto("First task", "Description", "1", "2");
        WebTestClient.ResponseSpec exchange = client.post().uri("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(task), CreateTaskDto.class)
                .exchange();

        exchange.expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1");
    }

    @Test
    @DisplayName("Test for /api/tasks GET find all tasks")
    void findAllTasks(){
        userCreateFactory(3);
        taskCreateFactory(2);

        WebTestClient.ResponseSpec exchange = client.get().uri("/api/tasks").exchange();
        exchange.expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.size()").isEqualTo(2);
    }

    @Test
    @DisplayName("Test for /api/tasks/{id} PATCH update task")
    void updateTaskById(){
        userCreateFactory(3);
        taskCreateFactory(1);

        UpdateTaskDto task = new UpdateTaskDto("New Name", "New description", "3");

        WebTestClient.ResponseSpec exchange = client.patch().uri("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(task), UpdateTaskDto.class)
                .exchange();

        exchange.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.name").isEqualTo("New Name")
                .jsonPath("$.assignee.id").isEqualTo("3");
    }

    @Test
    @DisplayName("Test for /api/tasks/{id} GET find task by id")
    void findTaskById(){
        userCreateFactory(2);
        taskCreateFactory(1);

        WebTestClient.ResponseSpec exchange = client.get().uri("/api/tasks/1").exchange();
        exchange.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.name").isEqualTo("New Task 1");
    }

    @Test
    @DisplayName("Test for /api/tasks/{id}/observers PATCH add observer to task")
    void addObserver(){
        userCreateFactory(3);
        taskCreateFactory(1);

        WebTestClient.ResponseSpec exchange = client.patch().uri("/api/tasks/1/observers")
                .body(Mono.just("3"), String.class)
                .exchange();

        exchange.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.observers.size()").isEqualTo(1);
    }

    @Test
    @DisplayName("Test for /api/tasks/{id} DELETE delete task by id")
    void deleteTaskById(){
        userCreateFactory(2);
        taskCreateFactory(1);

        WebTestClient.ResponseSpec exchange = client.delete().uri("/api/tasks/1").exchange();
        exchange.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Задача удалена");
    }



    private void userCreateFactory(int count) {
        Flux.range(1, count)
                .flatMap(i -> userService.create(Mono.just(new CreateUserDto("Roman" + i, "email" + i))))
                .blockLast();
    }


    private void taskCreateFactory(int count) {
        Flux.range(1, count)
                .flatMap(i -> taskService.create(Mono.just(new CreateTaskDto("New Task " + i, "Description", String.valueOf(i), String.valueOf(i + 1)))))
                .blockLast();
    }
}
