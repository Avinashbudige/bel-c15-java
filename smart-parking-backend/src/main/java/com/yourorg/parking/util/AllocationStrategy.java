package com.yourorg.parking.util;

import com.yourorg.parking.model.ParkingSpot;
import java.util.List;

/**
 * Strategy interface for parking spot allocation algorithms.
 * 
 * This interface defines the contract for different spot allocation strategies,
 * following the Strategy design pattern. It allows the system to support
 * multiple allocation algorithms and switch between them based on business
 * requirements or user preferences.
 * 
 * Why use the Strategy pattern:
 * - Enables multiple allocation algorithms without changing core logic
 * - Makes the system extensible for new allocation rules
 * - Allows runtime algorithm selection based on context
 * - Separates allocation logic from spot management
 * - Facilitates testing of different strategies
 * 
 * Common allocation strategies that could implement this interface:
 * 
 * 1. NearestToEntranceStrategy:
 *    - Selects the spot closest to the facility entrance
 *    - Minimizes walking distance for users
 *    - Uses distance/proximity calculations
 * 
 * 2. LoadBalancingStrategy:
 *    - Distributes vehicles evenly across floors/zones
 *    - Prevents crowding in specific areas
 *    - Improves traffic flow within facility
 * 
 * 3. FirstAvailableStrategy:
 *    - Simply picks the first available spot from the list
 *    - Fastest algorithm (no complex logic)
 *    - Suitable for non-critical scenarios
 * 
 * 4. PreferredFloorStrategy:
 *    - Considers user's preferred floor/zone
 *    - Improves user satisfaction
 *    - Fallback to other floors if preferred not available
 * 
 * 5. RandomStrategy:
 *    - Randomly selects from available spots
 *    - Ensures even wear and tear
 *    - Simple and fair distribution
 * 
 * 6. PriorityStrategy:
 *    - Reserves premium spots for VIP/disabled users
 *    - Implements business rules for spot prioritization
 *    - May consider subscription level or user type
 * 
 * Implementation guidelines:
 * - Implementations should be stateless (thread-safe)
 * - Should handle empty list gracefully (return null or throw exception)
 * - Should be deterministic when possible (for testing and debugging)
 * - Performance should be O(n) or better for scalability
 * - Should log allocation decisions for audit trails
 * 
 * Usage example:
 * <pre>
 * AllocationStrategy strategy = new NearestToEntranceStrategy();
 * List<ParkingSpot> availableSpots = spotRepository.findByVehicleTypeAndStatus("CAR", "AVAILABLE");
 * ParkingSpot selected = strategy.allocateSpot(availableSpots, "CAR");
 * </pre>
 * 
 * Configuration:
 * The active strategy can be injected via Spring dependency injection,
 * allowing easy switching via configuration:
 * <pre>
 * @Service
 * public class AllocationService {
 *     @Autowired
 *     @Qualifier("nearestToEntranceStrategy")
 *     private AllocationStrategy strategy;
 * }
 * </pre>
 * 
 * @author Smart Parking System
 * @version 1.0
 */
public interface AllocationStrategy {
    
    /**
     * Selects the optimal parking spot from a list of available spots.
     * 
     * This method implements the core allocation logic specific to each strategy.
     * It receives a list of spots that are:
     * - Available (status = AVAILABLE)
     * - Compatible with the vehicle type
     * - Not under maintenance or reserved
     * 
     * The implementation should:
     * 1. Evaluate each spot based on the strategy's criteria
     * 2. Select the most suitable spot
     * 3. Return the selected spot
     * 
     * Contract:
     * - If availableSpots is null or empty, return null (no spots available)
     * - If multiple spots are equally suitable, choose one deterministically
     * - Do NOT modify the spot's status (caller's responsibility)
     * - Do NOT persist changes to database (caller's responsibility)
     * - Should be fast (called frequently during check-in)
     * 
     * The vehicleType parameter can be used for:
     * - Validation (though spots should already be filtered)
     * - Special logic based on vehicle type
     * - Logging and audit trails
     * 
     * Example implementations:
     * 
     * FirstAvailableStrategy:
     * return availableSpots.isEmpty() ? null : availableSpots.get(0);
     * 
     * NearestToEntranceStrategy:
     * return availableSpots.stream()
     *     .min(Comparator.comparing(spot -> calculateDistance(spot)))
     *     .orElse(null);
     * 
     * @param availableSpots list of spots eligible for allocation (already filtered by type and status)
     * @param vehicleType the type of vehicle requesting a spot (for validation or special logic)
     * @return the selected ParkingSpot, or null if no suitable spot found
     */
    ParkingSpot allocateSpot(List<ParkingSpot> availableSpots, String vehicleType);
}





