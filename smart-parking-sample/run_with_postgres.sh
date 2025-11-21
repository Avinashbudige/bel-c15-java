#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIBS_DIR="$ROOT_DIR/libs"
OUT_DIR="$ROOT_DIR/out"
mkdir -p "$LIBS_DIR" "$OUT_DIR"

PG_JAR="$LIBS_DIR/postgresql.jar"
if [ ! -f "$PG_JAR" ]; then
  echo "Downloading Postgres JDBC driver..."
  wget -q -O "$PG_JAR" https://repo1.maven.org/maven2/org/postgresql/postgresql/42.6.0/postgresql-42.6.0.jar || {
    echo "Failed to download Postgres JDBC driver. Please add it to $PG_JAR"; exit 1;
  }
fi

echo "Starting Postgres via docker-compose..."
docker compose -f "$ROOT_DIR/docker-compose.yml" up -d db

echo "Waiting for Postgres to be ready..."
until docker exec $(docker compose -f "$ROOT_DIR/docker-compose.yml" ps -q db) pg_isready -U postgres >/dev/null 2>&1; do
  sleep 1
done

echo "Applying migration inside container..."
DB_CONTAINER=$(docker compose -f "$ROOT_DIR/docker-compose.yml" ps -q db)
docker exec -i $DB_CONTAINER psql -U postgres -d parking -f /migrations/V1__create_schema.sql

echo "Compiling Java..."
javac -d "$OUT_DIR" "$ROOT_DIR/src"/*.java

echo "Running service connected to Postgres..."
export USE_DB=true
export JDBC_URL="jdbc:postgresql://localhost:5432/parking"
export JDBC_USER=postgres
export JDBC_PASS=pass

java -cp "$OUT_DIR:$PG_JAR" Main
