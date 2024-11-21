package com.roman.service;

import com.roman.dao.entity.DbSequence;
import com.roman.dao.entity.User;
import com.roman.dao.repository.MongoContainerInitializer;
import com.roman.service.dto.user.CreateUserDto;
import com.roman.service.dto.user.ShowUserDto;
import com.roman.service.dto.user.UpdateUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
//@DirtiesContext
public class UserServiceTest extends MongoContainerInitializer {

    private final UserService userService;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public UserServiceTest(UserService userService, ReactiveMongoTemplate reactiveMongoTemplate) {
        this.userService = userService;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @BeforeEach
    void setUp(){
        reactiveMongoTemplate.dropCollection("userDoc").subscribe();
        reactiveMongoTemplate.dropCollection("db_sequence").subscribe();
    }

    @Test
    @DisplayName("Test for create user method")
    void createMethodTest(){
        CreateUserDto dto1 = new CreateUserDto("username1", "email1");
        CreateUserDto dto2 = new CreateUserDto("username2", "email2");
        Mono<CreateUserDto> futureUser1 = Mono.just(dto1);
        Mono<CreateUserDto> futureUser2 = Mono.just(dto2);
        Mono<ShowUserDto> savedUser1 = userService.create(futureUser1).log();
        Mono<ShowUserDto> savedUser2 = userService.create(futureUser2).log();

        StepVerifier.create(savedUser1)
                .consumeNextWith(us -> {
                    assertThat(us.getId()).isNotEqualTo(0);
                    assertThat(us.getUsername()).isEqualTo("username1");
                })
                .expectComplete()
                .verify();

        StepVerifier.create(savedUser2)
                .consumeNextWith(us -> {
                    assertThat(us.getId()).isNotEqualTo(0);
                    assertThat(us.getUsername()).isEqualTo("username2");
                })
                .expectComplete()
                .verify();


        Mono<Long> count = reactiveMongoTemplate.count(Query.query(Criteria.where("_id").is(User.SEQUENCE_NAME)), "db_sequence");
        Mono<DbSequence> one = reactiveMongoTemplate.findOne(Query.query(Criteria.where("_id").is(User.SEQUENCE_NAME)), DbSequence.class, "db_sequence");

        StepVerifier.create(count)
                .expectNextCount(1L)
                .verifyComplete();


        StepVerifier.create(one)
                .consumeNextWith(c -> assertThat(c).isNotNull())
                .verifyComplete();
    }

    @Test
    @DisplayName("Test for find all method")
    void findAllMethod(){
        Flux<ShowUserDto> all = userService.findAll();

        StepVerifier.create(all)
                .expectNextCount(0)
                .verifyComplete();

        Mono<ShowUserDto> savedUser1 = userService.create(Mono.just(new CreateUserDto("username1", "email1")));
        Mono<ShowUserDto> savedUser2 = userService.create(Mono.just(new CreateUserDto("username2", "email2")));

        StepVerifier.create(savedUser1)
                .expectNextMatches(user -> user.getUsername().equals("username1") && user.getEmail().equals("email1"))
                .verifyComplete();

        StepVerifier.create(savedUser2)
                .expectNextMatches(user -> user.getUsername().equals("username2") && user.getEmail().equals("email2"))
                .verifyComplete();

        StepVerifier.create(all)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test for find user by id method")
    void findById(){
        Mono<ShowUserDto> mayBeUser = userService.findById("1");

        StepVerifier.create(mayBeUser)
                .expectError(RuntimeException.class)
                .verify();

        Mono<ShowUserDto> savedUser1 = userService.create(Mono.just(new CreateUserDto("username1", "email1")));

        StepVerifier.create(savedUser1)
                .expectNextMatches(user -> user.getUsername().equals("username1") && user.getEmail().equals("email1"))
                .verifyComplete();


        StepVerifier.create(mayBeUser)
                .consumeNextWith(us -> {
                    assertThat(us.getUsername()).isEqualTo("username1");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test for update user method")
    void updateUserMethodTest(){
        Mono<UpdateUserDto> update = Mono.just(new UpdateUserDto("1", "newUsername", "newEmail")).log();
        Mono<ShowUserDto> mayBeCanUpdate = userService.update(update);

        StepVerifier.create(mayBeCanUpdate)
                .expectError(RuntimeException.class)
                .verify();

        Mono<ShowUserDto> savedUser1 = userService.create(Mono.just(new CreateUserDto("username1", "email1")));

        StepVerifier.create(savedUser1)
                .expectNextMatches(user -> user.getUsername().equals("username1") && user.getEmail().equals("email1"))
                .verifyComplete();

        StepVerifier.create(mayBeCanUpdate)
                .expectNext(new ShowUserDto(1,"username1","email1"))
                .verifyComplete();

    }

    @Test
    @DisplayName("Test for delete by id method")
    void deleteByIdMethodTest(){
        Mono<Boolean> mayBeDelete = userService.delete("1");

        StepVerifier.create(mayBeDelete)
                .expectError(RuntimeException.class)
                .verify();

        Mono<ShowUserDto> savedUser1 = userService.create(Mono.just(new CreateUserDto("username1", "email1")));

        StepVerifier.create(savedUser1)
                .expectNextMatches(user -> user.getUsername().equals("username1") && user.getEmail().equals("email1"))
                .verifyComplete();

        StepVerifier.create(mayBeDelete)
                .expectNext(true)
                .verifyComplete();

    }
}
