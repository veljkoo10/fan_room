package com.example.fan_room.controller;

import com.example.fan_room.dto.Request.SportCreateRequest;
import com.example.fan_room.dto.Request.SportUpdateRequest;
import com.example.fan_room.dto.Response.SportResponse;
import com.example.fan_room.dto.Response.SportStatisticsResponse;
import com.example.fan_room.service.SportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

import java.util.List;

@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
public class SportController {

    private final SportService sportService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SportResponse> createSport(@Valid @RequestBody SportCreateRequest dto) {
        SportResponse response = sportService.createSport(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<SportResponse>> getAllSports() {
        List<SportResponse> sports = sportService.getAllSports();
        return ResponseEntity.ok(sports);
    }


    @DeleteMapping("/{sportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSport(@PathVariable UUID sportId) {
        sportService.deleteSport(sportId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{sportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SportResponse> updateSport(@PathVariable UUID sportId,
                                                     @Valid @RequestBody SportUpdateRequest dto) {
        SportResponse updatedSport = sportService.updateSport(sportId, dto);
        return ResponseEntity.ok(updatedSport);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SportStatisticsResponse>> getSportStatistics() {
        List<SportStatisticsResponse> stats = sportService.getSportReservationStatistics();
        return ResponseEntity.ok(stats);
    }

}
