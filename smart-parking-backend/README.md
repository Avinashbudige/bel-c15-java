# Smart Parking Backend

Smart Parking Management System Backend Application

## Overview

This is a comprehensive Spring Boot application for managing smart parking operations. The system automates parking facility management with features including automated vehicle check-in/check-out, intelligent spot allocation, real-time availability updates, and dynamic fee calculation.

### Key Features

- **Automated Check-in/Check-out**: Seamless vehicle entry and exit with spot assignment
- **Intelligent Spot Allocation**: Pluggable allocation strategies (nearest, load balancing, etc.)
- **Real-time Updates**: WebSocket-based live notifications to connected clients
- **Dynamic Fee Calculation**: Time-based parking fees with vehicle type differentiation
- **High Performance**: Redis caching for fast spot availability queries
- **Scalable Architecture**: Microservice-ready with clear separation of concerns
- **Administrative Tools**: Comprehensive facility management and reporting APIs

### System Architecture

The application follows a layered architecture pattern:

```
┌─────────────────────────────────────────────────┐
│            Client Layer                          │
│  (Web App, Mobile App, Admin Dashboard)         │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│         Controller Layer (REST + WebSocket)      │
│  - ParkingController (public API)                │
│  - AdminController (admin operations)            │
│  - WebSocket endpoints (real-time updates)       │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│            Service Layer                         │
│  - ParkingService (core business logic)          │
│  - AllocationService (spot allocation)           │
│  - Business rules and orchestration              │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│         Repository Layer (Data Access)           │
│  - VehicleRepository                             │
│  - ParkingSpotRepository                         │
│  - ParkingTransactionRepository                  │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│          Data Layer                              │
│  - PostgreSQL (persistent storage)               │
│  - Redis (caching & real-time data)              │
└─────────────────────────────────────────────────┘
```

### Data Model

**Vehicle**: Represents vehicles using the parking facility
- Tracks vehicle registration, type, and owner information
- Used for identification and transaction history

**ParkingSpot**: Represents physical parking spaces
- Organized by floor, zone, and vehicle type compatibility
- Status tracking: AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE

**ParkingTransaction**: Records parking sessions
- Links vehicle to assigned spot
- Tracks check-in/check-out times and calculated fees
- Status: ACTIVE, COMPLETED, CANCELLED

## Tech Stack

- Java 17
- Spring Boot 3.2.0
- PostgreSQL
- Redis
- WebSocket
- Flyway (Database Migrations)

## Project Structure

```
smart-parking-backend/
├─ src/main/java/com/yourorg/parking/
│  ├─ SmartParkingApplication.java
│  ├─ config/          # Configuration classes
│  ├─ controller/      # REST controllers
│  ├─ dto/            # Data Transfer Objects
│  ├─ service/        # Business logic
│  ├─ repository/     # Data access layer
│  ├─ model/          # Entity models
│  └─ util/           # Utility classes
├─ src/main/resources/
│  ├─ application.yml
│  └─ db/migrations/  # Database migration scripts
└─ docker/            # Docker configuration
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- PostgreSQL
- Redis

### Running the Application

```bash
mvn spring-boot:run
```

### Docker

```bash
docker-compose up
```

## API Documentation

### Public Endpoints

**POST /api/parking/check-in**
- Initiates a new parking session
- Request: `{ "vehicleNumber": "ABC-1234", "vehicleType": "CAR" }`
- Response: Assigned spot details and transaction ID
- Returns HTTP 201 on success

**POST /api/parking/check-out/{transactionId}**
- Completes a parking session
- Response: Parking summary with duration and total fee
- Returns HTTP 200 on success

**GET /api/parking/spots/available**
- Queries available parking spots
- Optional query params: vehicleType, floor, zone
- Returns list of available spots

### Admin Endpoints

**GET /api/admin/spots**
- Retrieves all parking spots with their current status
- Requires admin authentication

**POST /api/admin/spots**
- Creates new parking spot(s)
- Request: `{ "spotNumber": "A1-101", "vehicleType": "CAR", "floor": "1", "zone": "A" }`

**GET /api/admin/transactions**
- Retrieves all parking transactions
- Supports filtering and pagination

### WebSocket Endpoint

**WS /ws**
- Real-time connection endpoint using STOMP protocol
- Subscribe to `/topic/parking-updates` for live availability updates
- Subscribe to `/topic/spot-availability` for spot status changes

## Design Patterns

The application leverages several design patterns for maintainability and extensibility:

### Strategy Pattern
- **AllocationStrategy interface**: Enables pluggable spot allocation algorithms
- Implementations: NearestToEntrance, LoadBalancing, FirstAvailable, etc.
- Allows runtime algorithm selection based on business needs

### Repository Pattern
- Abstracts data access logic from business logic
- Spring Data JPA provides automatic implementation
- Enables easy testing with mock repositories

### Dependency Injection
- Constructor-based injection for required dependencies
- Promotes loose coupling and testability
- Managed by Spring IoC container

### Layered Architecture
- Clear separation: Controller → Service → Repository → Data
- Each layer has specific responsibilities
- Improves maintainability and testability

## Performance Optimizations

### Redis Caching
- Cache available spots by vehicle type
- Store spot availability counts
- Implement distributed locking for concurrent operations
- Session management and rate limiting

### Database Indexing
- Indexes on frequently queried fields:
  - Vehicle.vehicleNumber
  - ParkingSpot.spotNumber
  - ParkingSpot.vehicleType + status
  - ParkingTransaction.vehicleId

### WebSocket for Real-time Updates
- Push-based notifications instead of polling
- Reduces server load
- Provides instant updates to clients

## Future Enhancements

API documentation (Swagger/OpenAPI) will be available at `/swagger-ui.html` once configured.


