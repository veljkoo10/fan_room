package com.example.fan_room.service;

import com.example.fan_room.dto.Response.NotificationDTO;
import com.example.fan_room.mapper.NotificationMapper;
import com.example.fan_room.model.Notification;
import com.example.fan_room.model.User;
import com.example.fan_room.repository.NotificationRepository;
import com.example.fan_room.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationMapper notificationMapper;


    public NotificationDTO sendNotification(String message, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        Notification notification = Notification.builder()
                .message(message)
                .user(user)
                .seen(false)
                .timestamp(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        NotificationDTO dto = notificationMapper.toDto(saved);

        messagingTemplate.convertAndSend("/topic/notifications/" + userId, dto);

        return dto;
    }

    public List<Notification> getAllByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        return notificationRepository.findAllByUserOrderByTimestampDesc(user);
    }


    public Notification markAsSeen(UUID notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id " + notificationId));
        n.setSeen(true);
        return notificationRepository.save(n);
    }

    @Transactional
    public void markAllAsSeen(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        notificationRepository.markAllAsSeenByUser(user);
    }
}
