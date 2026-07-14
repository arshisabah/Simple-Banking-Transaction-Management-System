package com.bank.exception;

public final class DuplicateAccountException extends BankException {
    public DuplicateAccountException(String accountNumber) {
        super("Account already exists with number: " + accountNumber);
    }
}
