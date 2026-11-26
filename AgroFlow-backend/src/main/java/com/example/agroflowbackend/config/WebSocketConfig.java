package com.example.agroflowbackend.config;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.service.AuthTokenService;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@NullMarked
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String allowedOrigin;
    private final AuthTokenService authTokenService;

    public WebSocketConfig(
            @Value("${app.cors.allowed-origin:http://localhost:5173}") String allowedOrigin,
            AuthTokenService authTokenService
    ) {
        this.allowedOrigin = allowedOrigin;
        this.authTokenService = authTokenService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigin);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new AuthenticatedWebSocketInterceptor());
    }

    private class AuthenticatedWebSocketInterceptor implements ChannelInterceptor {

        @Override
        public @Nullable Message<@NonNull ?> preSend(
                Message<@NonNull ?> message,
                MessageChannel channel
        ) {
            StompHeaderAccessor accessor =
                    MessageHeaderAccessor.getAccessor(
                            message, StompHeaderAccessor.class
                    );

            if (accessor == null) {
                return message;
            }

            StompCommand command = accessor.getCommand();

            if (StompCommand.CONNECT.equals(command)
                    || StompCommand.STOMP.equals(command)) {

                String authorizationHeader =
                        accessor.getFirstNativeHeader("Authorization");

                String token = authTokenService.extractToken(authorizationHeader)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Missing WebSocket authentication token."
                        ));

                AuthenticatedUser user = authTokenService.findByToken(token)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid WebSocket authentication token."
                        ));

                accessor.setUser(user);
                return message;
            }

            if (StompCommand.SUBSCRIBE.equals(command)) {

                if (!(accessor.getUser() instanceof AuthenticatedUser user)) {
                    throw new SecurityException("Authentication required.");
                }

                String destination = accessor.getDestination();

                boolean adminSubscription =
                        user.getRole() == UserRole.ADMIN
                                && "/topic/farms/admin".equals(destination);

                boolean ownerSubscription =
                        user.getRole() == UserRole.USER
                                && ("/topic/farms/user/" + user.getId())
                                .equals(destination);

                if (!adminSubscription && !ownerSubscription) {
                    throw new SecurityException(
                            "Unauthorized WebSocket subscription."
                    );
                }
            }

            if (StompCommand.SEND.equals(command)) {
                throw new SecurityException(
                        "Client message publishing is not allowed."
                );
            }

            return message;
        }
    }
}
