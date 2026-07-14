package com.bank.repository;

import com.bank.exception.TransactionNotFoundException;
import com.bank.model.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * In-memory transaction log. CopyOnWriteArrayList is used because transactions
 * are appended far more often than the full list is iterated, and it gives
 * lock-free, safe concurrent reads while multiple worker threads append records.
 */
public class TransactionRepository {

    private final List<Transaction> transactions = new CopyOnWriteArrayList<>();

    public void save(Transaction transaction) {
        transactions.add(transaction);
    }

    public List<Transaction> findAll() {
        return List.copyOf(transactions);
    }

    public List<Transaction> findByAccount(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.sourceAccount().equals(accountNumber)
                        || t.destinationAccount().equals(accountNumber))
                .collect(Collectors.toList());
    }

    public Transaction findById(String transactionId) throws TransactionNotFoundException {
        Optional<Transaction> found = transactions.stream()
                .filter(t -> t.transactionId().equals(transactionId))
                .findFirst();
        return found.orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }
}
