package com.example.fan_room.service;

import com.example.fan_room.dto.Request.SportCreateRequest;
import com.example.fan_room.dto.Request.SportUpdateRequest;
import com.example.fan_room.dto.Response.SportResponse;
import com.example.fan_room.dto.Response.SportStatisticsResponse;
import com.example.fan_room.exception.NotFoundException;
import com.example.fan_room.exception.SportAlreadyExistsException;
import com.example.fan_room.model.ReservationStatus;
import com.example.fan_room.model.Sport;
import com.example.fan_room.repository.ReservationRepository;
import com.example.fan_room.repository.SportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SportServiceImpl implements SportService {

    private final SportRepository sportRepository;
    private final ReservationRepository reservationRepository;

    public SportResponse createSport(SportCreateRequest dto) {
        if (sportRepository.existsByName(dto.getName())) {
            throw new SportAlreadyExistsException("A sport with a name '" + dto.getName() + "' already exists!");
        }

        Sport sport = Sport.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .playerCount(dto.getPlayerCount())
                .build();


        Sport savedSport = sportRepository.save(sport);

        SportResponse response = SportResponse.builder()
                .id(savedSport.getId())
                .name(savedSport.getName())
                .description(savedSport.getDescription())
                .playerCount(savedSport.getPlayerCount())
                .build();
        return response;
    }

    @Override
    public List<SportResponse> getAllSports() {
        return sportRepository.findAll()
                .stream()
                .map(sport -> SportResponse.builder()
                        .id(sport.getId())
                        .name(sport.getName())
                        .description(sport.getDescription())
                        .playerCount(sport.getPlayerCount())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void deleteSport(UUID sportId) {
        Sport sport = sportRepository.findById(sportId)
                .orElseThrow(() -> new NotFoundException("Sport with ID " + sportId + " not found."));

        reservationRepository.deleteBySportId(sportId);
        sportRepository.delete(sport);
    }

    @Override
    @Transactional
    public SportResponse updateSport(UUID sportId, SportUpdateRequest dto) {
        Sport sport = sportRepository.findById(sportId)
                .orElseThrow(() -> new NotFoundException("Sport with ID " + sportId + " not found."));

        String normalizedNewName = dto.getName().trim();
        String normalizedCurrentName = sport.getName().trim();

        if (!normalizedCurrentName.equalsIgnoreCase(normalizedNewName)
                && sportRepository.existsByNameIgnoreCase(normalizedNewName)) {
            throw new SportAlreadyExistsException("A sport with a name '" + dto.getName() + "' already exists!");
        }

        boolean hasActiveReservations = reservationRepository
                .existsBySportIdWithActiveStatus(sportId, ReservationStatus.ACTIVE);

        sport.setName(normalizedNewName);
        sport.setDescription(dto.getDescription());

        boolean playerCountUpdated = false;

        if (!hasActiveReservations && dto.getPlayerCount() != null) {
            sport.setPlayerCount(dto.getPlayerCount());
            playerCountUpdated = true;
        }

        Sport updatedSport = sportRepository.save(sport);

        SportResponse response = SportResponse.builder()
                .id(updatedSport.getId())
                .name(updatedSport.getName())
                .description(updatedSport.getDescription())
                .playerCount(updatedSport.getPlayerCount())
                .build();

        if (hasActiveReservations && dto.getPlayerCount() != null && !playerCountUpdated) {
            response.setMessage("The number of players cannot be changed due to active bookings.");
        }

        return response;
    }

    @Override
    public List<SportStatisticsResponse> getSportReservationStatistics() {
        return reservationRepository.countReservationsBySport();
    }

}
