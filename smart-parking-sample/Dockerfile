# Multi-stage build to compile the application
FROM openjdk:17-slim AS build
WORKDIR /app

# Copy source code
COPY src /app/src
COPY libs /app/libs

# Create output directory
RUN mkdir out

# Compile Java files
# Note: This assumes libs/postgresql.jar or h2.jar exists if needed for compilation classpath
# For this simple sample, we compile without external libs in classpath if they are only runtime deps,
# but if Main depends on JDBC, we need the JDK's rt.jar (included) and standard SQL interfaces.
RUN javac -d out src/*.java

# Runtime stage
FROM openjdk:17-slim
WORKDIR /app

# Copy compiled classes and libraries
COPY --from=build /app/out /app/classes
COPY --from=build /app/libs /app/libs

# Set environment variables (can be overridden)
ENV JDBC_URL=jdbc:postgresql://db:5432/parking
ENV JDBC_USER=postgres
ENV JDBC_PASS=pass

# Run the application
CMD ["java", "-cp", "classes:libs/*", "Main"]
