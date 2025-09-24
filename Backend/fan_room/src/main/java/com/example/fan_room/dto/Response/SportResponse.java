package com.example.fan_room.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportResponse {
    private UUID id;
    private String name;
    private String description;
    private int playerCount;
    private String message;

}
