package com.gestiontaches.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class IssueTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static Issue getIssueSample1() {
        return new Issue().id(1L).title("title1").description("description1");
    }

    public static Issue getIssueSample2() {
        return new Issue().id(2L).title("title2").description("description2");
    }

    public static Issue getIssueRandomSampleGenerator() {
        return new Issue().id(longCount.incrementAndGet()).title(UUID.randomUUID().toString()).description(UUID.randomUUID().toString());
    }
}
