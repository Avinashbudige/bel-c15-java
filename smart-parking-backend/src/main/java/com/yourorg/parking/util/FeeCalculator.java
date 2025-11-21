package com.yourorg.parking.util;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class FeeCalculator {

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

    private static BigDecimal getBaseRate(String vehicleType) {
        // TODO: Implement rate lookup based on vehicle type
        return BigDecimal.valueOf(10.0);
    }
}





