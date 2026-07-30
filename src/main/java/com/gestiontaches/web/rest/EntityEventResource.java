package com.gestiontaches.web.rest;

import com.gestiontaches.service.EntityEventSseService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/events")
public class EntityEventResource {

    private final EntityEventSseService entityEventSseService;

    public EntityEventResource(EntityEventSseService entityEventSseService) {
        this.entityEventSseService = entityEventSseService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter stream() {
        return entityEventSseService.subscribe();
    }
}
