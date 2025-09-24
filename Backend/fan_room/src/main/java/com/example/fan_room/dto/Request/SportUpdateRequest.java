package com.example.fan_room.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class SportUpdateRequest {
    @NotBlank
    private String name;

    private String description;

    @PositiveOrZero
    private Integer playerCount;
}
