package com.example.fan_room.dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenResponse {
    private String token;
    private long expiresIn;
}
