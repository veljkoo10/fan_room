package com.example.fan_room.dto.Response;

import com.example.fan_room.model.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserProfileResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private UserRole role;
}
