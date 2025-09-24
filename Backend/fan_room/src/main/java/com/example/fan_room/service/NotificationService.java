package com.example.fan_room.service;

import com.example.fan_room.dto.Response.NotificationDTO;
import com.example.fan_room.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationDTO sendNotification(String message, UUID userId);

    List<Notification> getAllByUser(UUID userId);

    Notification markAsSeen(UUID notificationId);

    void markAllAsSeen(UUID userId);
}