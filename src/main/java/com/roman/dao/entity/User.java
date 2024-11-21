package com.roman.dao.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@EqualsAndHashCode(of = {"id","username"})
@ToString
@Document(collection = "userDoc")
public class User implements BaseEntity<String>{

    @ReadOnlyProperty
    public static final String SEQUENCE_NAME = "user_sequence";

    @Id
    private String id;

    private String username;

    private String email;

    public User(){}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
