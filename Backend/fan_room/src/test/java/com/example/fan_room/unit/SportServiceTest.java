package com.example.fan_room.unit;

import com.example.fan_room.dto.Request.SportCreateRequest;
import com.example.fan_room.dto.Response.SportResponse;
import com.example.fan_room.exception.SportAlreadyExistsException;
import com.example.fan_room.model.Sport;
import com.example.fan_room.repository.SportRepository;
import com.example.fan_room.service.SportService;
import com.example.fan_room.service.SportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SportServiceTest {

    @Mock
    private SportRepository sportRepository;

    @InjectMocks
    private SportServiceImpl sportServiceImpl;

    @Mock
    private SportService sportService;

    @BeforeEach
    void setUp() {
        sportService = sportServiceImpl;
    }

    @Test
    void createSport_success() {
        SportCreateRequest request = SportCreateRequest.builder()
                .name("Football")
                .description("Team sport")
                .playerCount(11)
                .build();

        UUID sportId = UUID.randomUUID();
        Sport sport = Sport.builder()
                .id(sportId)
                .name(request.getName())
                .description(request.getDescription())
                .playerCount(request.getPlayerCount())
                .build();

        when(sportRepository.existsByName(request.getName())).thenReturn(false);
        when(sportRepository.save(any(Sport.class))).thenReturn(sport);

        SportResponse response = sportService.createSport(request);

        assertNotNull(response);
        assertEquals(sportId, response.getId());
        assertEquals("Football", response.getName());
        assertEquals("Team sport", response.getDescription());
        assertEquals(11, response.getPlayerCount());

        verify(sportRepository, times(1)).existsByName(request.getName());
        verify(sportRepository, times(1)).save(any(Sport.class));
    }

    @Test
    void createSport_alreadyExists_shouldThrowException() {
        SportCreateRequest request = SportCreateRequest.builder()
                .name("Football")
                .build();

        when(sportRepository.existsByName(request.getName())).thenReturn(true);

        SportAlreadyExistsException exception = assertThrows(SportAlreadyExistsException.class,
                () -> sportService.createSport(request)
        );

        assertEquals("A sport with a name 'Football' already exists!", exception.getMessage());

        verify(sportRepository, times(1)).existsByName(request.getName());
        verify(sportRepository, never()).save(any(Sport.class));
    }
}
