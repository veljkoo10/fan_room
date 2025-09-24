package com.example.fan_room.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class BadRequestException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    @Override
    public String getMessage() {
        return message;
    }
}
