package com.example.fan_room.repository;

import com.example.fan_room.model.Reservation;
import com.example.fan_room.model.ReservationRating;
import com.example.fan_room.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRatingRepository extends JpaRepository<ReservationRating, UUID> {

    boolean existsByReservationAndUser(Reservation reservation, User user);

    List<ReservationRating> findByReservation(Reservation reservation);

    Optional<ReservationRating> findByReservationAndUser(Reservation reservation, User user);

}
