package com.roman.dao.repository;

import com.mongodb.client.result.DeleteResult;
import com.roman.dao.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class TaskRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    @Autowired
    public TaskRepository(@Qualifier("reactiveMongoTemplate") ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Mono<Task> createNewTask(Task task) {
        return mongoTemplate.insert(task, Task.COLLECTION_NAME);
    }

    public Flux<Task> findAllTasks() {
        return mongoTemplate.findAll(Task.class, Task.COLLECTION_NAME);
    }

    public Mono<Task> findTaskById(String id) {
        return mongoTemplate.findById(id, Task.class, Task.COLLECTION_NAME);
    }

    public Mono<Task> updateTask(Query query, Update update, FindAndModifyOptions options) {
        return mongoTemplate.findAndModify(query, update, options, Task.class, Task.COLLECTION_NAME);
    }

    public Mono<DeleteResult> deleteTask(Task task){
        return mongoTemplate.remove(task, Task.COLLECTION_NAME);
    }

}
