package com.yourorg.parking.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "parking_spots")
@Data
public class ParkingSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String spotNumber;

    @Column(nullable = false)
    private String vehicleType; // CAR, BIKE, TRUCK, etc.

    @Column(nullable = false)
    private String status; // AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE

    @Column(nullable = false)
    private String floor;

    @Column(nullable = false)
    private String zone;
}





