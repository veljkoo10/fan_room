package com.example.fan_room.service;

import com.example.fan_room.dto.Request.PasswordResetRequest;
import com.example.fan_room.dto.Response.UserProfileResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserProfileResponse getCurrentUserProfile();
    void resetPassword(PasswordResetRequest request);
    List<String> getAllUsernames();
    UUID getUserIdByUsername(String username);

}
