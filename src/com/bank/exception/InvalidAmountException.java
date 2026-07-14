package com.bank.exception;

public final class InvalidAmountException extends BankException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
