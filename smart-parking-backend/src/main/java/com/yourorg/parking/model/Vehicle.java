package com.yourorg.parking.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vehicles")
@Data
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String vehicleType; // CAR, BIKE, TRUCK, etc.

    private String ownerName;
    private String ownerContact;
}





