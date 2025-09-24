package com.example.fan_room.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SportStatisticsResponse {
    private String sportName;
    private long reservationCount;
}