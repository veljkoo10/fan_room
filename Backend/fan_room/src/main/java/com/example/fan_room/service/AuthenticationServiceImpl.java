package com.example.fan_room.service;

import com.example.fan_room.dto.Request.LoginRequest;
import com.example.fan_room.dto.Request.RefreshTokenRequest;
import com.example.fan_room.dto.Request.RegisterRequest;
import com.example.fan_room.dto.Response.LoginResponse;
import com.example.fan_room.dto.Response.RefreshTokenResponse;
import com.example.fan_room.dto.Response.RegisterResponse;
import com.example.fan_room.dto.Response.ResetPasswordResponse;
import com.example.fan_room.exception.UserNotAllowed;
import com.example.fan_room.model.PasswordResetToken;
import com.example.fan_room.model.RefreshToken;
import com.example.fan_room.model.User;
import com.example.fan_room.model.UserRole;
import com.example.fan_room.repository.PasswordResetTokenRepository;
import com.example.fan_room.repository.RefreshTokenRepository;
import com.example.fan_room.repository.UserRepository;
import com.example.fan_room.security.JwtService;
import com.example.fan_room.sender.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailSender emailSender;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public RegisterResponse signup(RegisterRequest input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new UserNotAllowed("Email already in use");
        }

        if (userRepository.existsByUsername(input.getUsername())) {
            throw new UserNotAllowed("Username already in use");
        }

        String password = input.getPassword();
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$";
        if (password == null || !password.matches(passwordRegex)) {
            throw new UserNotAllowed(
                    "Password must be at least 8 characters long, contain one uppercase letter, one lowercase letter, and one special character."
            );
        }

        User user = User.builder()
                .email(input.getEmail())
                .password(passwordEncoder.encode(password))
                .username(input.getUsername())
                .firstName(input.getFirstName())
                .lastName(input.getLastName())
                .role(UserRole.USER)
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole())
                .build();
    }



    @Override
    public LoginResponse login(LoginRequest input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new UserNotAllowed("User not found"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return LoginResponse.builder()
                .token(accessToken)
                .expiresIn(jwtService.getExpirationTime())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Override
    public void sendResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotAllowed("No user found with this email"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(passwordResetToken);

        String subject = "Password reset";
        String resetUrl = "http://localhost:4200/reset-password?token=" + token;
        String body = "To reset your password, click the link below:\n" + resetUrl;

        emailSender.sendEmail(user.getEmail(), subject, body);
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UserNotAllowed("Invalid refresh token"));

        if (!refreshTokenService.isValid(refreshToken)) {
            throw new UserNotAllowed("Refresh token expired");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);

        return RefreshTokenResponse.builder()
                .token(newAccessToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }

    @Override
    public void logout(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UserNotAllowed("Invalid refresh token"));
        refreshTokenRepository.delete(refreshToken);
    }

    @Override
    public ResetPasswordResponse resetPasswordWithValidation(String token, String newPassword) {
        validatePasswordStrength(newPassword);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().before(new Date())) {
            throw new UserNotAllowed("Invalid or expired token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        return ResetPasswordResponse.builder()
                .message("Password has been reset successfully.")
                .userId(user.getId())
                .build();
    }

    private void validatePasswordStrength(String password) {
        if (password == null || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$")) {
            throw new IllegalArgumentException("Password must be at least 8 characters, include uppercase, lowercase, number, and special character.");
        }
    }

}
