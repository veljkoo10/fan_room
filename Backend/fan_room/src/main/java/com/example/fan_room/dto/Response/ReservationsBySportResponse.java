package com.example.fan_room.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationsBySportResponse {
    private String sport;
    private List<ReservationResponse> reservations;
}
