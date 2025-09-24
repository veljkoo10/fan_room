package com.example.fan_room.repository;

import com.example.fan_room.model.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SportRepository extends JpaRepository<Sport, UUID> {
    boolean existsByName(String name);
    boolean existsByNameIgnoreCase(String name);

}