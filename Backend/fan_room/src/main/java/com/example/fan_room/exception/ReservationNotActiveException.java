package com.example.fan_room.exception;

import org.springframework.http.HttpStatus;

public class ReservationNotActiveException extends RuntimeException {
    private final HttpStatus status;

    public ReservationNotActiveException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
