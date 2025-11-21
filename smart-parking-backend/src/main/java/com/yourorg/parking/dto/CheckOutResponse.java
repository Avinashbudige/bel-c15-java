package com.yourorg.parking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CheckOutResponse {
    private Long transactionId;
    private String vehicleNumber;
    private String spotNumber;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private BigDecimal totalFee;
    // Add other fields as needed
}





