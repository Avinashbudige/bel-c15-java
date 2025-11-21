package com.yourorg.parking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for Redis caching in the smart parking system.
 * 
 * This configuration sets up Redis as a high-performance in-memory cache
 * to improve system performance and scalability. Redis is used throughout
 * the application for various caching and real-time data scenarios.
 * 
 * Why Redis in the parking system:
 * 
 * 1. Performance optimization:
 *    - Cache frequently accessed data (available spots, spot details)
 *    - Reduce database queries for read-heavy operations
 *    - Sub-millisecond response times for cached data
 * 
 * 2. Real-time spot availability:
 *    - Maintain live count of available spots by type
 *    - Quick lookups without querying database
 *    - Support high-concurrency check-in operations
 * 
 * 3. Session management:
 *    - Store user session data
 *    - Track active connections
 *    - Manage temporary reservations
 * 
 * 4. Rate limiting:
 *    - Implement API rate limiting per user/IP
 *    - Prevent abuse and ensure fair usage
 * 
 * 5. Distributed locking:
 *    - Prevent race conditions during spot allocation
 *    - Ensure only one vehicle can claim a spot
 *    - Handle concurrent check-in requests safely
 * 
 * Cache key patterns used in the system:
 * - "spot:available:{vehicleType}": List of available spot IDs by type
 * - "spot:count:{status}": Count of spots by status
 * - "transaction:active:{vehicleId}": Active transaction for a vehicle
 * - "rate-limit:{userId}:{endpoint}": API rate limiting counters
 * 
 * Cache expiration strategies:
 * - Spot availability: Updated on check-in/check-out, no expiration
 * - Session data: TTL based on session timeout (e.g., 30 minutes)
 * - Rate limit counters: TTL based on rate limit window (e.g., 1 minute)
 * 
 * @author Smart Parking System
 * @version 1.0
 */
@Configuration
public class RedisConfig {

    /**
     * Creates and configures a RedisTemplate bean for Redis operations.
     * 
     * RedisTemplate is the central class for interacting with Redis,
     * providing methods for various data structures (strings, lists,
     * sets, sorted sets, hashes, etc.).
     * 
     * Serialization configuration:
     * - Key serializer: StringRedisSerializer
     *   - Keys are stored as strings (human-readable in Redis)
     *   - Example key: "spot:available:CAR"
     * 
     * - Value serializer: GenericJackson2JsonRedisSerializer
     *   - Values are serialized to JSON format
     *   - Allows storing complex objects (POJOs)
     *   - Maintains type information for deserialization
     *   - Human-readable when inspecting Redis
     * 
     * Usage examples in the application:
     * 
     * 1. Cache available spots:
     *    redisTemplate.opsForList().rightPushAll("spot:available:CAR", spotIds);
     * 
     * 2. Get cached spots:
     *    List<Long> spotIds = redisTemplate.opsForList().range("spot:available:CAR", 0, -1);
     * 
     * 3. Cache spot details:
     *    redisTemplate.opsForValue().set("spot:details:" + spotId, parkingSpot);
     * 
     * 4. Increment counter:
     *    redisTemplate.opsForValue().increment("spot:count:AVAILABLE");
     * 
     * 5. Set with expiration:
     *    redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(30));
     * 
     * Connection pooling:
     * - RedisConnectionFactory (injected) manages connection pooling
     * - Configured via application.yml (host, port, pool size, etc.)
     * - Default: localhost:6379
     * 
     * Best practices:
     * - Always set appropriate TTL for time-sensitive data
     * - Use pipeline for bulk operations
     * - Handle Redis connection failures gracefully
     * - Monitor Redis memory usage and eviction policy
     * 
     * @param connectionFactory the Redis connection factory (auto-configured by Spring)
     * @return configured RedisTemplate instance ready for use
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}


