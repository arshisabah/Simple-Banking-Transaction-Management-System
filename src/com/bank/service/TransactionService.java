package com.bank.service;

import com.bank.exception.AccountClosedException;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.exception.InvalidAmountException;
import com.bank.exception.InvalidTransferException;
import com.bank.exception.TransactionNotFoundException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.TransactionStatus;
import com.bank.model.TransactionType;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import com.bank.util.IdGenerator;
import com.bank.util.Validator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles money movement. Every account mutation happens while holding that
 * account's own lock, and a transfer always locks the two accounts in a fixed
 * order (by account number) so that two threads transferring in opposite
 * directions between the same pair of accounts can never deadlock.
 */
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public Transaction deposit(String accountNumber, BigDecimal amount)
            throws AccountNotFoundException, InvalidAmountException, AccountClosedException {

        Account account = accountRepository.findByNumber(accountNumber);

        try {
            Validator.requirePositiveAmount(amount);
            if (!account.isActive()) {
                throw new AccountClosedException(accountNumber);
            }
            account.lock();
            try {
                account.credit(amount);
            } finally {
                account.unlock();
            }
        } catch (InvalidAmountException | AccountClosedException ex) {
            record(TransactionType.DEPOSIT, Transaction.NOT_APPLICABLE, accountNumber, amount, TransactionStatus.FAILED);
            throw ex;
        }

        return record(TransactionType.DEPOSIT, Transaction.NOT_APPLICABLE, accountNumber, amount, TransactionStatus.SUCCESS);
    }

    public Transaction withdraw(String accountNumber, BigDecimal amount)
            throws AccountNotFoundException, InvalidAmountException, AccountClosedException, InsufficientBalanceException {

        Account account = accountRepository.findByNumber(accountNumber);

        try {
            Validator.requirePositiveAmount(amount);
            if (!account.isActive()) {
                throw new AccountClosedException(accountNumber);
            }
            account.lock();
            try {
                if (account.getBalance().compareTo(amount) < 0) {
                    throw new InsufficientBalanceException(accountNumber);
                }
                account.debit(amount);
            } finally {
                account.unlock();
            }
        } catch (InvalidAmountException | AccountClosedException | InsufficientBalanceException ex) {
            record(TransactionType.WITHDRAWAL, accountNumber, Transaction.NOT_APPLICABLE, amount, TransactionStatus.FAILED);
            throw ex;
        }

        return record(TransactionType.WITHDRAWAL, accountNumber, Transaction.NOT_APPLICABLE, amount, TransactionStatus.SUCCESS);
    }

    public Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount)
            throws AccountNotFoundException, InvalidAmountException, AccountClosedException,
            InsufficientBalanceException, InvalidTransferException {

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidTransferException("Source and destination accounts must be different");
        }

        Account from = accountRepository.findByNumber(fromAccountNumber);
        Account to = accountRepository.findByNumber(toAccountNumber);

        // Always lock in a consistent, account-number order to avoid deadlock
        // when two transfers run concurrently in opposite directions.
        Account first = fromAccountNumber.compareTo(toAccountNumber) < 0 ? from : to;
        Account second = fromAccountNumber.compareTo(toAccountNumber) < 0 ? to : from;

        try {
            Validator.requirePositiveAmount(amount);
            if (!from.isActive() || !to.isActive()) {
                throw new AccountClosedException(!from.isActive() ? fromAccountNumber : toAccountNumber);
            }

            first.lock();
            try {
                second.lock();
                try {
                    if (from.getBalance().compareTo(amount) < 0) {
                        throw new InsufficientBalanceException(fromAccountNumber);
                    }
                    from.debit(amount);
                    to.credit(amount);
                } finally {
                    second.unlock();
                }
            } finally {
                first.unlock();
            }
        } catch (InvalidAmountException | AccountClosedException | InsufficientBalanceException ex) {
            record(TransactionType.TRANSFER, fromAccountNumber, toAccountNumber, amount, TransactionStatus.FAILED);
            throw ex;
        }

        return record(TransactionType.TRANSFER, fromAccountNumber, toAccountNumber, amount, TransactionStatus.SUCCESS);
    }

    private Transaction record(TransactionType type, String source, String destination,
                                BigDecimal amount, TransactionStatus status) {
        Transaction transaction = new Transaction(
                IdGenerator.nextTransactionId(), type, source, destination, amount, LocalDateTime.now(), status);
        transactionRepository.save(transaction);
        return transaction;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsForAccount(String accountNumber) {
        return transactionRepository.findByAccount(accountNumber);
    }

    public Transaction findTransaction(String transactionId) throws TransactionNotFoundException {
        return transactionRepository.findById(transactionId);
    }
}
