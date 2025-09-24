package com.example.fan_room.service;

import com.example.fan_room.dto.Request.LoginRequest;
import com.example.fan_room.dto.Request.RefreshTokenRequest;
import com.example.fan_room.dto.Request.RegisterRequest;
import com.example.fan_room.dto.Response.LoginResponse;
import com.example.fan_room.dto.Response.RefreshTokenResponse;
import com.example.fan_room.dto.Response.RegisterResponse;
import com.example.fan_room.dto.Response.ResetPasswordResponse;
import com.example.fan_room.model.User;

import java.util.Map;

public interface AuthenticationService {
    RegisterResponse signup(RegisterRequest input);
    LoginResponse login(LoginRequest input);
    void sendResetEmail(String email);
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
    void logout(String token);
    ResetPasswordResponse resetPasswordWithValidation(String token, String newPassword);

}
