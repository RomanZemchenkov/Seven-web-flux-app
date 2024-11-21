package com.roman.service;

import com.roman.dao.entity.Task;
import com.roman.dao.repository.SequenceGenerator;
import com.roman.dao.repository.TaskRepository;
import com.roman.dao.repository.UserMongoRepository;
import com.roman.service.dto.task.CreateTaskDto;
import com.roman.service.dto.task.ShowTaskDto;
import com.roman.service.dto.task.UpdateTaskDto;
import com.roman.service.dto.user.ShowUserDto;
import com.roman.service.mapping.TaskMapper;
import com.roman.service.mapping.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserMongoRepository userMongoRepository;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final SequenceGenerator sequenceGenerator;

    public Mono<ShowTaskDto> create(Mono<CreateTaskDto> dto) {
        return dto.flatMap(createTaskDto ->
                sequenceGenerator.getSequenceNumber(Task.TASK_SEQUENCE)
                        .map(id -> createTask(createTaskDto, id))
                .flatMap(taskRepository::createNewTask)
                .flatMap(this::taskFactory));
    }

    public Flux<ShowTaskDto> findAllTasks(){
        return taskRepository.findAllTasks()
                .flatMap(this::taskFactory);
    }

    public Mono<ShowTaskDto> findTaskById(String id){
        return taskRepository
                .findTaskById(id)
                .flatMap(this::taskFactory);

    }

    public Mono<ShowTaskDto> updateTask(Mono<UpdateTaskDto> dto, String taskId){
        return dto.flatMap(task -> {
            return taskRepository
                    .findTaskById(taskId)
                    .switchIfEmpty(Mono.error(new RuntimeException("Задачи не существует")))
                    .flatMap(ex -> {
                        Query query = new Query().addCriteria(Criteria.where("id").is(taskId));
                        Update update = new Update();
                        if(task.getName() != null) update.set("name",task.getName());
                        if(task.getDescription() != null) update.set("description",task.getDescription());
                        if(task.getAssigneeId() != null) {
                            return userMongoRepository.findById(task.getAssigneeId())
                                    .switchIfEmpty(Mono.error(new RuntimeException("Исполнитель не существует")))
                                    .flatMap(user -> {
                                        update.set("assigneeId", user.getId());
                                        update.set("updatedAt", Instant.now().toString());
                                        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
                                        return taskRepository.updateTask(query, update, options);
                                    });
                        }
                        update.set("updatedAt", Instant.now().toString());

                        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
                        return taskRepository.updateTask(query, update, options);
                    });
        })
                .flatMap(this::taskFactory);
    }

    public Mono<ShowTaskDto> addNewObserver(String taskId, String observerId){
        return userMongoRepository
                .findById(observerId)
                .switchIfEmpty(Mono.error(new RuntimeException("Наблюдатель не существует")))
                .flatMap(user -> {
                    return taskRepository.findTaskById(taskId)
                            .switchIfEmpty(Mono.error(new RuntimeException("Задачи не существует")))
                            .flatMap(task -> {
                                Query query = new Query().addCriteria(Criteria.where("id").is(taskId));
                                Update update = new Update();
                                update.addToSet("observerIds", observerId);

                                FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
                                return taskRepository.updateTask(query, update, options);
                            });
                })
                .flatMap(this::taskFactory);
    }

    public Mono<Void> deleteTask(String taskId){
        return taskRepository
                .findTaskById(taskId)
                .switchIfEmpty(Mono.error(new RuntimeException("Задачи не существует.")))
                .flatMap(taskRepository::deleteTask)
                .then();
    }


    private Task createTask(CreateTaskDto dto, String id){
            Task task = taskMapper.mapToTask(dto);
            task.setId(id);
            return task;
    }


    private Mono<ShowTaskDto> taskFactory(Task task){
        String authorId = task.getAuthorId();
        String assigneeId = task.getAssigneeId();
        Set<String> observerIds = task.getObserverIds() == null ? Collections.emptySet() : task.getObserverIds();

        Mono<ShowUserDto> author = userMongoRepository.findById(authorId).map(userMapper::mapToShow);
        Mono<ShowUserDto> assignee = userMongoRepository.findById(assigneeId).map(userMapper::mapToShow);
        Flux<ShowUserDto> observers = Flux.fromIterable(observerIds)
                .flatMap(userMongoRepository::findById)
                .map(userMapper::mapToShow);

        return Mono.zip(author, assignee, observers.collectList())
                .map(tuple -> {
                    ShowUserDto aut = tuple.getT1();
                    ShowUserDto ass = tuple.getT2();
                    List<ShowUserDto> obs = tuple.getT3();

                    return taskMapper.mapToShow(task, aut ,ass, obs);
                });
    }
}
