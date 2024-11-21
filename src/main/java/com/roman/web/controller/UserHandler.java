package com.roman.web.controller;

import com.roman.service.UserService;
import com.roman.service.dto.user.CreateUserDto;
import com.roman.service.dto.user.ShowUserDto;
import com.roman.service.dto.user.UpdateUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserHandler {

    private final UserService userService;

    public Mono<ServerResponse> findAll(ServerRequest request){
        return ServerResponse
                .ok()
                .body(userService.findAll(), ShowUserDto.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request){
        String userId = request.pathVariable("id");

        return userService.findById(userId)
                .flatMap(us -> ServerResponse.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(us))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> create(ServerRequest request){
        Mono<CreateUserDto> dto = request.bodyToMono(CreateUserDto.class);
        return userService.create(dto)
                .flatMap(us -> ServerResponse.status(201)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(us))
                .switchIfEmpty(ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue("Текст ошибки"));
    }

    public Mono<ServerResponse> update(ServerRequest request){
        Mono<UpdateUserDto> updateDto = request.bodyToMono(UpdateUserDto.class);

        return userService.update(updateDto)
                .flatMap(us ->
                    ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(us))
                .switchIfEmpty(ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue("Текст ошибки"));
    }

    public Mono<ServerResponse> delete(ServerRequest request){
        String userId = request.pathVariable("id");

        return userService.delete(userId)
                .flatMap(res ->
                    ServerResponse.ok()
                            .bodyValue("Пользователь был удалён"))
                .switchIfEmpty(ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue("Текст ошибки"));
    }

}
