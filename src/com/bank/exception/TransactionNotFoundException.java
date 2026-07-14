package com.bank.exception;

public final class TransactionNotFoundException extends BankException {
    public TransactionNotFoundException(String transactionId) {
        super("Transaction not found with ID: " + transactionId);
    }
}
