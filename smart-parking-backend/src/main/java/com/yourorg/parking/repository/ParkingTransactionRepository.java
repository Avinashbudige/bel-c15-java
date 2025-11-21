package com.yourorg.parking.repository;

import com.yourorg.parking.model.ParkingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingTransactionRepository extends JpaRepository<ParkingTransaction, Long> {
    Optional<ParkingTransaction> findByVehicleIdAndCheckOutTimeIsNull(Long vehicleId);
    List<ParkingTransaction> findByVehicleId(Long vehicleId);
}





