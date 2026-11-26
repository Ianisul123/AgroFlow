package com.example.agroflowbackend.config;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.service.AuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.ExecutorSubscribableChannel;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebSocketConfigTest {

    private AuthTokenService authTokenService;
    private ChannelInterceptor interceptor;
    private ExecutorSubscribableChannel channel;

    private final AuthenticatedUser user =
            new AuthenticatedUser(
                    10L,
                    "Test User",
                    "user@example.com",
                    UserRole.USER
            );

    private final AuthenticatedUser admin =
            new AuthenticatedUser(
                    1L,
                    "Test Admin",
                    "admin@example.com",
                    UserRole.ADMIN
            );

    @BeforeEach
    void setUp() {
        authTokenService = mock(AuthTokenService.class);

        WebSocketConfig config = new WebSocketConfig(
                "http://localhost:5173",
                authTokenService
        );

        ChannelRegistration registration =
                mock(ChannelRegistration.class);

        config.configureClientInboundChannel(registration);

        org.mockito.ArgumentCaptor<ChannelInterceptor> captor =
                org.mockito.ArgumentCaptor.forClass(
                        ChannelInterceptor.class
                );

        verify(registration).interceptors(captor.capture());

        interceptor = captor.getValue();
        channel = new ExecutorSubscribableChannel();
    }

    @Test
    void connectWithValidTokenShouldBeAllowed() {
        when(authTokenService.extractToken("Bearer valid-token"))
                .thenReturn(Optional.of("valid-token"));

        when(authTokenService.findByToken("valid-token"))
                .thenReturn(Optional.of(user));

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);

        accessor.setNativeHeader(
                "Authorization",
                "Bearer valid-token"
        );

        Message<byte[]> message = buildMessage(accessor);

        assertDoesNotThrow(() ->
                interceptor.preSend(message, channel)
        );

        assertEquals(user, accessor.getUser());
    }

    @Test
    void connectWithoutTokenShouldBeRejected() {
        when(authTokenService.extractToken(null))
                .thenReturn(Optional.empty());

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);

        assertThrows(IllegalArgumentException.class, () ->
                interceptor.preSend(buildMessage(accessor), channel)
        );
    }

    @Test
    void connectWithInvalidTokenShouldBeRejected() {
        when(authTokenService.extractToken("Bearer invalid-token"))
                .thenReturn(Optional.of("invalid-token"));

        when(authTokenService.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);

        accessor.setNativeHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        assertThrows(IllegalArgumentException.class, () ->
                interceptor.preSend(buildMessage(accessor), channel)
        );
    }

    @Test
    void userShouldSubscribeToOwnTopic() {
        Message<byte[]> message =
                subscription(user, "/topic/farms/user/10");

        assertDoesNotThrow(() ->
                interceptor.preSend(message, channel)
        );
    }

    @Test
    void userShouldNotSubscribeToAnotherUsersTopic() {
        Message<byte[]> message =
                subscription(user, "/topic/farms/user/20");

        assertThrows(SecurityException.class, () ->
                interceptor.preSend(message, channel)
        );
    }

    @Test
    void userShouldNotSubscribeToAdminTopic() {
        Message<byte[]> message =
                subscription(user, "/topic/farms/admin");

        assertThrows(SecurityException.class, () ->
                interceptor.preSend(message, channel)
        );
    }

    @Test
    void adminShouldSubscribeToAdminTopic() {
        Message<byte[]> message =
                subscription(admin, "/topic/farms/admin");

        assertDoesNotThrow(() ->
                interceptor.preSend(message, channel)
        );
    }

    @Test
    void unauthenticatedSubscriptionShouldBeRejected() {
        Message<byte[]> message =
                subscription(null, "/topic/farms/user/10");

        assertThrows(SecurityException.class, () ->
                interceptor.preSend(message, channel)
        );
    }

    @Test
    void oldPublicTopicShouldBeRejected() {
        Message<byte[]> message =
                subscription(user, "/topic/farms");

        assertThrows(SecurityException.class, () ->
                interceptor.preSend(message, channel)
        );
    }

    @Test
    void clientPublishingShouldBeRejected() {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.SEND);

        accessor.setUser(user);
        accessor.setDestination("/topic/farms/user/10");

        assertThrows(SecurityException.class, () ->
                interceptor.preSend(buildMessage(accessor), channel)
        );
    }

    private Message<byte[]> subscription(
            AuthenticatedUser authenticatedUser,
            String destination
    ) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        accessor.setUser(authenticatedUser);
        accessor.setDestination(destination);
        accessor.setSubscriptionId("test-subscription");

        return buildMessage(accessor);
    }

    private Message<byte[]> buildMessage(
            StompHeaderAccessor accessor
    ) {
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );
    }
}
