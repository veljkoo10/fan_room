package com.example.fan_room.dto.Response;

import com.example.fan_room.model.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RegisterResponse {
    private UUID id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private UserRole role;
}
