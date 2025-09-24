package com.example.fan_room.controller;

import com.example.fan_room.dto.Request.CreateRatingRequest;
import com.example.fan_room.dto.Response.ReservationRatingResponse;
import com.example.fan_room.service.ReservationRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationRatingController {

    private final ReservationRatingService ratingService;

    @PostMapping("/{reservationId}/ratings")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ReservationRatingResponse> rateReservation(
            @PathVariable UUID reservationId,
            @Valid @RequestBody CreateRatingRequest request) {

        return ResponseEntity.ok(ratingService.rateReservation(reservationId, request));
    }

    @GetMapping("/{reservationId}/ratings")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<ReservationRatingResponse>> getRatings(@PathVariable UUID reservationId) {
        return ResponseEntity.ok(ratingService.getRatingsForReservation(reservationId));
    }

    @GetMapping("/{reservationId}/has-rated")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public boolean hasUserRated(@PathVariable UUID reservationId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ratingService.hasUserRated(reservationId, username);
    }

    @DeleteMapping("/{reservationId}/ratings")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Void> deleteRating(@PathVariable UUID reservationId) {
        ratingService.deleteRating(reservationId);
        return ResponseEntity.noContent().build();
    }

}
