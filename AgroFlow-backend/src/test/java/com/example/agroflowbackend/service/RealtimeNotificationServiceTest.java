package com.example.agroflowbackend.service;

import com.example.agroflowbackend.domain.Farm;
import com.example.agroflowbackend.dto.RealtimeNotificationResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class RealtimeNotificationServiceTest {

    private static final String ADMIN_TOPIC = "/topic/farms/admin";
    private static final String USER_TOPIC_PREFIX = "/topic/farms/user/";

    @Test
    void notifyFarmCreatedShouldPublishToAdminAndOwner() {
        SimpMessagingTemplate messagingTemplate =
                mock(SimpMessagingTemplate.class);

        RealtimeNotificationService notificationService =
                new RealtimeNotificationService(messagingTemplate);

        Farm farm = new Farm(1L, "North Field", "Eindhoven", 10L);

        notificationService.notifyFarmCreated(farm);

        RealtimeNotificationResponse notification =
                captureNotification(messagingTemplate, 10L);

        assertEquals("FARM_CREATED", notification.getType());
        assertEquals("FARM", notification.getResourceType());
        assertEquals(1L, notification.getResourceId());
        assertEquals("North Field", notification.getResourceName());
        assertEquals("Farm 'North Field' was created.", notification.getMessage());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    void notifyFarmUpdatedShouldPublishToAdminAndOwner() {
        SimpMessagingTemplate messagingTemplate =
                mock(SimpMessagingTemplate.class);

        RealtimeNotificationService notificationService =
                new RealtimeNotificationService(messagingTemplate);

        Farm farm = new Farm(2L, "Updated Field", "Tilburg", 20L);

        notificationService.notifyFarmUpdated(farm);

        RealtimeNotificationResponse notification =
                captureNotification(messagingTemplate, 20L);

        assertEquals("FARM_UPDATED", notification.getType());
        assertEquals("FARM", notification.getResourceType());
        assertEquals(2L, notification.getResourceId());
        assertEquals("Updated Field", notification.getResourceName());
        assertEquals("Farm 'Updated Field' was updated.", notification.getMessage());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    void notifyFarmDeletedShouldPublishToAdminAndOwner() {
        SimpMessagingTemplate messagingTemplate =
                mock(SimpMessagingTemplate.class);

        RealtimeNotificationService notificationService =
                new RealtimeNotificationService(messagingTemplate);

        Farm farm = new Farm(3L, "Deleted Field", "Breda", 30L);

        notificationService.notifyFarmDeleted(farm);

        RealtimeNotificationResponse notification =
                captureNotification(messagingTemplate, 30L);

        assertEquals("FARM_DELETED", notification.getType());
        assertEquals("FARM", notification.getResourceType());
        assertEquals(3L, notification.getResourceId());
        assertEquals("Deleted Field", notification.getResourceName());
        assertEquals("Farm 'Deleted Field' was deleted.", notification.getMessage());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    void farmWithoutOwnerShouldNotifyOnlyAdmins() {
        SimpMessagingTemplate messagingTemplate =
                mock(SimpMessagingTemplate.class);

        RealtimeNotificationService notificationService =
                new RealtimeNotificationService(messagingTemplate);

        Farm farm = new Farm(4L, "Unassigned Field", "Eindhoven");

        notificationService.notifyFarmCreated(farm);

        ArgumentCaptor<RealtimeNotificationResponse> captor =
                ArgumentCaptor.forClass(RealtimeNotificationResponse.class);

        verify(messagingTemplate).convertAndSend(
                eq(ADMIN_TOPIC),
                captor.capture()
        );

        assertEquals("FARM_CREATED", captor.getValue().getType());
        assertEquals(4L, captor.getValue().getResourceId());

        verifyNoMoreInteractions(messagingTemplate);
    }

    private RealtimeNotificationResponse captureNotification(
            SimpMessagingTemplate messagingTemplate,
            Long ownerId
    ) {
        ArgumentCaptor<RealtimeNotificationResponse> captor =
                ArgumentCaptor.forClass(RealtimeNotificationResponse.class);

        verify(messagingTemplate).convertAndSend(
                eq(ADMIN_TOPIC),
                captor.capture()
        );

        RealtimeNotificationResponse notification = captor.getValue();

        verify(messagingTemplate).convertAndSend(
                USER_TOPIC_PREFIX + ownerId,
                notification
        );

        verifyNoMoreInteractions(messagingTemplate);

        return notification;
    }
}
