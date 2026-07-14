package com.bank.model;

import java.math.BigDecimal;

public final class SavingsAccount extends Account {

    public static final BigDecimal MINIMUM_BALANCE = BigDecimal.valueOf(500);

    public SavingsAccount(String accountNumber, String customerId, BigDecimal openingBalance) {
        super(accountNumber, customerId, openingBalance);
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.SAVINGS;
    }
}
