package com.example.fan_room.controller;


import com.example.fan_room.dto.Request.CreateReservationRequest;
import com.example.fan_room.dto.Response.ReservationResponse;
import com.example.fan_room.dto.Response.ReservationsBySportResponse;
import com.example.fan_room.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> createReservation(@RequestBody CreateReservationRequest request) {
        reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ReservationResponse>> getReservationsForUser() {
        List<ReservationResponse> reservations = reservationService.getReservationsByUserId();
        return ResponseEntity.ok(reservations);
    }

    @PatchMapping("/{reservationId}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{reservationId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockReservation(@PathVariable UUID reservationId) {
        reservationService.blockReservation(reservationId);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{reservationId}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable UUID reservationId,
            @RequestBody CreateReservationRequest request) {

        ReservationResponse updatedReservation = reservationService.updateReservationTime(reservationId, request);
        return ResponseEntity.ok(updatedReservation);
    }

    @GetMapping("/by-sport")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationsBySportResponse>> getAllReservationsBySport() {
        List<ReservationsBySportResponse> responseList = reservationService.getAllReservationsBySport();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/by-sport-user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ReservationsBySportResponse>> getUserActiveReservationsBySport() {
        List<ReservationsBySportResponse> responseList = reservationService.getUserActiveReservationsBySport();
        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/admin/blocked")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReservationResponse> createBlockedReservation(@RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.createBlockedReservationByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{reservationId}/join")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ReservationResponse> joinReservation(@PathVariable UUID reservationId) {
        ReservationResponse response = reservationService.joinReservation(reservationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reservationId}/remove")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ReservationResponse> removeFromReservation(@PathVariable UUID reservationId) {
        return ResponseEntity.ok(reservationService.removeFromReservation(reservationId));
    }
}