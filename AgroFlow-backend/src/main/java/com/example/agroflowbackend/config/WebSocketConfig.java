package com.example.agroflowbackend.config;

import com.example.agroflowbackend.domain.AuthenticatedUser;
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
//configurează WebSocket /ws, topic-uri /topic și auth pentru WebSocket.
@Configuration
@NullMarked
// Activeaza suportul Spring pentru WebSocket + STOMP.
// Pe scurt pentru profesor: folosim WebSocket ca frontend-ul sa primeasca update-uri
// in timp real, fara sa faca refresh sau polling repetat catre backend.
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Valoarea vine din application.properties: app.cors.allowed-origin.
    // Asa putem permite frontend-ul local, de exemplu http://localhost:5173,
    // fara sa hardcodam valoarea in mai multe locuri.
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
        // /topic este canalul pe care backend-ul publica mesaje catre clienti.
        // Exemplu concret: RealtimeNotificationService trimite notificari pe /topic/farms,
        // iar frontend-ul este abonat acolo si isi actualizeaza lista de ferme.
        registry.enableSimpleBroker("/topic");

        // /app ar fi prefixul pentru mesaje trimise din frontend catre metode @MessageMapping.
        // In cazul nostru fluxul principal este backend -> frontend, dar prefixul ramane
        // configurat pentru extensii viitoare.
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // /ws este endpoint-ul la care se conecteaza frontend-ul prin STOMP/WebSocket.
        // setAllowedOrigins este partea similara cu CORS pentru handshake-ul WebSocket:
        // backend-ul accepta conexiuni doar de la originea configurata.
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigin);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new AuthenticatedWebSocketInterceptor());
    }

    private class AuthenticatedWebSocketInterceptor implements ChannelInterceptor {

        @Override
        public @Nullable Message<@NonNull ?> preSend(Message<@NonNull ?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
                return message;
            }

            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            String token = authTokenService.extractToken(authorizationHeader)
                    .orElseThrow(() -> new IllegalArgumentException("Missing WebSocket authentication token."));
            AuthenticatedUser user = authTokenService.findByToken(token)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid WebSocket authentication token."));

            accessor.setUser(user);
            return message;
        }
    }
}
