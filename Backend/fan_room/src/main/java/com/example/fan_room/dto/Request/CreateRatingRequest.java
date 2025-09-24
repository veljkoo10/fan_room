package com.example.fan_room.dto.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateRatingRequest {
    @Min(1)
    private int hygiene;

    @Min(1)
    private int equipment;

    @Min(1)
    private int atmosphere;

    private String comment;
}
