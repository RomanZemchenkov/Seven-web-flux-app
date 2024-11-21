package com.roman.web;

import lombok.Getter;

@Getter
public class Response {

    private final boolean result;
    private final String message;

    public Response(boolean result, String message) {
        this.result = result;
        this.message = message;
    }
}
