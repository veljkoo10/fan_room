package com.example.fan_room.exception;

import org.springframework.http.HttpStatus;

public class MaxParticipantsReachedException extends RuntimeException {
    private final HttpStatus status;

    public MaxParticipantsReachedException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
