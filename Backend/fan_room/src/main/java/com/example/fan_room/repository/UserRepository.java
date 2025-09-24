package com.example.fan_room.repository;

import com.example.fan_room.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findAllByUsernameIn(List<String> usernames);
    @Query("SELECT u.username FROM User u")
    List<String> findAllUsernames();
}