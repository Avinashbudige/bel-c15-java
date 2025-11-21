# Smart Parking Sample Service

This is a tiny, self-contained Java microservice demonstrating parking spot allocation, check-in/check-out, fee calculation, and real-time availability. It is intentionally lightweight and uses the JDK HTTP server (no external frameworks required).

Build & run (requires JDK 11+):

````markdown
# Smart Parking Sample Service

This is a tiny, self-contained Java microservice demonstrating parking spot allocation, check-in/check-out, fee calculation, and real-time availability. It is intentionally lightweight and uses the JDK HTTP server (no external frameworks required).

Build & run (requires JDK 11+):

```bash
cd smart-parking-sample
./build_and_run.sh
```

API endpoints (examples):

- POST /entry?plate=ABC123&type=CAR&preferredFloor=1
  - Response: JSON with `sessionId`, `spotId`, `floor` and `allocatedAt`.
- POST /exit?sessionId=1
  - Response: JSON with `sessionId`, `durationMinutes`, `feeCents`, `closedAt`.
- GET /availability
  - Response: JSON with free counts per floor and per spot size.

Notes:
- This sample keeps all data in-memory for simplicity. It's designed to illustrate the allocation algorithm, fee logic, and concurrency-safe operations.
- For production use, persist models to a database (Postgres), add Redis caching, and secure the APIs.

DB-backed run (next step)
- Highest priority for a production-capable system: implement persistent, concurrency-safe spot allocation. That requires a database schema and allocation logic using row-level locking (`SELECT ... FOR UPDATE SKIP LOCKED`) or atomic cache ops.
- I added a SQL migration at `db/migration/V1__create_schema.sql` (Postgres-compatible). To move from the in-memory sample to a DB-backed service you can:
  1. Run the migration against Postgres: `psql -d yourdb -f db/migration/V1__create_schema.sql`.
  2. Implement a JDBC/Postgres repository that performs allocation with `SELECT ... FOR UPDATE SKIP LOCKED` to safely claim a spot.
  3. Replace `InMemoryRepository` with the DB-backed implementation and re-run the server.

If you'd like, I can implement the DB-backed repository and allocation logic next (H2 for local demo or Postgres for production). Which DB would you prefer for the demo: `H2` (no external setup) or `Postgres` (production-like)?

Postgres quick start (recommended)
- A docker-compose file and helper script are included to run Postgres and the service together.
- Start Postgres, apply migrations and run the service with:

```bash
cd smart-parking-sample
chmod +x run_with_postgres.sh
./run_with_postgres.sh
```

This will:
- start Postgres via `docker compose` (container mounts `db/migration`)
- apply `db/migration/V1__create_schema.sql` inside the container
- download the Postgres JDBC driver to `libs/postgresql.jar`
- compile and run the Java service connected to Postgres


````
