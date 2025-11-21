package com.yourorg.parking.repository;

import com.yourorg.parking.model.ParkingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ParkingTransaction entity data access operations.
 * 
 * This interface manages the persistence of parking transactions, which represent
 * the complete lifecycle of a parking session from check-in to check-out.
 * 
 * Key responsibilities:
 * - Creating new transactions during check-in
 * - Finding active (ongoing) transactions
 * - Retrieving transaction history for vehicles
 * - Updating transactions during check-out
 * - Supporting reporting and analytics queries
 * 
 * A transaction with checkOutTime = null indicates an active parking session,
 * while transactions with checkOutTime set are completed sessions.
 * 
 * @author Smart Parking System
 * @version 1.0
 */
@Repository
public interface ParkingTransactionRepository extends JpaRepository<ParkingTransaction, Long> {
    
    /**
     * Finds the active (ongoing) parking transaction for a specific vehicle.
     * 
     * An active transaction is one where the vehicle has checked in but not
     * yet checked out (checkOutTime is null). This method is crucial during
     * check-out to locate the transaction that needs to be completed.
     * 
     * Business rule: A vehicle should have at most one active transaction at a time.
     * If this returns a result, it means the vehicle is currently parked.
     * 
     * @param vehicleId the database ID of the vehicle
     * @return an Optional containing the active transaction if found, empty if vehicle is not currently parked
     */
    Optional<ParkingTransaction> findByVehicleIdAndCheckOutTimeIsNull(Long vehicleId);
    
    /**
     * Retrieves all parking transactions for a specific vehicle.
     * 
     * This includes both active and completed transactions, providing a
     * complete parking history for the vehicle. Useful for:
     * - Displaying parking history to users
     * - Generating usage reports
     * - Analyzing parking patterns
     * - Customer service inquiries
     * 
     * @param vehicleId the database ID of the vehicle
     * @return list of all transactions associated with the vehicle, ordered by date
     */
    List<ParkingTransaction> findByVehicleId(Long vehicleId);
}





