package com.example.fan_room.repository;

import com.example.fan_room.model.Notification;
import com.example.fan_room.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByUserOrderByTimestampDesc(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.seen = true WHERE n.user = :user AND n.seen = false")
    int markAllAsSeenByUser(User user);
}
