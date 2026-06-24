package com.gestiontaches.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ActionHistoryTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2L * Integer.MAX_VALUE));

    public static ActionHistory getActionHistorySample1() {
        return new ActionHistory().id(1L).action("action1").fieldChanged("fieldChanged1").oldValue("oldValue1").newValue("newValue1");
    }

    public static ActionHistory getActionHistorySample2() {
        return new ActionHistory().id(2L).action("action2").fieldChanged("fieldChanged2").oldValue("oldValue2").newValue("newValue2");
    }

    public static ActionHistory getActionHistoryRandomSampleGenerator() {
        return new ActionHistory()
            .id(longCount.incrementAndGet())
            .action(UUID.randomUUID().toString())
            .fieldChanged(UUID.randomUUID().toString())
            .oldValue(UUID.randomUUID().toString())
            .newValue(UUID.randomUUID().toString());
    }
}
