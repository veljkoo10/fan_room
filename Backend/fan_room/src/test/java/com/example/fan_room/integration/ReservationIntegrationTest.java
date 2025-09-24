package com.example.fan_room.integration;

import com.example.fan_room.dto.Request.CreateReservationRequest;
import com.example.fan_room.model.*;
import com.example.fan_room.repository.ReservationRepository;
import com.example.fan_room.repository.SportRepository;
import com.example.fan_room.repository.UserRepository;
import com.example.fan_room.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class ReservationIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private ReservationService reservationService;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void testCreateReservation() {
        User user = createTestUser("test@example.com", "testuser");
        Sport sport = createTestSport("Test Sport");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        CreateReservationRequest request = CreateReservationRequest.builder()
                .startTime(start)
                .endTime(end)
                .sportId(sport.getId())
                .build();

        reservationService.createReservation(request);

        List<Reservation> reservations = reservationRepository.findByUserId(user.getId());
        assertThat(reservations).isNotEmpty();
        assertThat(reservations).hasSize(2);
    }

    @Test
    void testCancelReservation() {
        User user = createTestUser("cancel@example.com", "canceluser");
        Sport sport = createTestSport("Cancel Sport");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        CreateReservationRequest request = CreateReservationRequest.builder()
                .startTime(start)
                .endTime(end)
                .sportId(sport.getId())
                .build();

        reservationService.createReservation(request);

        List<Reservation> reservations = reservationRepository.findByUserId(user.getId());
        assertThat(reservations).isNotEmpty();

        UUID reservationId = reservations.get(0).getId();

        reservationService.cancelReservation(reservationId);

        Reservation cancelledReservation = reservationRepository.findById(reservationId)
                .orElseThrow();

        assertThat(cancelledReservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    private User createTestUser(String email, String username) {
        User user = User.builder()
                .email(email)
                .password("password")
                .username(username)
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .build();
        return userRepository.save(user);
    }

    private Sport createTestSport(String name) {
        Sport sport = Sport.builder()
                .name(name)
                .build();
        return sportRepository.save(sport);
    }

}
