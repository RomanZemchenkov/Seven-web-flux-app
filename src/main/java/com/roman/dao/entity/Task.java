package com.roman.dao.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "taskDoc")
@Getter
@Setter
@ToString(exclude = {"observer"})
@EqualsAndHashCode
public class Task implements BaseEntity<String> {

    @ReadOnlyProperty
    public static final String COLLECTION_NAME = "taskDoc";
    @ReadOnlyProperty
    public static final String TASK_SEQUENCE = "task_sequence";


    @Id
    private String id;

    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private TaskStatus status;
    private String authorId;
    private String assigneeId;
    private Set<String> observerIds = new HashSet<>();

    @Transient
    private User author;
    @Transient
    private User assignee;
    @Transient
    private Set<User> observer;

    public Task(){}


}
