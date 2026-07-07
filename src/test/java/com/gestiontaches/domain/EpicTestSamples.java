package com.gestiontaches.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class EpicTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Epic getEpicSample1() {
        return new Epic().id(1L).title("title1").description("description1");
    }

    public static Epic getEpicSample2() {
        return new Epic().id(2L).title("title2").description("description2");
    }

    public static Epic getEpicRandomSampleGenerator() {
        return new Epic().id(longCount.incrementAndGet()).title(UUID.randomUUID().toString()).description(UUID.randomUUID().toString());
    }
}
