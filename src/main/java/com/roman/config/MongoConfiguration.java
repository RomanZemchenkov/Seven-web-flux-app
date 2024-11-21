package com.roman.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoClientFactoryBean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;

@Configuration
public class MongoConfiguration {

    @Value("${spring.data.mongodb.host}")
    private String host;
    @Value("${spring.data.mongodb.port}")
    private String port;


    @Bean(name = "reactiveMongoFactoryBean")
    public ReactiveMongoClientFactoryBean reactiveMongoClientFactoryBean(){
        ReactiveMongoClientFactoryBean mongo = new ReactiveMongoClientFactoryBean();
        mongo.setHost(host);
        mongo.setPort(Integer.parseInt(port));

        return mongo;
    }

    @Bean("mongoDatabase")
    public ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory(){
        try {
            return new SimpleReactiveMongoDatabaseFactory(reactiveMongoClientFactoryBean().getObject(), "user_task");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean(name = "reactiveMongoTemplate")
    public ReactiveMongoTemplate reactiveMongoTemplate(){
        return new ReactiveMongoTemplate(reactiveMongoDatabaseFactory());
    }


}
