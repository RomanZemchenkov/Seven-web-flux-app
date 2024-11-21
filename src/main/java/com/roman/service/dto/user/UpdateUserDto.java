package com.roman.service.dto.user;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UpdateUserDto {

    private final String id;
    private final String username;
    private final String email;

    public UpdateUserDto(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
}
