package com.example.fan_room.repository;

import com.example.fan_room.dto.Response.SportStatisticsResponse;
import com.example.fan_room.model.Reservation;
import com.example.fan_room.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByUserId(UUID userId);
    @Query("""
    SELECT r FROM Reservation r
    WHERE r.sport.id = :sportId
      AND r.status IN ('ACTIVE', 'BLOCKED') 
      AND r.startTime < :endTime 
      AND r.endTime > :startTime
""")
    List<Reservation> findSameReservations(@Param("sportId") UUID sportId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    @Modifying
    @Query("DELETE FROM Reservation r WHERE r.sport.id = :sportId")
    void deleteBySportId(@Param("sportId") UUID sportId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Reservation r " +
            "WHERE r.sport.id = :sportId AND r.status = :activeStatus")
    boolean existsBySportIdWithActiveStatus(@Param("sportId") UUID sportId,
                                            @Param("activeStatus") ReservationStatus activeStatus);


    @Query("SELECT r FROM Reservation r " +
            "WHERE (r.user.username = :username OR :username MEMBER OF r.participants) " +
            "AND r.startTime < :endTime " +
            "AND r.endTime > :startTime " +
            "AND r.status = 'ACTIVE'")
    List<Reservation> findUserReservationsInTimeRange(@Param("username") String username,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);


    @Query("SELECT new com.example.fan_room.dto.Response.SportStatisticsResponse(s.name, COUNT(r.id)) " +
            "FROM Reservation r JOIN r.sport s GROUP BY s.name ORDER BY s.name")
    List<SportStatisticsResponse> countReservationsBySport();

}