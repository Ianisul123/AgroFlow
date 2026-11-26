package com.example.agroflowbackend.service;

import com.example.agroflowbackend.domain.Farm;
import com.example.agroflowbackend.dto.RealtimeNotificationResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RealtimeNotificationServiceTest {

    @Test
    void notifyFarmCreatedShouldPublishFarmCreatedEvent() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RealtimeNotificationService notificationService = new RealtimeNotificationService(messagingTemplate);
        Farm farm = new Farm(1L, "North Field", "Eindhoven");

        notificationService.notifyFarmCreated(farm);

        RealtimeNotificationResponse notification = captureNotification(messagingTemplate);
        assertEquals("FARM_CREATED", notification.getType());
        assertEquals("FARM", notification.getResourceType());
        assertEquals(1L, notification.getResourceId());
        assertEquals("North Field", notification.getResourceName());
        assertEquals("Farm 'North Field' was created.", notification.getMessage());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    void notifyFarmUpdatedShouldPublishFarmUpdatedEvent() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RealtimeNotificationService notificationService = new RealtimeNotificationService(messagingTemplate);
        Farm farm = new Farm(2L, "Updated Field", "Tilburg");

        notificationService.notifyFarmUpdated(farm);

        RealtimeNotificationResponse notification = captureNotification(messagingTemplate);
        assertEquals("FARM_UPDATED", notification.getType());
        assertEquals("FARM", notification.getResourceType());
        assertEquals(2L, notification.getResourceId());
        assertEquals("Updated Field", notification.getResourceName());
        assertEquals("Farm 'Updated Field' was updated.", notification.getMessage());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    void notifyFarmDeletedShouldPublishFarmDeletedEvent() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RealtimeNotificationService notificationService = new RealtimeNotificationService(messagingTemplate);
        Farm farm = new Farm(3L, "Deleted Field", "Breda");

        notificationService.notifyFarmDeleted(farm);

        RealtimeNotificationResponse notification = captureNotification(messagingTemplate);
        assertEquals("FARM_DELETED", notification.getType());
        assertEquals("FARM", notification.getResourceType());
        assertEquals(3L, notification.getResourceId());
        assertEquals("Deleted Field", notification.getResourceName());
        assertEquals("Farm 'Deleted Field' was deleted.", notification.getMessage());
        assertNotNull(notification.getCreatedAt());
    }

    private RealtimeNotificationResponse captureNotification(SimpMessagingTemplate messagingTemplate) {
        ArgumentCaptor<RealtimeNotificationResponse> notificationCaptor =
                ArgumentCaptor.forClass(RealtimeNotificationResponse.class);
        verify(messagingTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("/topic/farms"),
                notificationCaptor.capture()
        );

        return notificationCaptor.getValue();
    }
}
