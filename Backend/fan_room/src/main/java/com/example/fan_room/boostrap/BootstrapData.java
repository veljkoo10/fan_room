package com.example.fan_room.boostrap;

import com.example.fan_room.model.*;
import com.example.fan_room.repository.ReservationRatingRepository;
import com.example.fan_room.repository.ReservationRepository;
import com.example.fan_room.repository.SportRepository;
import com.example.fan_room.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.github.javafaker.Faker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BootstrapData implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final SportRepository sportRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationRatingRepository ratingRepository;

    @Override
    public void run(String... args) throws Exception {
        Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        Long sportCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sports", Long.class);

        if (userCount == 0 && sportCount == 0) {
            loadSportData();
            loadUserData();
            loadReservationData();
        }
    }



    private void loadSportData() {
        jdbcTemplate.update(
                "INSERT INTO sports (id, name, description, player_count) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "Football", "Popular team sport played with a ball.", 22
        );
        jdbcTemplate.update(
                "INSERT INTO sports (id, name, description, player_count) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "Basketball", "Dynamic sport played between two teams of five players.", 10
        );
        jdbcTemplate.update(
                "INSERT INTO sports (id, name, description, player_count) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "Tennis", "Racket sport played individually or in pairs.", 2
        );
        jdbcTemplate.update(
                "INSERT INTO sports (id, name, description, player_count) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "Volleyball", "Team sport where two teams compete on a court separated by a net.", 12
        );
        jdbcTemplate.update(
                "INSERT INTO sports (id, name, description, player_count) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "Table Tennis", "Fast-paced skill sport, also known as ping-pong.", 2
        );
    }

    private void loadUserData() {
        Faker faker = new Faker(new Locale("en"));

        String adminPasswordHash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("Voki25r2003!");
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, username, email, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                "Admin",
                "Admin",
                "admin",
                "admin@gmail.com",
                adminPasswordHash,
                UserRole.ADMIN.name()
        );

        String user1PasswordHash = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("Voki25r2003!");
        jdbcTemplate.update(
                "INSERT INTO users (id, first_name, last_name, username, email, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                "Voki",
                "User",
                "user1",
                "user1@gmail.com",
                user1PasswordHash,
                UserRole.USER.name()
        );

        String fixedPasswordHash = "$2a$10$I.GRGych.8VJP81GL8OOteuFVlGNn0KbDnYfuCiNd5uZp.L8Xfw8.";
        for (int i = 0; i < 3; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            jdbcTemplate.update(
                    "INSERT INTO users (id, first_name, last_name, username, email, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    UUID.randomUUID(),
                    firstName,
                    lastName,
                    faker.name().username(),
                    firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com",
                    fixedPasswordHash,
                    UserRole.USER.name()
            );
        }
    }
    private void loadReservationData() {
        List<User> normalUsers = userRepository.findAll().stream()
                .filter(u -> !u.getEmail().equals("admin@gmail.com") && !u.getEmail().equals("user1@gmail.com"))
                .toList();

        List<Sport> sports = sportRepository.findAll();

        User user1 = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals("user1@gmail.com"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("user1@gmail.com not found"));

        if (!sports.isEmpty()) {
            Reservation specialReservation = Reservation.builder()
                    .user(user1)
                    .sport(sports.get(0))
                    .startTime(LocalDateTime.of(2025, 9, 4, 10, 0))
                    .endTime(LocalDateTime.of(2025, 9, 4, 11, 0))
                    .status(ReservationStatus.ACTIVE)
                    .openForJoin(false)
                    .participants(List.of())
                    .build();
            reservationRepository.save(specialReservation);
            loadReservationRatings(specialReservation);

        }

        LocalDateTime startTime = LocalDateTime.of(2025, 9, 5, 10, 0);
        int reservationCount = Math.min(normalUsers.size(), sports.size());

        for (int i = 0; i < reservationCount; i++) {
            Reservation reservation = Reservation.builder()
                    .user(normalUsers.get(i))
                    .sport(sports.get(i))
                    .startTime(startTime.plusHours(i))
                    .endTime(startTime.plusHours(i + 1))
                    .status(ReservationStatus.ACTIVE)
                    .openForJoin(false)
                    .participants(List.of())
                    .build();
            reservationRepository.save(reservation);
        }
    }
    private void loadReservationRatings(Reservation reservation) {
        List<User> otherUsers = userRepository.findAll().stream()
                .filter(u -> !u.getEmail().equals("admin@gmail.com") && !u.getEmail().equals("user1@gmail.com"))
                .toList();

        if (otherUsers.isEmpty()) return;

        ReservationRating rating1 = ReservationRating.builder()
                .reservation(reservation)
                .user(otherUsers.get(0))
                .hygieneRating(8)
                .equipmentRating(9)
                .atmosphereRating(7)
                .comment("Great reservation, very well organized!")
                .build();
        ratingRepository.save(rating1);

        if (otherUsers.size() > 1) {
            ReservationRating rating2 = ReservationRating.builder()
                    .reservation(reservation)
                    .user(otherUsers.get(1))
                    .hygieneRating(7)
                    .equipmentRating(8)
                    .atmosphereRating(8)
                    .comment("Good experience, I enjoyed it!")
                    .build();
            ratingRepository.save(rating2);
        }
    }

}
