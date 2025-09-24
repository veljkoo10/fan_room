package com.example.fan_room.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {
    public String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
