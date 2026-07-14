package com.bank.exception;

public final class CustomerNotFoundException extends BankException {
    public CustomerNotFoundException(String customerId) {
        super("Customer not found with ID: " + customerId);
    }
}
