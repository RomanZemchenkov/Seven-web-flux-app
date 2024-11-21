package com.roman.service;

import com.mongodb.reactivestreams.client.MongoDatabase;
import com.roman.dao.entity.Task;
import com.roman.dao.repository.MongoContainerInitializer;
import com.roman.service.dto.task.CreateTaskDto;
import com.roman.service.dto.task.ShowTaskDto;
import com.roman.service.dto.task.UpdateTaskDto;
import com.roman.service.dto.user.CreateUserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TaskServiceIT extends MongoContainerInitializer {

    private final TaskService taskService;
    private final UserService userService;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public TaskServiceIT(TaskService taskService, UserService userService, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.taskService = taskService;

        this.userService = userService;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @BeforeEach
    void setUp(){
        reactiveMongoTemplate.dropCollection("userDoc").subscribe();
        reactiveMongoTemplate.dropCollection("taskDoc").subscribe();
        reactiveMongoTemplate.dropCollection("db_sequence").subscribe();
    }

    @AfterEach
    void clear() {
        reactiveMongoTemplate.getMongoDatabase()
                .flatMapMany(MongoDatabase::listCollectionNames)
                .flatMap(reactiveMongoTemplate::dropCollection)
                .blockLast();
    }

    @Test
    @DisplayName("Test for create new task")
    void createNewTask() {
        userCreateFactory(2);

        CreateTaskDto task = new CreateTaskDto("New Task", "Description", "1", "2");
        Mono<ShowTaskDto> showTask = taskService.create(Mono.just(task));

        StepVerifier.create(showTask)
                .expectNextMatches(show -> show.getClass().equals(ShowTaskDto.class))
                .verifyComplete();
    }

    @Test
    @DisplayName("Test for find all tasks")
    void findAllTasks() {
        userCreateFactory(10);
        taskCreateFactory(9);

        Flux<ShowTaskDto> allTasks = taskService.findAllTasks();

        StepVerifier.create(allTasks)
                .thenAwait(Duration.of(1L, ChronoUnit.SECONDS))
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test for find task by id")
    void findTaskById() {
        userCreateFactory(4);
        taskCreateFactory(3);

        Mono<ShowTaskDto> task = taskService.findTaskById("2");

        StepVerifier.create(task)
                .consumeNextWith(el -> {
                    assertThat(el.getId()).isEqualTo("2");
                    assertThat(el.getName()).isEqualTo("New Task 2");
                    assertThat(el.getAuthor().getId()).isEqualTo(2);
                    assertThat(el.getAssignee().getId()).isEqualTo(3);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test for update task")
    void updateTask() {
        userCreateFactory(3);
        taskCreateFactory(1);
        String taskId = "1";

        Mono<Task> findTask = reactiveMongoTemplate.findById(taskId, Task.class, Task.COLLECTION_NAME);

        StepVerifier.create(findTask)
                .consumeNextWith(t -> {
                    assertThat(t.getAssigneeId()).isEqualTo("2");
                })
                .verifyComplete();

        String name = "Updated task";
        String description = "New description";
        String assigneeId = "3";


        UpdateTaskDto updateTask = new UpdateTaskDto(name, description, assigneeId);
        Mono<ShowTaskDto> task = taskService.updateTask(Mono.just(updateTask), taskId);

        StepVerifier.create(task)
                .consumeNextWith(t -> {
                    assertThat(t.getId()).isEqualTo(taskId);
                    assertThat(t.getName()).isEqualTo(name);
                    assertThat(t.getDescription()).isEqualTo(description);
                    assertThat(t.getAssignee().getId()).isEqualTo(Integer.valueOf(assigneeId));
                })
                .verifyComplete();

        StepVerifier.create(findTask)
                .consumeNextWith(t -> {
                    assertThat(t.getAssigneeId()).isEqualTo(assigneeId);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test for add new observer to task")
    void addNewObserverToTask() {
        userCreateFactory(4);
        taskCreateFactory(1);
        String taskId = "1";
        Mono<Task> findTask = reactiveMongoTemplate.findById(taskId, Task.class, Task.COLLECTION_NAME);

        StepVerifier.create(findTask)
                .consumeNextWith(task -> {
                    assertThat(task.getObserverIds()).hasSize(0);
                    assertThat(task.getAssigneeId()).isEqualTo("2");
                })
                .verifyComplete();

        String observerId1 = "3";
        Mono<ShowTaskDto> firstObserver = taskService.addNewObserver(taskId, observerId1);

        StepVerifier.create(firstObserver)
                .consumeNextWith(task -> {
                    assertThat(task.getObservers()).hasSize(1);
                })
                .verifyComplete();

        StepVerifier.create(findTask)
                .consumeNextWith(task -> {
                    assertThat(task.getObserverIds()).hasSize(1);
                    assertThat(task.getAssigneeId()).isEqualTo("2");
                })
                .verifyComplete();
        String observerId2 = "4";
        Mono<ShowTaskDto> secondObserver = taskService.addNewObserver(taskId, observerId2);

        StepVerifier.create(secondObserver)
                .consumeNextWith(task -> {
                    assertThat(task.getObservers()).hasSize(2);
                })
                .verifyComplete();

        StepVerifier.create(findTask)
                .consumeNextWith(task -> {
                    assertThat(task.getObserverIds()).hasSize(2);
                    assertThat(task.getAssigneeId()).isEqualTo("2");
                })
                .verifyComplete();

    }

    @Test
    @DisplayName("Test for delete task by id")
    void deleteTaskById() {
        userCreateFactory(2);
        taskCreateFactory(1);
        String taskId = "1";

        Mono<Task> existTask = reactiveMongoTemplate.findById(taskId, Task.class, Task.COLLECTION_NAME);

        StepVerifier.create(existTask)
                .consumeNextWith(task -> assertThat(task).isNotNull())
                .verifyComplete();

        Mono<Void> deleteTask = taskService.deleteTask(taskId);

        StepVerifier.create(deleteTask)
                .verifyComplete();

        StepVerifier.create(existTask)
                .expectNextCount(0)
                .verifyComplete();
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
