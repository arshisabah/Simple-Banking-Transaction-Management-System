package com.bank.exception;

public final class DuplicateCustomerException extends BankException {
    public DuplicateCustomerException(String customerId) {
        super("Customer already exists with ID: " + customerId);
    }
}
