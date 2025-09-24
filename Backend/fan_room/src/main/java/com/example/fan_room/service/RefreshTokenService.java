package com.example.fan_room.service;

import com.example.fan_room.model.RefreshToken;
import com.example.fan_room.model.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
    boolean isValid(RefreshToken token);
}
