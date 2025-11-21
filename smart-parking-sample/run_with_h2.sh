#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIBS_DIR="$ROOT_DIR/libs"
OUT_DIR="$ROOT_DIR/out"
mkdir -p "$LIBS_DIR" "$OUT_DIR"

# 1. Download H2 Driver
H2_JAR="$LIBS_DIR/h2.jar"
if [ ! -f "$H2_JAR" ]; then
  echo "Downloading H2 JDBC driver..."
  wget -q -O "$H2_JAR" https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar || {
    echo "Failed to download H2 driver."; exit 1;
  }
fi

# 2. Compile Java
echo "Compiling Java..."
# Clean up previous build artifacts to ensure a fresh compile
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

SOURCES_LIST="$OUT_DIR/sources.txt"
find "$ROOT_DIR/src" -name "*.java" > "$SOURCES_LIST"

if [ ! -s "$SOURCES_LIST" ]; then
  echo "Error: No Java source files found in $ROOT_DIR/src."
  exit 1
fi

javac -d "$OUT_DIR" @"$SOURCES_LIST"

# 3. Prepare Database
DB_PATH="$OUT_DIR/parking_h2"
JDBC_URL="jdbc:h2:$DB_PATH;MODE=PostgreSQL"
MIGRATION_FILE="$ROOT_DIR/migrations/h2_schema.sql"

# Clean up previous run to ensure schema is applied cleanly
rm -f "${DB_PATH}.mv.db"

if [ -f "$MIGRATION_FILE" ]; then
  echo "Applying H2 migration..."
  java -cp "$H2_JAR" org.h2.tools.RunScript \
    -url "$JDBC_URL" \
    -user sa \
    -password "" \
    -script "$MIGRATION_FILE"
else
  echo "Warning: Migration file not found at $MIGRATION_FILE"
fi

# 4. Run Application
echo "Running service with H2..."
export USE_DB=true
export JDBC_URL="$JDBC_URL"
export JDBC_USER=sa
export JDBC_PASS=""

java -cp "$OUT_DIR:$H2_JAR" Main
