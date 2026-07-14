package com.bank.util;

import com.bank.exception.InvalidAmountException;

import java.math.BigDecimal;

/**
 * Reusable validation helpers shared across services.
 */
public final class Validator {

    private Validator() {
    }

    public static void requirePositiveAmount(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transaction amount must be greater than zero");
        }
    }
}
