#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="$ROOT_DIR/out"
SRC_DIR="$ROOT_DIR/src"
LIBS_DIR="$ROOT_DIR/libs"
mkdir -p "$OUT_DIR" "$LIBS_DIR" "$ROOT_DIR/data"

# Assignment requires Database persistence, defaulting to H2
DB_TYPE="h2"

echo "Compiling..."
javac -d "$OUT_DIR" "$SRC_DIR"/*.java

if [ "$DB_TYPE" = "h2" ]; then
	H2_JAR="$LIBS_DIR/h2.jar"
	if [ ! -f "$H2_JAR" ]; then
		echo "Downloading H2 driver..."
		wget -q -O "$H2_JAR" https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar || {
			echo "Failed to download H2 jar. Please provide it at $H2_JAR"; exit 1;
		}
	fi

	# Run H2 migration
	echo "Applying H2 migration..."
	# Updated path to match the schema created for the assignment
	java -cp "$OUT_DIR:$H2_JAR" org.h2.tools.RunScript -url "jdbc:h2:./data/parking;MODE=PostgreSQL;DB_CLOSE_DELAY=-1" -user sa -script "$ROOT_DIR/migrations/h2_schema.sql"

	# Start server with H2
	export USE_DB=true
	export JDBC_URL="jdbc:h2:./data/parking;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
	export JDBC_USER=sa
	export JDBC_PASS=
	echo "Running with H2 (USE_DB=true) ..."
	java -cp "$OUT_DIR:$H2_JAR" Main
fi
