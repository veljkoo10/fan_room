package com.example.fan_room.service;

import com.example.fan_room.dto.Request.CreateRatingRequest;
import com.example.fan_room.dto.Response.ReservationRatingResponse;
import com.example.fan_room.exception.BadRequestException;
import com.example.fan_room.exception.NotFoundException;
import com.example.fan_room.model.Reservation;
import com.example.fan_room.model.ReservationRating;
import com.example.fan_room.model.User;
import com.example.fan_room.repository.ReservationRatingRepository;
import com.example.fan_room.repository.ReservationRepository;
import com.example.fan_room.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationRatingServiceImpl implements ReservationRatingService {

    private final ReservationRatingRepository ratingRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public ReservationRatingResponse rateReservation(UUID reservationId, CreateRatingRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        validateReservationRating(reservation, user, request);

        ReservationRating rating = ReservationRating.builder()
                .reservation(reservation)
                .user(user)
                .hygieneRating(request.getHygiene())
                .equipmentRating(request.getEquipment())
                .atmosphereRating(request.getAtmosphere())
                .comment(request.getComment())
                .build();

        ratingRepository.save(rating);

        return ReservationRatingResponse.builder()
                .username(username)
                .hygieneRating(request.getHygiene())
                .equipmentRating(request.getEquipment())
                .atmosphereRating(request.getAtmosphere())
                .comment(request.getComment())
                .build();
    }

    private void validateReservationRating(Reservation reservation, User user, CreateRatingRequest request) {
        if (reservation.getEndTime().isAfter(java.time.LocalDateTime.now())) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "You can only rate your past reservations");
        }

        boolean alreadyRated = ratingRepository.existsByReservationAndUser(reservation, user);
        if (alreadyRated) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "You have already rated this reservation");
        }

        if (request.getHygiene() < 1 || request.getHygiene() > 10 ||
                request.getEquipment() < 1 || request.getEquipment() > 10 ||
                request.getAtmosphere() < 1 || request.getAtmosphere() > 10) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Ratings must be between 1 and 10");
        }
    }

    public List<ReservationRatingResponse> getRatingsForReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        return ratingRepository.findByReservation(reservation)
                .stream()
                .map(r -> ReservationRatingResponse.builder()
                        .username(r.getUser().getUsername())
                        .hygieneRating(r.getHygieneRating())
                        .equipmentRating(r.getEquipmentRating())
                        .atmosphereRating(r.getAtmosphereRating())
                        .comment(r.getComment())
                        .build())
                .collect(Collectors.toList());
    }

    public boolean hasUserRated(UUID reservationId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        return ratingRepository.existsByReservationAndUser(reservation, user);
    }

    public void deleteRating(UUID reservationId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        ReservationRating rating = ratingRepository.findByReservationAndUser(reservation, user)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

        ratingRepository.delete(rating);
    }

}
