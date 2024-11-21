package com.roman.dao.repository;

import com.roman.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class UserMongoRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private static final String COLLECTION_NAME = "userDoc";
    private static final Logger log = LoggerFactory.getLogger(UserMongoRepository.class);

    @Autowired
    public UserMongoRepository(
            @Qualifier("reactiveMongoTemplate") ReactiveMongoTemplate reactiveMongoTemplate
    ) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<User> save(User user) {
        return reactiveMongoTemplate.insert(user, COLLECTION_NAME);
    }

    public Flux<User> findAllUsers() {
        Flux<User> userFlux = reactiveMongoTemplate.findAll(User.class, COLLECTION_NAME)
                .doOnSubscribe(el -> log.info("Подписка на получение всех пользователей"));
        return userFlux;
    }

    public Mono<User> findById(String id) {
        return reactiveMongoTemplate.findById(id, User.class, COLLECTION_NAME);
    }

    public Mono<User> update(Query query, Update update, FindAndModifyOptions options) {
        return reactiveMongoTemplate.findAndModify(query, update, options, User.class, COLLECTION_NAME);
    }

    public Mono<String> delete(String id) {
        return reactiveMongoTemplate
                .findAndRemove(
                        Query.query(Criteria.where("id").is(id)),
                        User.class,
                        COLLECTION_NAME
                )
                .map(User::getId);
    }
}
