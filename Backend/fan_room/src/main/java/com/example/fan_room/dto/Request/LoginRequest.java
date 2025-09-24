package com.example.fan_room.dto.Request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
