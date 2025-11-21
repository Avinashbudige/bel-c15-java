package com.yourorg.parking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Smart Parking Management System backend application.
 * 
 * This Spring Boot application provides a comprehensive solution for managing
 * parking facilities, including vehicle check-in/check-out, spot allocation,
 * real-time availability updates, and fee calculation.
 * 
 * System Overview:
 * ================
 * The Smart Parking System is designed to automate and optimize parking facility
 * operations. It replaces traditional manual systems with a digital solution that
 * provides real-time information, automated billing, and efficient space utilization.
 * 
 * Key Features:
 * - Automated vehicle check-in and spot allocation
 * - Real-time parking spot availability tracking
 * - Dynamic fee calculation based on duration and vehicle type
 * - WebSocket-based live updates to connected clients
 * - Administrative dashboard for facility management
 * - Redis caching for high-performance operations
 * - PostgreSQL for reliable data persistence
 * - RESTful API for integration with mobile apps and web clients
 * 
 * Architecture Components:
 * 
 * 1. Model Layer (com.yourorg.parking.model):
 *    - Vehicle: Represents vehicles using the facility
 *    - ParkingSpot: Represents physical parking spaces
 *    - ParkingTransaction: Tracks parking sessions
 * 
 * 2. Repository Layer (com.yourorg.parking.repository):
 *    - JPA repositories for data access
 *    - Custom queries for complex operations
 *    - Transaction management
 * 
 * 3. Service Layer (com.yourorg.parking.service):
 *    - ParkingService: Core check-in/check-out logic
 *    - AllocationService: Spot allocation algorithms
 *    - Business rules and orchestration
 * 
 * 4. Controller Layer (com.yourorg.parking.controller):
 *    - ParkingController: Public API endpoints
 *    - AdminController: Administrative endpoints
 *    - REST API with JSON request/response
 * 
 * 5. Configuration Layer (com.yourorg.parking.config):
 *    - WebSocketConfig: Real-time messaging setup
 *    - RedisConfig: Caching configuration
 *    - Database and security configuration
 * 
 * 6. Utility Layer (com.yourorg.parking.util):
 *    - FeeCalculator: Parking fee computation
 *    - AllocationStrategy: Pluggable allocation algorithms
 * 
 * Technology Stack:
 * - Spring Boot 3.2.0: Application framework
 * - Spring Data JPA: Data persistence and ORM
 * - PostgreSQL: Relational database
 * - Redis: In-memory cache for performance
 * - WebSocket (STOMP): Real-time communication
 * - Flyway: Database migration management
 * - Lombok: Boilerplate code reduction
 * - Maven: Build and dependency management
 * 
 * Typical User Flow:
 * 
 * Check-in:
 * 1. Vehicle arrives at facility entrance
 * 2. User sends POST request to /api/parking/check-in with vehicle details
 * 3. System validates vehicle and finds available spot
 * 4. AllocationService selects optimal spot using allocation strategy
 * 5. System creates ACTIVE transaction and assigns spot
 * 6. Spot status updated to OCCUPIED
 * 7. Response includes assigned spot number and transaction ID
 * 8. Real-time update broadcast via WebSocket to all clients
 * 
 * Check-out:
 * 1. Vehicle ready to leave
 * 2. User sends POST request to /api/parking/check-out/{transactionId}
 * 3. System retrieves active transaction
 * 4. FeeCalculator computes parking fee based on duration
 * 5. Transaction updated with checkout time and fee
 * 6. Spot status changed back to AVAILABLE
 * 7. Response includes parking summary and total fee
 * 8. Real-time update broadcast via WebSocket
 * 
 * Configuration:
 * Application configuration is managed through:
 * - src/main/resources/application.yml: Main configuration
 * - Environment variables: Override for different environments
 * - Database connection settings
 * - Redis connection settings
 * - Server port and context path
 * - Logging configuration
 * 
 * Running the Application:
 * - Development: mvn spring-boot:run
 * - Production: java -jar smart-parking-backend.jar
 * - Docker: docker-compose up
 * 
 * Prerequisites:
 * - Java 17 or higher
 * - PostgreSQL 12 or higher
 * - Redis 6 or higher
 * - Maven 3.6 or higher (for building)
 * 
 * Default Endpoints:
 * - API Base: http://localhost:8080/api
 * - WebSocket: ws://localhost:8080/ws
 * - Health Check: http://localhost:8080/actuator/health (if actuator enabled)
 * - Swagger UI: http://localhost:8080/swagger-ui.html (if configured)
 * 
 * Future Enhancements:
 * - Payment gateway integration
 * - License plate recognition (LPR) camera integration
 * - Mobile app (iOS/Android)
 * - Reservation system
 * - Dynamic pricing based on demand
 * - Analytics and reporting dashboard
 * - Multi-facility support
 * - User accounts and loyalty programs
 * 
 * @author Smart Parking System
 * @version 1.0
 * @see <a href="README.md">Project README</a>
 */
@SpringBootApplication
public class SmartParkingApplication {

    /**
     * Main method to bootstrap the Spring Boot application.
     * 
     * This method:
     * 1. Initializes the Spring application context
     * 2. Scans for components, configurations, and entities
     * 3. Auto-configures beans based on dependencies
     * 4. Starts the embedded web server (default: Tomcat on port 8080)
     * 5. Initializes database connections (PostgreSQL)
     * 6. Establishes Redis connection
     * 7. Runs Flyway migrations if configured
     * 8. Registers REST endpoints
     * 9. Sets up WebSocket endpoints
     * 
     * Spring Boot auto-configuration handles:
     * - DataSource configuration
     * - JPA/Hibernate setup
     * - Transaction management
     * - Web MVC configuration
     * - Error handling
     * - Logging setup
     * 
     * Application startup sequence:
     * 1. Load application.yml configuration
     * 2. Initialize Spring context
     * 3. Connect to PostgreSQL and run migrations
     * 4. Connect to Redis
     * 5. Register beans and components
     * 6. Start embedded server
     * 7. Log startup completion message
     * 
     * Shutdown:
     * Graceful shutdown is handled by Spring Boot, ensuring:
     * - Active transactions complete
     * - Database connections close properly
     * - Redis connections close
     * - WebSocket connections terminate
     * 
     * @param args command-line arguments (can specify profiles, properties, etc.)
     */
    public static void main(String[] args) {
        SpringApplication.run(SmartParkingApplication.class, args);
    }
}


