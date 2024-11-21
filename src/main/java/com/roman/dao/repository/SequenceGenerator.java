package com.roman.dao.repository;

import com.roman.dao.entity.DbSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
public class SequenceGenerator {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    public SequenceGenerator(
            @Qualifier("reactiveMongoTemplate") ReactiveMongoTemplate reactiveMongoTemplate
    ) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<String> getSequenceNumber(String sequenceName){
        Query query = Query.query(Criteria.where("id").is(sequenceName));
        Update update = new Update().inc("seq", 1);
        Mono<DbSequence> dbSequence = reactiveMongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                DbSequence.class
        );

        return dbSequence.map(seq -> seq.getSeq() == 0 ? "1" : String.valueOf(seq.getSeq()));
    }

}
