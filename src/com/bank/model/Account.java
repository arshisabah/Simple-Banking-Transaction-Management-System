package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base type for all bank accounts.
 * Sealed to a fixed, known set of account types (SavingsAccount, CurrentAccount),
 * which lets callers exhaustively pattern-match on the concrete type when needed.
 *
 * Balance mutation is guarded by an internal lock so that concurrent deposits,
 * withdrawals, and transfers touching the same account never race each other.
 */
public sealed abstract class Account permits SavingsAccount, CurrentAccount {

    private final String accountNumber;
    private final String customerId;
    private final LocalDateTime createdDate;
    private final ReentrantLock lock = new ReentrantLock();

    private volatile BigDecimal balance;
    private volatile AccountStatus status;

    protected Account(String accountNumber, String customerId, BigDecimal openingBalance) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.balance = openingBalance;
        this.status = AccountStatus.ACTIVE;
        this.createdDate = LocalDateTime.now();
    }

    public abstract AccountType getAccountType();

    /** Locks the account for the duration of a compound balance operation. */
    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return """
                Account[%s] Type=%s Customer=%s Balance=%.2f Status=%s Created=%s"""
                .formatted(accountNumber, getAccountType(), customerId, balance, status, createdDate);
    }
}
