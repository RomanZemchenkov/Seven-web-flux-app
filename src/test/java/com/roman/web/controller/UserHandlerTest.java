package com.roman.web.controller;

import com.roman.dao.repository.MongoContainerInitializer;
import com.roman.service.UserService;
import com.roman.service.dto.user.CreateUserDto;
import com.roman.service.dto.user.UpdateUserDto;
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
public class UserHandlerTest extends MongoContainerInitializer {

    private final WebTestClient webTestClient;
    private final UserService userService;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public UserHandlerTest(WebTestClient webTestClient, UserService userService, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.webTestClient = webTestClient;
        this.userService = userService;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @BeforeEach
    void setUp(){
        reactiveMongoTemplate.dropCollection("userDoc").subscribe();
        reactiveMongoTemplate.dropCollection("db_sequence").subscribe();
    }

    @Test
    @DisplayName("Test for /api/users GET method")
    void findAllUsersMethod(){
        userCreateFactory(2);
        WebTestClient.ResponseSpec exchange = webTestClient
                .get()
                .uri("/api/users")
                .exchange();

        exchange
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.size()").isEqualTo(2);
    }

    @Test
    @DisplayName("Test for /api/users POST method")
    void createNewUserMethod(){
        userCreateFactory(1);
        Mono<CreateUserDto> newUser = Mono.just(new CreateUserDto("Kollega", "unknownuser"));

        WebTestClient.ResponseSpec exchange = webTestClient
                .post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(newUser, CreateUserDto.class)
                .exchange();

        exchange
                .expectStatus()
                .isCreated()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$.id").isEqualTo(2)
                .jsonPath("$.username").isEqualTo("Kollega")
                .jsonPath("$.email").isEqualTo("unknownuser");
    }

    @Test
    @DisplayName("Test for /api/users/{id} GET method")
    void findByIdMethod(){
        userCreateFactory(1);
        WebTestClient.ResponseSpec exchange = webTestClient
                .get()
                .uri("/api/users/1")
                .exchange();

        exchange
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.username").isEqualTo("Roman1")
                .jsonPath("$.email").isEqualTo("email1");

    }

    @Test
    @DisplayName("Test for /api/users PATCH method")
    void updateUserMethod(){
        userCreateFactory(1);
        Mono<UpdateUserDto> updateUser = Mono.just(new UpdateUserDto("1", "newUsername", "newEmail"));

        WebTestClient.ResponseSpec exchange = webTestClient
                .patch()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateUser, UpdateUserDto.class)
                .exchange();

        exchange
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.username").isEqualTo("newUsername")
                .jsonPath("$.email").isEqualTo("newEmail");
    }

    @Test
    @DisplayName("Test for /api/delete/{id} DELETE method")
    void deleteUserMethod(){
        userCreateFactory(1);

        WebTestClient.ResponseSpec exchange = webTestClient
                .delete()
                .uri("/api/users/1")
                .exchange();

        exchange
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$").isEqualTo("Пользователь был удалён");
    }

    private void userCreateFactory(int count) {
        Flux.range(1, count)
                .flatMap(i -> userService.create(Mono.just(new CreateUserDto("Roman" + i, "email" + i))))
                .blockLast();
    }

}
