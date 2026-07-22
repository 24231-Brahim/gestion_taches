package com.gestiontaches.domain;

import com.gestiontaches.domain.enumeration.Priority;
import com.gestiontaches.domain.enumeration.TaskStatus;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class TaskTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Task getTaskSample1() {
        return new Task()
            .id(1L)
            .title("title1")
            .description("description1")
            .status(TaskStatus.NEW)
            .priority(Priority.LOW)
            .createdAt(Instant.ofEpochSecond(1L));
    }

    public static Task getTaskSample2() {
        return new Task()
            .id(2L)
            .title("title2")
            .description("description2")
            .status(TaskStatus.IN_PROGRESS)
            .priority(Priority.HIGH)
            .createdAt(Instant.ofEpochSecond(2L));
    }

    public static Task getTaskRandomSampleGenerator() {
        return new Task()
            .id(longCount.incrementAndGet())
            .title(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .status(getTaskStatusRandomSampleGenerator())
            .priority(getPriorityRandomSampleGenerator())
            .createdAt(Instant.now());
    }

    public static Task getTaskCreateSampleGenerator() {
        return new Task()
            .id(longCount.incrementAndGet())
            .title(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .status(getTaskStatusRandomSampleGenerator())
            .priority(getPriorityRandomSampleGenerator())
            .createdAt(Instant.now());
    }

    public static Task getTaskUpdateSampleGenerator() {
        return new Task()
            .id(longCount.incrementAndGet())
            .title(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .status(getTaskStatusRandomSampleGenerator())
            .priority(getPriorityRandomSampleGenerator())
            .createdAt(Instant.now());
    }

    public static TaskStatus getTaskStatusRandomSampleGenerator() {
        return TaskStatus.values()[random.nextInt(TaskStatus.values().length)];
    }

    public static Priority getPriorityRandomSampleGenerator() {
        return Priority.values()[random.nextInt(Priority.values().length)];
    }
}
