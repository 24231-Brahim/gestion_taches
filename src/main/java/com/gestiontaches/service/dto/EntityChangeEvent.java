package com.gestiontaches.service.dto;

public class EntityChangeEvent {

    private String entityType;
    private String eventType;
    private Long entityId;
    private Long projectId;

    public EntityChangeEvent() {}

    public EntityChangeEvent(String entityType, String eventType, Long entityId, Long projectId) {
        this.entityType = entityType;
        this.eventType = eventType;
        this.entityId = entityId;
        this.projectId = projectId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
