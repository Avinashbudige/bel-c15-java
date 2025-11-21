-- V1: Create core tables for parking system
-- Run on PostgreSQL or compatible RDBMS

CREATE TABLE parking_spot (
  id BIGSERIAL PRIMARY KEY,
  floor INT NOT NULL,
  zone TEXT,
  spot_number TEXT,
  size TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'AVAILABLE',
  sensor_id TEXT,
  metadata JSONB,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  version INT NOT NULL DEFAULT 1
);

CREATE INDEX idx_parking_spot_status_size_floor ON parking_spot (status, size, floor);

CREATE TABLE vehicle (
  id BIGSERIAL PRIMARY KEY,
  plate_number TEXT UNIQUE NOT NULL,
  type TEXT NOT NULL,
  owner TEXT,
  metadata JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE parking_session (
  id BIGSERIAL PRIMARY KEY,
  vehicle_id BIGINT REFERENCES vehicle(id),
  spot_id BIGINT REFERENCES parking_spot(id),
  entry_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  exit_time TIMESTAMP WITH TIME ZONE,
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  reserved_at TIMESTAMP WITH TIME ZONE,
  fee_cents BIGINT,
  payment_status TEXT,
  metadata JSONB
);

CREATE INDEX idx_parking_session_vehicle_status ON parking_session (vehicle_id, status);

CREATE TABLE pricing_policy (
  id BIGSERIAL PRIMARY KEY,
  vehicle_type TEXT NOT NULL,
  grace_period_minutes INT DEFAULT 0,
  unit TEXT NOT NULL, -- MINUTE or HOUR
  rate_per_unit_cents BIGINT NOT NULL,
  rounding TEXT DEFAULT 'UP',
  daily_cap_cents BIGINT,
  effective_from TIMESTAMP WITH TIME ZONE DEFAULT now(),
  effective_to TIMESTAMP WITH TIME ZONE
);

-- sample seed data
INSERT INTO pricing_policy (vehicle_type, grace_period_minutes, unit, rate_per_unit_cents, daily_cap_cents)
VALUES
('MOTORCYCLE', 15, 'HOUR', 100, NULL),
('CAR', 15, 'HOUR', 300, NULL),
('BUS', 0, 'HOUR', 500, NULL);
