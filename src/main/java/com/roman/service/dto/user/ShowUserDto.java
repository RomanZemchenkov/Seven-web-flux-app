package com.roman.service.dto.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class ShowUserDto {

    private final int id;
    private final String username;
    private final String email;

    public ShowUserDto(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
}
