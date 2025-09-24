package com.example.fan_room.controller;

import com.example.fan_room.dto.Response.NotificationDTO;
import com.example.fan_room.dto.Request.NotificationMessage;
import com.example.fan_room.model.Notification;
import com.example.fan_room.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/sendMessage")
    public void sendMessage(NotificationMessage message) {
        notificationService.sendNotification(message.getMessage(), message.getUserId());
    }

    @GetMapping("/{userId}")
    public List<NotificationDTO> getUserNotifications(@PathVariable UUID userId) {
        List<Notification> notifications = notificationService.getAllByUser(userId);
        return notifications.stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
    }

    @PutMapping("/{userId}/seen")
    public void markAllAsSeen(@PathVariable UUID userId) {
        notificationService.markAllAsSeen(userId);
    }

}
