package com.yourorg.parking.repository;

import com.yourorg.parking.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Vehicle entity data access operations.
 * 
 * This interface extends JpaRepository to provide standard CRUD operations
 * for Vehicle entities, along with custom query methods specific to the
 * smart parking system's needs.
 * 
 * Spring Data JPA automatically implements this interface at runtime,
 * generating the necessary database queries based on method names.
 * 
 * Primary responsibilities:
 * - Vehicle record creation and retrieval
 * - Vehicle lookup by license plate number
 * - Vehicle information updates
 * 
 * @author Smart Parking System
 * @version 1.0
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    /**
     * Finds a vehicle by its registration number (license plate).
     * 
     * This is the most common lookup method as vehicle numbers are unique
     * and are used as the primary business identifier during check-in/check-out.
     * 
     * @param vehicleNumber the vehicle registration number to search for
     * @return an Optional containing the vehicle if found, empty otherwise
     */
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}





