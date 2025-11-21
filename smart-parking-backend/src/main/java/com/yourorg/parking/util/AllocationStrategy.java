package com.yourorg.parking.util;

import com.yourorg.parking.model.ParkingSpot;
import java.util.List;

public interface AllocationStrategy {
    ParkingSpot allocateSpot(List<ParkingSpot> availableSpots, String vehicleType);
}





