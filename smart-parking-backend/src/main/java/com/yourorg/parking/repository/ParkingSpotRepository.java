package com.yourorg.parking.repository;

import com.yourorg.parking.model.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    List<ParkingSpot> findByStatus(String status);
    Optional<ParkingSpot> findBySpotNumber(String spotNumber);
    List<ParkingSpot> findByVehicleTypeAndStatus(String vehicleType, String status);
}





