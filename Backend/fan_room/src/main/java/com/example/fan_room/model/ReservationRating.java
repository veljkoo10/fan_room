package com.example.fan_room.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "reservation_ratings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"reservation_id", "user_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRating {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int hygieneRating;

    @Column(nullable = false)
    private int equipmentRating;

    @Column(nullable = false)
    private int atmosphereRating;
    @Column(length = 1000)
    private String comment;
}
