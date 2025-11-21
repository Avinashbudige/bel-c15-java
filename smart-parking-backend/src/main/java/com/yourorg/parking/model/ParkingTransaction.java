package com.yourorg.parking.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_transactions")
@Data
public class ParkingTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "spot_id", nullable = false)
    private ParkingSpot parkingSpot;

    @Column(nullable = false)
    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private BigDecimal totalFee;

    @Column(nullable = false)
    private String status; // ACTIVE, COMPLETED, CANCELLED
}





