package com.yourorg.parking.repository;

import com.yourorg.parking.model.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ParkingSpot entity data access operations.
 * 
 * This interface provides methods to manage parking spots in the facility,
 * including finding available spots, updating spot status, and querying
 * spots based on various criteria.
 * 
 * Key use cases:
 * - Finding available spots during check-in
 * - Filtering spots by vehicle type compatibility
 * - Retrieving spot information by spot number
 * - Managing spot status (available, occupied, maintenance)
 * - Supporting administrative views of facility status
 * 
 * Spring Data JPA automatically implements this interface at runtime.
 * 
 * @author Smart Parking System
 * @version 1.0
 */
@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    
    /**
     * Finds all parking spots with a specific status.
     * 
     * Useful for queries like:
     * - Finding all available spots to display capacity
     * - Identifying spots under maintenance
     * - Generating reports on facility utilization
     * 
     * @param status the status to filter by (e.g., "AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE")
     * @return list of parking spots matching the status
     */
    List<ParkingSpot> findByStatus(String status);
    
    /**
     * Finds a parking spot by its unique spot number.
     * 
     * Used when:
     * - A vehicle needs to find their assigned spot
     * - Admin needs to update a specific spot
     * - System needs to verify spot existence
     * 
     * @param spotNumber the unique spot identifier (e.g., "A1-101")
     * @return an Optional containing the spot if found, empty otherwise
     */
    Optional<ParkingSpot> findBySpotNumber(String spotNumber);
    
    /**
     * Finds available parking spots that match a specific vehicle type.
     * 
     * This is the core query for the spot allocation algorithm.
     * During check-in, the system uses this to find eligible spots
     * for the incoming vehicle. The allocation service can then apply
     * additional logic (e.g., nearest spot, preferred floor) to select
     * the best spot from the returned list.
     * 
     * @param vehicleType the type of vehicle (e.g., "CAR", "BIKE", "TRUCK")
     * @param status the desired status (typically "AVAILABLE")
     * @return list of parking spots matching both vehicle type and status
     */
    List<ParkingSpot> findByVehicleTypeAndStatus(String vehicleType, String status);
}





