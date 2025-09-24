package com.example.fan_room.controller;

import com.example.fan_room.dto.Request.PasswordResetRequest;
import com.example.fan_room.dto.Response.UserProfileResponse;
import com.example.fan_room.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public UserProfileResponse getMyProfile() {
        return userService.getCurrentUserProfile();
    }

    @PutMapping("/reset-password")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void resetPassword(@RequestBody PasswordResetRequest request) {
        userService.resetPassword(request);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<String> getAllUsernames() {
        return userService.getAllUsernames();
    }

    @GetMapping("/id/{username}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public UUID getUserIdByUsername(@PathVariable String username) {
        return userService.getUserIdByUsername(username);
    }

}
