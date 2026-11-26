package com.example.agroflowbackend.service;

import com.example.agroflowbackend.domain.Farm;
import com.example.agroflowbackend.dto.RealtimeNotificationResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RealtimeNotificationService {

    private static final String ADMIN_TOPIC = "/topic/farms/admin";
    private static final String USER_TOPIC_PREFIX = "/topic/farms/user/";
    private static final String FARM_RESOURCE_TYPE = "FARM";

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeNotificationService(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyFarmCreated(Farm farm) {
        sendFarmNotification(
                "FARM_CREATED",
                farm,
                "Farm '%s' was created."
        );
    }

    public void notifyFarmUpdated(Farm farm) {
        sendFarmNotification(
                "FARM_UPDATED",
                farm,
                "Farm '%s' was updated."
        );
    }

    public void notifyFarmDeleted(Farm farm) {
        sendFarmNotification(
                "FARM_DELETED",
                farm,
                "Farm '%s' was deleted."
        );
    }

    private void sendFarmNotification(
            String type,
            Farm farm,
            String messageTemplate
    ) {
        String farmName = farm.getName();

        RealtimeNotificationResponse notification =
                new RealtimeNotificationResponse(
                        type,
                        FARM_RESOURCE_TYPE,
                        farm.getId(),
                        farmName,
                        String.format(messageTemplate, farmName),
                        Instant.now()
                );

        // Notify administrators.
        messagingTemplate.convertAndSend(
                ADMIN_TOPIC,
                notification
        );

        // Notify the farm owner.
        if (farm.getOwnerId() != null) {
            messagingTemplate.convertAndSend(
                    USER_TOPIC_PREFIX + farm.getOwnerId(),
                    notification
            );
        }
    }
}
