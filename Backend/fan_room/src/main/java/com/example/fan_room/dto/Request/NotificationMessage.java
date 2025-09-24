package com.example.fan_room.dto.Request;


import lombok.Data;
import java.util.UUID;

@Data
public class NotificationMessage {
    private String message;
    private UUID userId;
}