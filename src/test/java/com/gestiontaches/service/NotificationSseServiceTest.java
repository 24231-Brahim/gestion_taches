package com.gestiontaches.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gestiontaches.service.dto.NotificationDTO;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class NotificationSseServiceTest {

    private NotificationSseService notificationSseService;

    @BeforeEach
    void setUp() {
        notificationSseService = new NotificationSseService();
    }

    @Test
    void subscribe_returnsEmitter() {
        SseEmitter emitter = notificationSseService.subscribe(1L);
        assertThat(emitter).isNotNull();
    }

    @Test
    void subscribe_multipleSubscriptionsForSameUser() {
        SseEmitter emitter1 = notificationSseService.subscribe(1L);
        SseEmitter emitter2 = notificationSseService.subscribe(1L);
        assertThat(emitter1).isNotSameAs(emitter2);
    }

    @Test
    void sendNotification_sendsToCorrectUser() throws Exception {
        SseEmitter emitter = notificationSseService.subscribe(1L);

        NotificationDTO notification = new NotificationDTO();
        notification.setId(1L);
        notification.setMessage("Test notification");
        notification.setIsRead(false);

        AtomicReference<SseEmitter.SseEventBuilder> sentEvent = new AtomicReference<>();
        SseEmitter spyEmitter = new SseEmitter() {
            @Override
            public void send(SseEmitter.SseEventBuilder builder) throws IOException {
                sentEvent.set(builder);
            }
        };

        // Replace the emitter in the internal map
        try {
            Field field = NotificationSseService.class.getDeclaredField("emittersByUser");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Long, List<SseEmitter>> map = (Map<Long, List<SseEmitter>>) field.get(notificationSseService);
            map.get(1L).clear();
            map.get(1L).add(spyEmitter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        notificationSseService.sendNotification(1L, notification);
        assertThat(sentEvent.get()).isNotNull();
    }

    @Test
    void sendNotification_doesNotSendToOtherUsers() throws Exception {
        SseEmitter emitter2 = notificationSseService.subscribe(2L);

        AtomicReference<SseEmitter.SseEventBuilder> sentEvent = new AtomicReference<>();
        SseEmitter spyEmitter = new SseEmitter() {
            @Override
            public void send(SseEmitter.SseEventBuilder builder) throws IOException {
                sentEvent.set(builder);
            }
        };

        try {
            Field field = NotificationSseService.class.getDeclaredField("emittersByUser");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Long, List<SseEmitter>> map = (Map<Long, List<SseEmitter>>) field.get(notificationSseService);
            map.get(2L).clear();
            map.get(2L).add(spyEmitter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        NotificationDTO notification = new NotificationDTO();
        notification.setId(1L);
        notification.setMessage("Test notification");

        notificationSseService.sendNotification(1L, notification);
        assertThat(sentEvent.get()).isNull();
    }

    @Test
    void sendNotification_noSubscribers_doesNotThrow() {
        NotificationDTO notification = new NotificationDTO();
        notification.setId(1L);
        notification.setMessage("Test notification");

        notificationSseService.sendNotification(999L, notification);
    }
}
