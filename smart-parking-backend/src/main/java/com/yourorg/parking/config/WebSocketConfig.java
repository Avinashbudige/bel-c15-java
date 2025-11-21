package com.yourorg.parking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class for WebSocket messaging in the smart parking system.
 * 
 * This configuration enables real-time, bidirectional communication between
 * the server and connected clients (web browsers, mobile apps) using WebSockets
 * with the STOMP (Simple Text Oriented Messaging Protocol) protocol.
 * 
 * Purpose and benefits:
 * - Push real-time updates to clients without polling
 * - Notify users of parking spot availability changes
 * - Broadcast facility status updates
 * - Send notifications about check-in/check-out events
 * - Reduce server load compared to frequent polling
 * 
 * Use cases in the smart parking system:
 * 1. Real-time availability updates:
 *    - When a vehicle checks in, broadcast spot unavailability
 *    - When a vehicle checks out, broadcast spot availability
 *    - Update total available spots count
 * 
 * 2. User notifications:
 *    - Notify users when their transaction is processed
 *    - Send reminders or alerts
 *    - Update on payment processing status
 * 
 * 3. Facility monitoring:
 *    - Admin dashboard real-time updates
 *    - Digital signage updates for available spots
 *    - Capacity alerts and warnings
 * 
 * Configuration details:
 * - Message broker prefix: "/topic" (for pub-sub style messaging)
 * - Application destination prefix: "/app" (for client-to-server messages)
 * - WebSocket endpoint: "/ws" (connection URL)
 * - SockJS fallback enabled for older browsers
 * 
 * Example client subscription:
 * stompClient.subscribe('/topic/parking-updates', callback);
 * 
 * Example server broadcast:
 * messagingTemplate.convertAndSend("/topic/parking-updates", spotUpdateDto);
 * 
 * @author Smart Parking System
 * @version 1.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker for WebSocket communication.
     * 
     * This method sets up two key components:
     * 
     * 1. Simple in-memory message broker with prefix "/topic":
     *    - Handles pub-sub style messaging
     *    - Clients subscribe to topics like "/topic/parking-updates"
     *    - Server broadcasts messages to all subscribed clients
     *    - Suitable for simple use cases; for production, consider external
     *      message brokers like RabbitMQ or ActiveMQ for scalability
     * 
     * 2. Application destination prefix "/app":
     *    - Used for client-to-server messages
     *    - Messages sent to "/app/..." are routed to @MessageMapping methods
     *    - Example: client sends to "/app/request-update", server method handles it
     * 
     * Topic naming conventions:
     * - /topic/parking-updates: General parking facility updates
     * - /topic/spot-availability: Spot availability changes
     * - /topic/user/{userId}/notifications: User-specific notifications
     * 
     * @param config the message broker registry to configure
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers STOMP endpoints for WebSocket connections.
     * 
     * This method defines the WebSocket connection endpoint that clients
     * use to establish the initial connection.
     * 
     * Configuration details:
     * - Endpoint URL: "/ws" (clients connect to ws://host:port/ws)
     * - Allowed origins: "*" (all origins allowed - should be restricted in production)
     * - SockJS fallback: Enabled (provides compatibility with older browsers)
     * 
     * SockJS benefits:
     * - Automatic fallback to HTTP polling if WebSocket unavailable
     * - Cross-browser compatibility
     * - Handles proxy and firewall issues
     * 
     * Security considerations:
     * - In production, restrict allowed origins to known domains:
     *   registry.addEndpoint("/ws").setAllowedOrigins("https://your-domain.com")
     * - Consider adding authentication interceptors
     * - Implement rate limiting to prevent abuse
     * - Use WSS (WebSocket Secure) in production
     * 
     * Client connection example (JavaScript):
     * const socket = new SockJS('http://localhost:8080/ws');
     * const stompClient = Stomp.over(socket);
     * stompClient.connect({}, onConnected, onError);
     * 
     * @param registry the STOMP endpoint registry to configure
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}


