package com.yourorg.parking.controller;

import com.yourorg.parking.dto.CheckInRequest;
import com.yourorg.parking.dto.CheckOutResponse;
import com.yourorg.parking.service.ParkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestBody CheckInRequest request) {
        // TODO: Implement check-in logic
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-out/{transactionId}")
    public ResponseEntity<CheckOutResponse> checkOut(@PathVariable Long transactionId) {
        // TODO: Implement check-out logic
        return ResponseEntity.ok().build();
    }

    @GetMapping("/spots/available")
    public ResponseEntity<?> getAvailableSpots() {
        // TODO: Implement get available spots
        return ResponseEntity.ok().build();
    }
}


