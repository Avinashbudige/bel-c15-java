package com.yourorg.parking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for vehicle check-out responses.
 * 
 * This class encapsulates the complete parking session information
 * returned when a vehicle checks out. It provides the user with:
 * - A summary of their parking session
 * - The calculated parking fee
 * - Timestamps for duration calculation
 * - Transaction receipt information
 * 
 * This response is used for:
 * - Displaying parking summary to the user
 * - Processing payment
 * - Generating receipts
 * - Audit and reporting purposes
 * 
 * The check-out process that generates this response involves:
 * 1. Recording the check-out time
 * 2. Calculating parking duration
 * 3. Computing the parking fee based on duration and vehicle type
 * 4. Updating transaction status to COMPLETED
 * 5. Marking the parking spot as AVAILABLE
 * 
 * @author Smart Parking System
 * @version 1.0
 */
@Data
public class CheckOutResponse {
    
    /**
     * Unique identifier of the parking transaction.
     * Used for payment processing, receipt generation, and audit trails.
     */
    private Long transactionId;
    
    /**
     * Vehicle registration number (license plate).
     * Identifies which vehicle is checking out.
     */
    private String vehicleNumber;
    
    /**
     * Parking spot identifier where the vehicle was parked.
     * Helps users confirm they're checking out from the correct spot.
     */
    private String spotNumber;
    
    /**
     * Timestamp when the vehicle checked in.
     * Used to display the start of the parking session.
     */
    private LocalDateTime checkInTime;
    
    /**
     * Timestamp when the vehicle checked out.
     * Used to display the end of the parking session.
     * Duration = checkOutTime - checkInTime
     */
    private LocalDateTime checkOutTime;
    
    /**
     * Total parking fee charged for this session.
     * Calculated based on:
     * - Parking duration (checkOutTime - checkInTime)
     * - Vehicle type (different types may have different rates)
     * - Rate structure (per hour, with potential discounts for longer stays)
     */
    private BigDecimal totalFee;
    
    // Add other fields as needed (e.g., paymentStatus, receiptUrl, parkingDuration)
}





