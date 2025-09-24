package com.example.fan_room.unit;

import com.example.fan_room.dto.Request.CreateReservationRequest;
import com.example.fan_room.dto.Response.ReservationResponse;
import com.example.fan_room.exception.BadRequestException;
import com.example.fan_room.exception.NotFoundException;
import com.example.fan_room.mapper.ReservationMapper;
import com.example.fan_room.model.*;
import com.example.fan_room.repository.ReservationRepository;
import com.example.fan_room.repository.SportRepository;
import com.example.fan_room.repository.UserRepository;
import com.example.fan_room.service.ReservationService;
import com.example.fan_room.service.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationServiceImpl reservationServiceImpl;

    @Mock
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SportRepository sportRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @BeforeEach
    void setUp() {
        reservationService = reservationServiceImpl;

        ReflectionTestUtils.setField(reservationServiceImpl, "workingHoursStart", "10:00");
        ReflectionTestUtils.setField(reservationServiceImpl, "workingHoursEnd", "18:00");
        ReflectionTestUtils.setField(reservationServiceImpl, "workingDuration", 1);
    }

    private User createTestUser(UUID id, String username) {
        return User.builder()
                .id(id)
                .username(username)
                .role(UserRole.USER)
                .build();
    }

    private Sport createTestSport(UUID id, String name) {
        return Sport.builder()
                .id(id)
                .name(name)
                .build();
    }

    private Reservation createTestReservation(UUID id, User user, Sport sport, LocalDateTime start, LocalDateTime end) {
        return Reservation.builder()
                .id(id)
                .user(user)
                .sport(sport)
                .startTime(start)
                .endTime(end)
                .status(ReservationStatus.ACTIVE)
                .build();
    }

    @Test
    void createReservation_success() {
        UUID userId = UUID.randomUUID();
        UUID sportId = UUID.randomUUID();

        User user = createTestUser(userId, "testuser");
        Sport sport = createTestSport(sportId, "Fudbal");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUsername(), "password")
        );

        CreateReservationRequest request = CreateReservationRequest.builder()
                .sportId(sportId)
                .startTime(LocalDateTime.of(2026, 8, 21, 10, 0))
                .endTime(LocalDateTime.of(2026, 8, 21, 11, 0))
                .build();

        Reservation savedReservation = createTestReservation(UUID.randomUUID(), user, sport,
                request.getStartTime(), request.getEndTime());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(sportRepository.findById(sportId)).thenReturn(Optional.of(sport));
        when(reservationRepository.findSameReservations(any(), any(), any())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        when(userRepository.findAllByUsernameIn(anyList())).thenReturn(Collections.singletonList(user));

        reservationServiceImpl.createReservation(request);

        assertEquals(ReservationStatus.ACTIVE, savedReservation.getStatus());
        assertEquals(userId, savedReservation.getUser().getId());
        assertEquals(sportId, savedReservation.getSport().getId());
    }


    @Test
    void createReservation_userNotFound() {
        CreateReservationRequest request = CreateReservationRequest.builder()
                .sportId(UUID.randomUUID())
                .startTime(LocalDateTime.of(2025, 8, 21, 10, 0))
                .endTime(LocalDateTime.of(2025, 8, 21, 11, 0))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ghostuser", "password")
        );

        when(userRepository.findByUsername("ghostuser")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reservationServiceImpl.createReservation(request)
        );

        assertTrue(exception.getMessage().contains("Korisnik sa username-om ghostuser not found"));
    }

    @Test
    void createReservation_sportNotFound() {
        UUID sportId = UUID.randomUUID();
        User user = createTestUser(UUID.randomUUID(), "testuser");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUsername(), "password")
        );

        CreateReservationRequest request = CreateReservationRequest.builder()
                .sportId(sportId)
                .startTime(LocalDateTime.of(2025, 8, 21, 10, 0))
                .endTime(LocalDateTime.of(2025, 8, 21, 11, 0))
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(sportRepository.findById(sportId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> reservationServiceImpl.createReservation(request)
        );

        assertTrue(exception.getMessage().contains("Sport with ID " + sportId));
    }

    @Test
    void createReservation_sportAlreadyBooked_shouldThrowException() {
        UUID sportId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.of(2026, 8, 21, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 21, 12, 0);

        User user = createTestUser(UUID.randomUUID(), "testuser");
        Sport sport = createTestSport(sportId, "Fudbal");
        Reservation conflictingReservation = createTestReservation(UUID.randomUUID(), user, sport, start, end);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUsername(), "password")
        );

        CreateReservationRequest request = CreateReservationRequest.builder()
                .sportId(sportId)
                .startTime(start)
                .endTime(end)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(sportRepository.findById(sportId)).thenReturn(Optional.of(sport));
        when(userRepository.findAllByUsernameIn(anyList())).thenReturn(Collections.singletonList(user));
        when(reservationRepository.findSameReservations(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(conflictingReservation));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> reservationServiceImpl.createReservation(request)
        );

        assertTrue(exception.getMessage().contains("The sport is already taken in the selected time slot"));
    }

}
