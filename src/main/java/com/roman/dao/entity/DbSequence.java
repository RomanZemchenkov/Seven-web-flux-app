package com.roman.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "db_sequence")
@Getter
@Setter
@ToString
public class DbSequence {

    @Id
    private String id;
    private int seq;

    public DbSequence(){}

    public DbSequence(int seq) {
        this.seq = seq;
    }
}
