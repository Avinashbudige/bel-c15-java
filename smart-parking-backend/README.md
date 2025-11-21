# Smart Parking Backend

Smart Parking Management System Backend Application

## Overview

This is a Spring Boot application for managing smart parking operations including vehicle check-in, check-out, spot allocation, and real-time updates.

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

API documentation will be available at `/swagger-ui.html` once Swagger is configured.


