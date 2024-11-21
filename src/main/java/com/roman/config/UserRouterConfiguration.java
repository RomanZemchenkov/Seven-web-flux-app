package com.roman.config;

import com.roman.service.UserService;
import com.roman.web.controller.UserHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class UserRouterConfiguration {

    @Bean
    public RouterFunction<ServerResponse> userRoute(@Qualifier("userHandler") UserHandler userHandler){
        return RouterFunctions
                .nest(RequestPredicates.path("/api/users"),
                        RouterFunctions.route()
                                .GET("",userHandler::findAll)
                                .POST("",userHandler::create)
                                .GET("/{id}",userHandler::findById)
                                .PATCH("",userHandler::update)
                                .DELETE("/{id}",userHandler::delete)
                                .build()
                        );
    }


    @Bean("userHandler")
    public UserHandler userHandler(UserService userService){
        return new UserHandler(userService);
    }
}
