package com.example.fan_room.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateReservationRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private UUID sportId;
    private List<String> participants;
    private boolean openForJoin;
}