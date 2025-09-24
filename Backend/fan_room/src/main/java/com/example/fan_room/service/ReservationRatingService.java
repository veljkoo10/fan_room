package com.example.fan_room.service;

import com.example.fan_room.dto.Request.CreateRatingRequest;
import com.example.fan_room.dto.Response.ReservationRatingResponse;

import java.util.List;
import java.util.UUID;

public interface ReservationRatingService {
    ReservationRatingResponse rateReservation(UUID reservationId, CreateRatingRequest request);
    List<ReservationRatingResponse> getRatingsForReservation(UUID reservationId);
    boolean hasUserRated(UUID reservationId, String username);
    void deleteRating(UUID reservationId);
}
