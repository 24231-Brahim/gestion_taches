package com.gestiontaches.service;

import com.gestiontaches.service.dto.EntityChangeEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class EntityEventSseService {

    private static final Logger LOG = LoggerFactory.getLogger(EntityEventSseService.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> removeEmitter(emitter));
        emitter.onTimeout(() -> removeEmitter(emitter));
        emitter.onError(e -> removeEmitter(emitter));

        LOG.debug("SSE entity event subscription added (total: {})", emitters.size());
        return emitter;
    }

    public void sendEvent(EntityChangeEvent event) {
        if (emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("entity-change").data(event));
            } catch (IOException e) {
                LOG.debug("Failed to send SSE entity event: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    private void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
        LOG.debug("SSE entity event subscription removed (total: {})", emitters.size());
    }
}
