package com.yourorg.parking.util;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility class for calculating parking fees based on duration and vehicle type.
 * 
 * This class implements the business logic for computing parking charges.
 * The fee calculation can be based on various factors:
 * - Parking duration (time between check-in and check-out)
 * - Vehicle type (different rates for cars, bikes, trucks, etc.)
 * - Time of day (peak vs. off-peak pricing)
 * - Day of week (weekday vs. weekend rates)
 * - Special promotions or discounts
 * - Member vs. non-member pricing
 * 
 * Current implementation uses a simple hourly rate model, but this
 * can be enhanced to support more complex pricing strategies:
 * - Tiered pricing (first hour, subsequent hours)
 * - Maximum daily cap
 * - Minimum charge (e.g., 1 hour minimum)
 * - Grace period (e.g., free first 15 minutes)
 * - Subscription-based pricing
 * 
 * Design considerations:
 * - All monetary values use BigDecimal for precision (avoid floating point errors)
 * - Rates should be configurable (externalized to database or configuration)
 * - Rounding rules should be consistent and documented
 * - Calculation should be deterministic and auditable
 * 
 * Future enhancements:
 * - Load rates from database or configuration service
 * - Support multiple rate schedules (time-based, location-based)
 * - Implement discount rules and promotional codes
 * - Add validation and error handling
 * - Support different currencies
 * 
 * @author Smart Parking System
 * @version 1.0
 */
public class FeeCalculator {

    /**
     * Calculates the total parking fee based on check-in/check-out times and vehicle type.
     * 
     * This is the main entry point for fee calculation. It computes the parking
     * duration and applies the appropriate rate structure for the vehicle type.
     * 
     * Calculation steps:
     * 1. Calculate duration: checkOutTime - checkInTime
     * 2. Convert duration to billable hours (including partial hours)
     * 3. Retrieve base rate for the vehicle type
     * 4. Compute fee: baseRate * totalHours
     * 5. Apply any discounts or caps (if configured)
     * 6. Round to 2 decimal places
     * 
     * Rounding policy:
     * - Partial hours are rounded to 2 decimal places using HALF_UP
     * - This means 1 hour 30 minutes = 1.50 hours
     * - If rate is $10/hour, then 1.5 hours = $15.00
     * 
     * Edge cases to consider:
     * - Very short durations (< 1 minute): Apply minimum charge?
     * - Very long durations (> 24 hours): Apply daily cap?
     * - Negative duration (checkout before checkin): Validation error
     * - Null parameters: Should throw IllegalArgumentException
     * 
     * Example usage:
     * LocalDateTime checkIn = LocalDateTime.of(2024, 1, 15, 10, 0);
     * LocalDateTime checkOut = LocalDateTime.of(2024, 1, 15, 14, 30);
     * BigDecimal fee = FeeCalculator.calculateFee(checkIn, checkOut, "CAR");
     * // Result: 4.5 hours * $10/hour = $45.00
     * 
     * @param checkInTime the timestamp when the vehicle checked in (must not be null)
     * @param checkOutTime the timestamp when the vehicle checked out (must be after checkInTime)
     * @param vehicleType the type of vehicle (determines the rate) (must not be null)
     * @return the calculated parking fee as BigDecimal (always non-negative)
     * @throws IllegalArgumentException if any parameter is null or checkOutTime is before checkInTime
     */
    public static BigDecimal calculateFee(LocalDateTime checkInTime, LocalDateTime checkOutTime, String vehicleType) {
        // TODO: Implement fee calculation logic
        Duration duration = Duration.between(checkInTime, checkOutTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        // Placeholder calculation
        BigDecimal baseRate = getBaseRate(vehicleType);
        BigDecimal totalHours = BigDecimal.valueOf(hours).add(
            BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP)
        );
        
        return baseRate.multiply(totalHours);
    }

    /**
     * Retrieves the base hourly rate for a specific vehicle type.
     * 
     * This method returns the per-hour parking rate based on vehicle type.
     * Different vehicle types may have different rates due to:
     * - Space requirements (trucks need more space)
     * - Market pricing (premium for larger vehicles)
     * - Facility policy
     * 
     * Current implementation returns a hardcoded rate, but this should be
     * enhanced to:
     * - Load rates from database (ParkingRates table)
     * - Support time-based rate variations (peak/off-peak)
     * - Cache rates in Redis for performance
     * - Allow runtime rate updates without code changes
     * 
     * Typical rate structure:
     * - BIKE: $5/hour (smaller vehicle, less space)
     * - CAR: $10/hour (standard rate)
     * - SUV: $12/hour (larger vehicle)
     * - TRUCK: $15/hour (largest space requirement)
     * - EV: $8/hour (incentive for electric vehicles)
     * 
     * Example enhancement:
     * private static BigDecimal getBaseRate(String vehicleType) {
     *     return rateRepository.findByVehicleType(vehicleType)
     *         .map(Rate::getHourlyRate)
     *         .orElse(BigDecimal.valueOf(10.0)); // default rate
     * }
     * 
     * @param vehicleType the type of vehicle (e.g., "CAR", "BIKE", "TRUCK")
     * @return the base hourly rate for the vehicle type (always non-negative)
     */
    private static BigDecimal getBaseRate(String vehicleType) {
        // TODO: Implement rate lookup based on vehicle type
        return BigDecimal.valueOf(10.0);
    }
}





