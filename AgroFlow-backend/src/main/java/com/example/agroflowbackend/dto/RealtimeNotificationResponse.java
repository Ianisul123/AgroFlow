package com.example.agroflowbackend.dto;

import java.time.Instant;

public class RealtimeNotificationResponse {

    private final String type;
    private final String resourceType;
    private final Long resourceId;
    private final String resourceName;
    private final String message;
    private final Instant createdAt;

    public RealtimeNotificationResponse(
            String type,
            String resourceType,
            Long resourceId,
            String resourceName,
            String message,
            Instant createdAt
    ) {
        this.type = type;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
