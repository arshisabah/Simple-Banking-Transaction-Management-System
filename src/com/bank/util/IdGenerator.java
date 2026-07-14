package com.bank.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique, sequential, human-readable IDs.
 * Backed by AtomicLong so IDs remain unique even when requested
 * concurrently from multiple transaction-processing threads.
 */
public final class IdGenerator {

    private static final AtomicLong CUSTOMER_SEQ = new AtomicLong(1000);
    private static final AtomicLong ACCOUNT_SEQ = new AtomicLong(100000);
    private static final AtomicLong TRANSACTION_SEQ = new AtomicLong(1);

    private IdGenerator() {
    }

    public static String nextCustomerId() {
        return "CUST" + CUSTOMER_SEQ.incrementAndGet();
    }

    public static String nextAccountNumber() {
        return "ACC" + ACCOUNT_SEQ.incrementAndGet();
    }

    public static String nextTransactionId() {
        return "TXN" + TRANSACTION_SEQ.incrementAndGet();
    }
}
