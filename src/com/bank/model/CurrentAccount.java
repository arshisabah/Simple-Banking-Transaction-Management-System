package com.bank.model;

import java.math.BigDecimal;

public final class CurrentAccount extends Account {

    public CurrentAccount(String accountNumber, String customerId, BigDecimal openingBalance) {
        super(accountNumber, customerId, openingBalance);
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.CURRENT;
    }
}
