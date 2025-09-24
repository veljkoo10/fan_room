package com.example.fan_room.dto.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequest {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
