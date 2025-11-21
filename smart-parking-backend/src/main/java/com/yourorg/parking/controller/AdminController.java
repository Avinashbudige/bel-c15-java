package com.yourorg.parking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/spots")
    public ResponseEntity<?> getAllSpots() {
        // TODO: Implement get all spots
        return ResponseEntity.ok().build();
    }

    @PostMapping("/spots")
    public ResponseEntity<?> createSpot(@RequestBody Object spotRequest) {
        // TODO: Implement create spot
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions() {
        // TODO: Implement get all transactions
        return ResponseEntity.ok().build();
    }
}





