package com.example.fan_room.service;

import com.example.fan_room.dto.Request.CreateReservationRequest;
import com.example.fan_room.dto.Response.ReservationResponse;
import com.example.fan_room.dto.Response.ReservationsBySportResponse;

import java.util.List;
import java.util.UUID;

public interface ReservationService {
    void createReservation(CreateReservationRequest request);

    List<ReservationResponse> getAllReservations();

    List<ReservationResponse> getReservationsByUserId();

    void cancelReservation(UUID reservationId);

    String blockReservation(UUID reservationId);

    ReservationResponse updateReservationTime(UUID reservationId, CreateReservationRequest request);

    List<ReservationsBySportResponse> getAllReservationsBySport();

    List<ReservationsBySportResponse> getUserActiveReservationsBySport();

    ReservationResponse createBlockedReservationByAdmin(CreateReservationRequest request);

    ReservationResponse joinReservation(UUID reservationId);

    ReservationResponse removeFromReservation(UUID reservationId);

}
