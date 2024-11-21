package com.roman.service.dto.user;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CreateUserDto {

    private final String username;
    private final String email;

    public CreateUserDto(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
