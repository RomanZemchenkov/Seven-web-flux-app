package com.roman.dao.repository;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoContainerInitializer {


    private static final MongoDBContainer mongoContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.4.2")).withExposedPorts(27017);

    @BeforeAll
    static void init(){
        mongoContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry){
        registry.add("spring.data.mongodb.uri",mongoContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.host",mongoContainer::getHost);
        registry.add("spring.data.mongodb.port",() -> mongoContainer.getMappedPort(27017));
    }
}
