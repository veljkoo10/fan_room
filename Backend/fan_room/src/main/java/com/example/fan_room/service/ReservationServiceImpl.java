package com.example.fan_room.service;


import com.example.fan_room.dto.Request.CreateReservationRequest;
import com.example.fan_room.dto.Response.ReservationResponse;
import com.example.fan_room.dto.Response.ReservationsBySportResponse;
import com.example.fan_room.exception.*;
import com.example.fan_room.mapper.ReservationMapper;
import com.example.fan_room.model.*;
import com.example.fan_room.repository.ReservationRepository;
import com.example.fan_room.repository.SportRepository;
import com.example.fan_room.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SportRepository sportRepository;
    private final ReservationMapper reservationMapper;
    private final CurrentUserService currentUserService;
    private record TimeSlot(LocalDateTime start, LocalDateTime end) {}

    @Value("${working.hours.start}")
    private String workingHoursStart;

    @Value("${working.hours.end}")
    private String workingHoursEnd;

    @Value("${working.duration}")
    private int workingDuration;

    public void createReservation(CreateReservationRequest request) {
        String kreatorUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User kreator = userRepository.findByUsername(kreatorUsername)
                .orElseThrow(() -> new NotFoundException("Korisnik sa username-om " + kreatorUsername + " not found."));

        Sport sport = sportRepository.findById(request.getSportId())
                .orElseThrow(() -> new NotFoundException("Sport with ID " + request.getSportId() + " not found."));

        int maxDodatnihUcestnika = (sport.getPlayerCount() != null ? sport.getPlayerCount() : 1) - 1;

        List<String> usernamesUcestnika = request.getParticipants() != null
                ? new ArrayList<>(request.getParticipants())
                : new ArrayList<>();
        usernamesUcestnika.add(kreatorUsername);

        if (usernamesUcestnika.size() - 1 > maxDodatnihUcestnika) {
            throw new MaxParticipantsReachedException(
                    "Maximum number of additional participants for " + sport.getName() + " is " + maxDodatnihUcestnika
            );
        }

        List<User> ucestnici = userRepository.findAllByUsernameIn(usernamesUcestnika);

        if (ucestnici.size() != new HashSet<>(usernamesUcestnika).size()) {
            throw new NotFoundException("One or more participants were not found.");
        }

        validateReservation(request.getStartTime(), request.getEndTime());

        for (User ucestnik : ucestnici) {
            checkUserAvailability(ucestnik, request.getStartTime(), request.getEndTime());
        }

        generateTimeSlots(request.getStartTime(), request.getEndTime(), workingDuration)
                .forEach(slot -> {
                    checkSportAvailability(request.getSportId(), slot.start(), slot.end());

                    Reservation rezervacija = Reservation.builder()
                            .user(kreator)
                            .sport(sport)
                            .startTime(slot.start())
                            .endTime(slot.end())
                            .status(ReservationStatus.ACTIVE)
                            .participants(usernamesUcestnika)
                            .openForJoin(request.isOpenForJoin())
                            .build();

                    reservationRepository.save(rezervacija);
                });
    }

    private List<TimeSlot> generateTimeSlots(LocalDateTime start, LocalDateTime end, int durationHours) {
        long steps = Duration.between(start, end).toHours() / durationHours;

        return java.util.stream.IntStream.range(0, (int) steps)
                .mapToObj(i -> {
                    LocalDateTime slotStart = start.plusHours((long) i * durationHours);
                    return new TimeSlot(slotStart, slotStart.plusHours(durationHours));
                })
                .toList();
    }


    private void checkSportAvailability(UUID sportId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Reservation> conflicts = reservationRepository.findSameReservations(sportId, startTime, endTime);
        boolean hasBlocked = conflicts.stream()
                .anyMatch(r -> r.getStatus() == ReservationStatus.BLOCKED);
        if (hasBlocked) {
            throw new BadRequestException(HttpStatus.CONFLICT,
                    "You cannot create a reservation in this time slot because it is blocked by admin.");
        }
        if (conflicts.stream().anyMatch(r -> r.getStatus() == ReservationStatus.ACTIVE)) {
            throw new BadRequestException(HttpStatus.CONFLICT,
                    "The sport is already taken in the selected time slot.");
        }
    }

    private void validateReservation(LocalDateTime startTime, LocalDateTime endTime) {
        LocalTime startWorkingHours = LocalTime.parse(workingHoursStart);
        LocalTime endWorkingHours = LocalTime.parse(workingHoursEnd);

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST,
                    "It is not possible to create a reservation in the past.");
        }

        if (startTime.toLocalTime().isBefore(startWorkingHours) ||
                endTime.toLocalTime().isAfter(endWorkingHours)) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "Reservations must be made during business hours: "
                    + workingHoursStart + " - " + workingHoursEnd);
        }

        if (startTime.getMinute() != 0 || endTime.getMinute() != 0) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "The start and end times must start on the full hour (eg 08:00, 09:00).");
        }

        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "The end of the term must be after the beginning.");
        }

        long durationHours = Duration.between(startTime, endTime).toHours();
        if (durationHours % workingDuration != 0) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST, "The duration of the reservation must be a multiple of "
                    + workingDuration + "h.");
        }
    }


    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getReservationsByUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotAllowed("User with username " + username + " not found."));

        return reservationRepository.findByUserId(user.getId())
                .stream()
                .map(reservationMapper::toDto)
                .collect(Collectors.toList());
    }


    public void cancelReservation(UUID reservationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotAllowed("User with username " + username + " not found."));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        reservationRepository.findById(reservationId)
                .map(reservation -> {
                    boolean isOwner = reservation.getUser().getId().equals(user.getId());

                    if (!isOwner && !isAdmin) {
                        throw new BadRequestException(HttpStatus.BAD_REQUEST,
                                "You are not allowed to cancel this reservation because you are not its owner or admin.");
                    }
                    if (reservation.getStatus() != ReservationStatus.ACTIVE) {
                        throw new BadRequestException(HttpStatus.BAD_REQUEST,
                                "The reservation cannot be canceled because it is not active. Current status: "
                                        + reservation.getStatus());
                    }

                    reservation.setStatus(ReservationStatus.CANCELED);
                    return reservationRepository.save(reservation);
                })
                .orElseThrow(() -> new NotFoundException("Reservation with ID " + reservationId + " was not found."));
    }



    public String blockReservation(UUID reservationId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotAllowed("User with username " + username + " not found."));

        if (user.getRole() != UserRole.ADMIN) {
            throw new UserNotAllowed("Only an administrator can block a booking.");
        }

        reservationRepository.findById(reservationId)
                .map(reservation -> {
                    if (reservation.getStatus() != ReservationStatus.ACTIVE) {
                        throw new BadRequestException(HttpStatus.BAD_REQUEST,
                                "The reservation cannot be blocked because it is not active. Current status: " + reservation.getStatus());
                    }
                    reservation.setStatus(ReservationStatus.BLOCKED);
                    return reservationRepository.save(reservation);
                })
                .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND,
                        "Reservation with ID " + reservationId + " not found."));

        return "Reservation with ID successfully blocked.";

    }

    public ReservationResponse updateReservationTime(UUID reservationId, CreateReservationRequest request) {
        Reservation updatedReservation = reservationRepository.findById(reservationId)
                .map(reservation -> {
                    if (reservation.getStatus() != ReservationStatus.ACTIVE) {
                        throw new BadRequestException(HttpStatus.CONFLICT,
                                "The reservation cannot be changed because it is not active. Current status: "
                                        + reservation.getStatus());
                    }

                    validateReservation(request.getStartTime(), request.getEndTime());
                    checkSportAvailability(reservation.getSport().getId(), request.getStartTime(), request.getEndTime());

                    reservation.setStartTime(request.getStartTime());
                    reservation.setEndTime(request.getEndTime());

                    return reservationRepository.save(reservation);
                })
                .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND,
                        "Reservation with ID " + reservationId + " not found."));

        return reservationMapper.toDto(updatedReservation);
    }


    public List<ReservationsBySportResponse> getAllReservationsBySport() {
        Map<String, List<ReservationResponse>> reservationsBySport = reservationRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        reservation -> reservation.getSport().getName(),
                        Collectors.mapping(reservationMapper::toDto, Collectors.toList())
                ));
        return reservationsBySport.entrySet().stream()
                .map(entry -> new ReservationsBySportResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }


    @Override
    public List<ReservationsBySportResponse> getUserActiveReservationsBySport() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotAllowed("User with username " + username + " not found."));

        Map<String, List<ReservationResponse>> reservationsBySport = reservationRepository.findByUserId(user.getId())
                .stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.ACTIVE)
                .collect(Collectors.groupingBy(
                        reservation -> reservation.getSport().getName(),
                        Collectors.mapping(reservationMapper::toDto, Collectors.toList())
                ));

        return reservationsBySport.entrySet().stream()
                .map(entry -> new ReservationsBySportResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public ReservationResponse createBlockedReservationByAdmin(CreateReservationRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotAllowed("Admin with username " + username + " not found."));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new UserNotAllowed("Only an administrator can create a blocked reservation.");
        }

        Sport sport = sportRepository.findById(request.getSportId())
                .orElseThrow(() -> new NotFoundException("Sport not found with ID: " + request.getSportId()));

        validateReservation(request.getStartTime(), request.getEndTime());

        List<Reservation> conflictingReservations = reservationRepository.findSameReservations(
                request.getSportId(), request.getStartTime(), request.getEndTime()
        );
        conflictingReservations.forEach(reservation -> {
            reservation.setStatus(ReservationStatus.BLOCKED);
            reservationRepository.save(reservation);
        });
        List<Reservation> newReservations = generateTimeSlots(request.getStartTime(), request.getEndTime(), workingDuration)
                .stream()
                .map(slot -> Reservation.builder()
                        .user(admin)
                        .sport(sport)
                        .startTime(slot.start())
                        .endTime(slot.end())
                        .status(ReservationStatus.BLOCKED)
                        .build())
                .toList();

        reservationRepository.saveAll(newReservations);

        return reservationMapper.toDto(newReservations.get(0));
    }

    @Override
    public ReservationResponse joinReservation(UUID reservationId) {
        String username = currentUserService.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation with ID " + reservationId + " not found."));

        validateReservationStatus(reservation);
        validateReservationOpenForJoin(reservation);
        validateUserParticipation(reservation, username);
        validateMaxParticipants(reservation);

        checkUserAvailability(user, reservation.getStartTime(), reservation.getEndTime());

        addUserToReservation(reservation, username);

        Reservation updated = reservationRepository.save(reservation);
        return reservationMapper.toDto(updated);
    }

    private void validateReservationStatus(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new ReservationNotActiveException(
                    "You cannot join this reservation because it is not active. Current status: " + reservation.getStatus());
        }
    }

    private void validateReservationOpenForJoin(Reservation reservation) {
        if (!reservation.isOpenForJoin()) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST,
                    "You cannot join this reservation because the creator has closed it for additional participants.");
        }
    }

    private void validateUserParticipation(Reservation reservation, String username) {
        List<String> currentParticipants = reservation.getParticipants() != null
                ? reservation.getParticipants()
                : new ArrayList<>();
        if (currentParticipants.contains(username) || reservation.getUser().getUsername().equals(username)) {
            throw new BadRequestException(HttpStatus.CONFLICT,
                    "You are already part of this reservation.");
        }
    }

    private void validateMaxParticipants(Reservation reservation) {
        Sport sport = reservation.getSport();
        int allowedParticipants = sport.getPlayerCount() != null ? sport.getPlayerCount() : 0;
        List<String> currentParticipants = reservation.getParticipants() != null
                ? reservation.getParticipants()
                : new ArrayList<>();
        if (currentParticipants.size() >= allowedParticipants) {
            throw new BadRequestException(HttpStatus.CONFLICT,
                    "Max number of participants for " + sport.getName() + " is already reached.");
        }
    }

    private void addUserToReservation(Reservation reservation, String username) {
        List<String> currentParticipants = reservation.getParticipants() != null
                ? reservation.getParticipants()
                : new ArrayList<>();
        currentParticipants.add(username);
        reservation.setParticipants(currentParticipants);

        Sport sport = reservation.getSport();
        int allowedParticipants = sport.getPlayerCount() != null ? sport.getPlayerCount() : 0;
        if (currentParticipants.size() >= allowedParticipants) {
            reservation.setOpenForJoin(false);
        }
    }

    private void checkUserAvailability(User user, LocalDateTime startTime, LocalDateTime endTime) {
        List<Reservation> conflicts = reservationRepository.findUserReservationsInTimeRange(user.getUsername(), startTime, endTime);

        boolean hasConflict = conflicts.stream()
                .anyMatch(r -> r.getStatus() == ReservationStatus.ACTIVE);

        if (hasConflict) {
            throw new BadRequestException(HttpStatus.CONFLICT,
                    "User " + user.getUsername() + " already has a reservation at this time in another sport.");
        }
    }

    @Override
    public ReservationResponse removeFromReservation(UUID reservationId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation with ID " + reservationId + " not found."));

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST,
                    "You cannot leave this reservation because it is not active. Current status: " + reservation.getStatus());
        }

        if (reservation.getUser().getUsername().equals(username)) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST,
                    "You cannot remove yourself from a reservation you created. You must cancel the reservation instead.");
        }

        List<String> currentParticipants = new java.util.ArrayList<>(reservation.getParticipants());

        if (!currentParticipants.contains(username)) {
            throw new BadRequestException(HttpStatus.BAD_REQUEST,
                    "You are not part of this reservation.");
        }

        currentParticipants.remove(username);
        reservation.setParticipants(currentParticipants);
        Sport sport = reservation.getSport();
        int maxParticipants = sport.getPlayerCount() != null ? sport.getPlayerCount() : 0;

        if (currentParticipants.size() < maxParticipants) {
            reservation.setOpenForJoin(true);
        }

        Reservation updated = reservationRepository.save(reservation);
        return reservationMapper.toDto(updated);
    }

}