package com.bank.exception;

/**
 * Base type for all checked banking domain errors.
 * Sealed so that every specific failure mode in the system is known and
 * can be exhaustively handled (e.g. via pattern matching in the console layer).
 */
public sealed class BankException extends Exception permits
        CustomerNotFoundException,
        AccountNotFoundException,
        DuplicateCustomerException,
        DuplicateAccountException,
        InvalidAmountException,
        InsufficientBalanceException,
        AccountClosedException,
        InvalidTransferException,
        TransactionNotFoundException {

    public BankException(String message) {
        super(message);
    }
}
