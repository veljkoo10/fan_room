package com.example.fan_room.mapper;

import com.example.fan_room.dto.Response.NotificationDTO;
import com.example.fan_room.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDTO toDto(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setMessage(notification.getMessage());
        dto.setSeen(notification.isSeen());
        dto.setTimestamp(notification.getTimestamp());
        dto.setUserId(notification.getUser().getId());
        return dto;
    }
}
