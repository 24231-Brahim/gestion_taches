package com.gestiontaches.service.dto;

public final class EntityEventType {

    private EntityEventType() {}

    public static final String ENTITY_TASK = "TASK";
    public static final String ENTITY_SPRINT = "SPRINT";
    public static final String ENTITY_EPIC = "EPIC";
    public static final String ENTITY_PROJECT = "PROJECT";
    public static final String ENTITY_PROJECT_MEMBER = "PROJECT_MEMBER";

    public static final String CREATED = "CREATED";
    public static final String UPDATED = "UPDATED";
    public static final String DELETED = "DELETED";
}
