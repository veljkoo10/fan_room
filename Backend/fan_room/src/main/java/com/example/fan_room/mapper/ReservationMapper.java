package com.example.fan_room.mapper;

import com.example.fan_room.dto.Response.ReservationResponse;
import com.example.fan_room.model.Reservation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationMapper {

    public ReservationResponse toDto(Reservation reservation) {
        if (reservation == null) return null;

        return ReservationResponse.builder()
                .id(reservation.getId())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .username(reservation.getUser() != null ? reservation.getUser().getUsername() : null)
                .userId(reservation.getUser() != null ? reservation.getUser().getId() : null)
                .sportName(reservation.getSport() != null ? reservation.getSport().getName() : null)
                .sportId(reservation.getSport() != null ? reservation.getSport().getId() : null)
                .participants(reservation.getParticipants() != null ? reservation.getParticipants() : List.of())
                .openForJoin(reservation.isOpenForJoin())
                .build();
    }
}
