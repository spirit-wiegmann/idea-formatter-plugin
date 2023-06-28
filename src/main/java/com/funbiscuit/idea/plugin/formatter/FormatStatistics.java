package com.funbiscuit.idea.plugin.formatter;

import java.util.concurrent.atomic.AtomicInteger;

public class FormatStatistics {
    private final AtomicInteger processed = new AtomicInteger();
    private final AtomicInteger valid = new AtomicInteger();

    public void fileProcessed(boolean isValid) {
        processed.incrementAndGet();
        if (isValid) {
            valid.incrementAndGet();
        }
    }

    public int getProcessed() {
        return processed.get();
    }

    public boolean allValid() {
        return valid.get() == processed.get();
    }
}
