package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable record of a single banking transaction.
 * For a DEPOSIT there is no source account; for a WITHDRAWAL there is no destination account.
 * "N/A" is used as the sentinel for "not applicable" instead of null to keep the record
 * safe to print and compare without null-checks everywhere.
 */
public record Transaction(
        String transactionId,
        TransactionType type,
        String sourceAccount,
        String destinationAccount,
        BigDecimal amount,
        LocalDateTime dateTime,
        TransactionStatus status
) implements Comparable<Transaction> {

    public static final String NOT_APPLICABLE = "N/A";

    // Natural ordering: most recent first.
    @Override
    public int compareTo(Transaction other) {
        return other.dateTime.compareTo(this.dateTime);
    }

    @Override
    public String toString() {
        return """
                [%s] %-10s %-20s From=%-8s To=%-8s Amount=%10.2f Status=%s"""
                .formatted(dateTime, transactionId, type, sourceAccount, destinationAccount, amount, status);
    }
}
