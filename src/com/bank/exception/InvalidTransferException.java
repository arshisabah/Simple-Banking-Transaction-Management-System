package com.bank.exception;

public final class InvalidTransferException extends BankException {
    public InvalidTransferException(String message) {
        super(message);
    }
}
