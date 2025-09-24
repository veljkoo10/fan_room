package com.example.fan_room.dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationRatingResponse {
    private String username;
    private int hygieneRating;
    private int equipmentRating;
    private int atmosphereRating;
    private String comment;
}
