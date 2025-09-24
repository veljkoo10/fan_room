package com.example.fan_room.dto.Response;


import com.example.fan_room.model.Reservation;
import com.example.fan_room.model.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ReservationResponse {

    private UUID id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private String username;
    private String sportName;
    private UUID userId;
    private UUID sportId;
    private List<String> participants;
    private Boolean openForJoin;
}
