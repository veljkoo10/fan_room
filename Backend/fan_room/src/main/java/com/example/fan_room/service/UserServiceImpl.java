package com.example.fan_room.service;

import com.example.fan_room.dto.Request.PasswordResetRequest;
import com.example.fan_room.dto.Response.UserProfileResponse;
import com.example.fan_room.exception.UserNotAllowed;
import com.example.fan_room.model.User;
import com.example.fan_room.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse getCurrentUserProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotAllowed("User with username " + username + " not found."));

        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public void resetPassword(PasswordResetRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    return new UserNotAllowed("User with username " + username + " not found.");
                });

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new UserNotAllowed("Old password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new UserNotAllowed("New password and confirmation do not match");
        }

        String passwordPattern = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:',.<>/?]).{8,}$";
        if (!request.getNewPassword().matches(passwordPattern)) {
            throw new UserNotAllowed("Password must be at least 8 characters long, include one uppercase letter and one special character");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        System.out.println("Password reset successfully for user: " + username);
    }

    @Override
    public List<String> getAllUsernames() {
        return userRepository.findAllUsernames();
    }

    @Override
    public UUID getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotAllowed("User with username " + username + " not found."));
        return user.getId();
    }

}
