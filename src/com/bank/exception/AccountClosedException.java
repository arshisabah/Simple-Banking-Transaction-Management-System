package com.bank.exception;

public final class AccountClosedException extends BankException {
    public AccountClosedException(String accountNumber) {
        super("Account is closed: " + accountNumber);
    }
}
