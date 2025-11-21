# Smart Parking System Assignment

This project implements the low-level architecture for a backend system of a smart parking lot.

## Features Implemented
- **Parking Spot Allocation**: Automatically assigns spots based on vehicle size (SMALL, MEDIUM, LARGE).
- **Concurrency Handling**: Uses SQL `FOR UPDATE` locking to prevent race conditions when multiple vehicles enter simultaneously.
- **Fee Calculation**: Calculates fees based on duration and vehicle type upon exit.
- **Real-Time Availability**: Updates spot status transactionally.

## Tech Stack
- **Language**: Java 17 (Pure JDBC, no frameworks)
- **Database**: H2 (Embedded) or PostgreSQL (Docker)
- **Interface**: Command Line Interface (CLI)

## How to Run

### Option 1: Quick Start (H2 Database)
Uses an embedded database. Data is reset on each run.

```bash
cd smart-parking-sample
chmod +x run_with_h2.sh
./run_with_h2.sh
```

### Option 2: Production Mode (PostgreSQL)
Requires Docker. Data persists in the container.

```bash
cd smart-parking-sample
chmod +x run_with_postgres.sh
./run_with_postgres.sh
```

## Database Schema
- `parking_spot`: Stores static info (floor, spot number, size) and dynamic status (AVAILABLE/OCCUPIED).
- `parking_ticket`: Stores transaction info (entry time, exit time, fee).

## Usage
Follow the on-screen prompts to:
1. Park a vehicle (Check-In)
2. Unpark a vehicle (Check-Out)
3. View availability
