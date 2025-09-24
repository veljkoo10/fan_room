package com.example.fan_room.controller;

import com.example.fan_room.dto.Request.LoginRequest;
import com.example.fan_room.dto.Request.LogoutRequest;
import com.example.fan_room.dto.Request.RefreshTokenRequest;
import com.example.fan_room.dto.Request.RegisterRequest;
import com.example.fan_room.dto.Response.LoginResponse;
import com.example.fan_room.dto.Response.RefreshTokenResponse;
import com.example.fan_room.dto.Response.RegisterResponse;
import com.example.fan_room.dto.Response.ResetPasswordResponse;
import com.example.fan_room.model.User;
import com.example.fan_room.sender.EmailSender;
import com.example.fan_room.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final EmailSender emailSender;

    @PostMapping("/signup")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerUserDto) {
        return ResponseEntity.ok(authenticationService.signup(registerUserDto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginUserDto) {
        return ResponseEntity.ok(authenticationService.login(loginUserDto));
    }

    @GetMapping("/sendEmail")
    public ResponseEntity<String> sendEmail(@RequestParam("email") String email) {
        authenticationService.sendResetEmail(email);
        return ResponseEntity.ok("Email sent to: " + email);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @RequestParam("token") String token,
            @RequestBody Map<String, String> payload) {

        ResetPasswordResponse response = authenticationService.resetPasswordWithValidation(
                token, payload.get("password")
        );

        return ResponseEntity.ok(response);
    }

}
