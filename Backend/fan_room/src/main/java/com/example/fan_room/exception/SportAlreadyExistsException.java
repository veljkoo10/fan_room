package com.example.fan_room.exception;

public class SportAlreadyExistsException extends RuntimeException {
    public SportAlreadyExistsException(String message) {
        super(message);
    }
}