package com.bank.exception;

public final class AccountNotFoundException extends BankException {
    public AccountNotFoundException(String accountNumber) {
        super("Account not found with number: " + accountNumber);
    }
}
