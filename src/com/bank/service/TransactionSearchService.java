package com.bank.service;

import com.bank.model.Transaction;
import com.bank.model.TransactionStatus;
import com.bank.model.TransactionType;
import com.bank.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Read-only search, filter, sort, and aggregation operations over the transaction log.
 * Kept separate from TransactionService (which owns mutation) to keep each class
 * focused on a single responsibility.
 */
public class TransactionSearchService {

    private final TransactionRepository transactionRepository;

    public TransactionSearchService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> findAboveAmount(BigDecimal amount) {
        return transactionRepository.findAll().stream()
                .filter(t -> t.amount().compareTo(amount) > 0)
                .collect(Collectors.toList());
    }

    public List<Transaction> findByStatus(TransactionStatus status) {
        return transactionRepository.findAll().stream()
                .filter(t -> t.status() == status)
                .collect(Collectors.toList());
    }

    public List<Transaction> findByType(TransactionType type) {
        return transactionRepository.findAll().stream()
                .filter(t -> t.type() == type)
                .collect(Collectors.toList());
    }

    public List<Transaction> sortByAmount(boolean ascending) {
        Comparator<Transaction> byAmount = Comparator.comparing(Transaction::amount);
        return transactionRepository.findAll().stream()
                .sorted(ascending ? byAmount : byAmount.reversed())
                .collect(Collectors.toList());
    }

    public List<Transaction> sortByDate(boolean ascending) {
        Comparator<Transaction> byDate = Comparator.comparing(Transaction::dateTime);
        return transactionRepository.findAll().stream()
                .sorted(ascending ? byDate : byDate.reversed())
                .collect(Collectors.toList());
    }

    public Optional<Transaction> findLargestTransaction() {
        return transactionRepository.findAll().stream()
                .max(Comparator.comparing(Transaction::amount));
    }

    public BigDecimal totalTransferredAmount() {
        return transactionRepository.findAll().stream()
                .filter(t -> t.type() == TransactionType.TRANSFER && t.status() == TransactionStatus.SUCCESS)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<TransactionType, List<Transaction>> groupByType() {
        return transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(Transaction::type));
    }

    public Map<LocalDate, List<Transaction>> groupByDate() {
        return transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(t -> t.dateTime().toLocalDate()));
    }
}
