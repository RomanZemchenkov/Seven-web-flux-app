package com.roman.dao.repository;

import com.roman.dao.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
public class UserMongoRepositoryTest extends MongoContainerInitializer {

    private final UserMongoRepository userMongoRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public UserMongoRepositoryTest(UserMongoRepository userMongoRepository, ReactiveMongoTemplate mongoTemplate) {
        this.userMongoRepository = userMongoRepository;
        this.reactiveMongoTemplate = mongoTemplate;
    }

    @BeforeEach
    void setUp(){
        reactiveMongoTemplate.dropCollection("userDoc").subscribe();
        reactiveMongoTemplate.dropCollection("db_sequence").subscribe();
    }

    @Test
    @DisplayName("Testing the save to mongo method")
    void saveTest() {
        User user = new User("Kollega", "unknownuzer@yandex.ru");
        user.setId("1");
        Mono<User> save = userMongoRepository.save(user);

        StepVerifier.create(save)
                .expectNext(user)
                .verifyComplete();

        Mono<User> findUser = reactiveMongoTemplate.findById(user.getId(), User.class, "userDoc");

        StepVerifier.create(findUser)
                .expectNext(user)
                .verifyComplete();


    }
}
