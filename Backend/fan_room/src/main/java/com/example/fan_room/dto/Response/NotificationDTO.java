package com.example.fan_room.dto.Response;

import com.example.fan_room.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private String message;
    private boolean seen;
    private LocalDateTime timestamp;
    private UUID userId;

    public NotificationDTO(Notification notification) {
        this.message = notification.getMessage();
        this.seen = notification.isSeen();
        this.timestamp = notification.getTimestamp();
        this.userId = notification.getUser().getId();
    }
}
