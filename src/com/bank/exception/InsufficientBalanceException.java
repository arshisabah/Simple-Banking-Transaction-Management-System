package com.bank.exception;

public final class InsufficientBalanceException extends BankException {
    public InsufficientBalanceException(String accountNumber) {
        super("Insufficient balance in account: " + accountNumber);
    }
}
