package com.yourorg.parking.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for vehicle check-in requests.
 * 
 * This class encapsulates the information required when a vehicle
 * enters the parking facility and requests a parking spot. The system
 * uses this data to:
 * 1. Identify or create a vehicle record
 * 2. Determine the appropriate parking spot based on vehicle type
 * 3. Create a new parking transaction
 * 
 * The check-in process typically involves:
 * - Validating the vehicle information
 * - Finding an available spot matching the vehicle type
 * - Allocating the spot to the vehicle
 * - Creating an ACTIVE transaction
 * - Returning the assigned spot information to the user
 * 
 * @author Smart Parking System
 * @version 1.0
 */
@Data
public class CheckInRequest {
    
    /**
     * Vehicle registration number (license plate).
     * Required field used to uniquely identify the vehicle.
     * If this is a new vehicle, a record will be created.
     */
    private String vehicleNumber;
    
    /**
     * Type of vehicle requesting parking.
     * Required field that determines which parking spots are eligible.
     * Common values: CAR, BIKE, TRUCK, SUV
     * Must match available spot types in the system.
     */
    private String vehicleType;
    
    // Add other fields as needed (e.g., ownerName, ownerContact, preferredFloor)
}





