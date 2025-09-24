package com.example.fan_room.service;

import com.example.fan_room.dto.Request.SportCreateRequest;
import com.example.fan_room.dto.Request.SportUpdateRequest;
import com.example.fan_room.dto.Response.SportResponse;
import com.example.fan_room.dto.Response.SportStatisticsResponse;

import java.util.List;
import java.util.UUID;

public interface SportService {
    SportResponse createSport(SportCreateRequest dto);
    List<SportResponse> getAllSports();
    void deleteSport(UUID sportId);
    SportResponse updateSport(UUID sportId, SportUpdateRequest dto);
    List<SportStatisticsResponse> getSportReservationStatistics();
}
