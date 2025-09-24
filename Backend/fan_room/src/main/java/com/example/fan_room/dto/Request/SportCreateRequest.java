package com.example.fan_room.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SportCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @Min(value = 0, message = "Player count cannot be negative")
    private Integer playerCount = 0;
}
