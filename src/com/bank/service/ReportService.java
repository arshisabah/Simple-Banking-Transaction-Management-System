package com.bank.service;

import com.bank.model.Account;
import com.bank.model.AccountType;
import com.bank.model.Transaction;
import com.bank.model.TransactionStatus;
import com.bank.model.TransactionType;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Aggregated, bank-wide statistics assembled entirely with the Streams API.
 */
public class ReportService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public ReportService(CustomerRepository customerRepository,
                          AccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public int totalCustomers() {
        return customerRepository.findAll().size();
    }

    public int totalAccounts() {
        return accountRepository.findAll().size();
    }

    public long countByType(AccountType type) {
        return accountRepository.findAll().stream()
                .filter(a -> a.getAccountType() == type)
                .count();
    }

    public BigDecimal totalMoneyInBank() {
        return accountRepository.findAll().stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int totalTransactions() {
        return transactionRepository.findAll().size();
    }

    public BigDecimal totalByType(TransactionType type) {
        return transactionRepository.findAll().stream()
                .filter(t -> t.type() == type && t.status() == TransactionStatus.SUCCESS)
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Optional<Account> highestBalanceAccount() {
        return accountRepository.findAll().stream()
                .max(Comparator.comparing(Account::getBalance));
    }

    public List<Transaction> topFiveLargestTransactions() {
        return transactionRepository.findAll().stream()
                .sorted(Comparator.comparing(Transaction::amount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    public Map<LocalDate, Long> transactionsPerDay() {
        return transactionRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        t -> t.dateTime().toLocalDate(),
                        Collectors.counting()));
    }

    /** Prints a full, formatted banking report to standard output. */
    public String generateFullReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                ==================== BANKING REPORT ====================
                Total Customers            : %d
                Total Accounts              : %d
                  - Savings Accounts        : %d
                  - Current Accounts        : %d
                Total Money in Bank         : %.2f
                Total Transactions          : %d
                  - Total Deposits          : %.2f
                  - Total Withdrawals       : %.2f
                  - Total Transfers         : %.2f
                """.formatted(
                totalCustomers(),
                totalAccounts(),
                countByType(AccountType.SAVINGS),
                countByType(AccountType.CURRENT),
                totalMoneyInBank(),
                totalTransactions(),
                totalByType(TransactionType.DEPOSIT),
                totalByType(TransactionType.WITHDRAWAL),
                totalByType(TransactionType.TRANSFER)
        ));

        highestBalanceAccount().ifPresentOrElse(
                acc -> sb.append("Highest Balance Account     : %s (%.2f)%n"
                        .formatted(acc.getAccountNumber(), acc.getBalance())),
                () -> sb.append("Highest Balance Account     : N/A%n"));

        sb.append("\nTop 5 Largest Transactions:\n");
        List<Transaction> top5 = topFiveLargestTransactions();
        if (top5.isEmpty()) {
            sb.append("  (none)\n");
        } else {
            top5.forEach(t -> sb.append("  ").append(t).append("\n"));
        }

        sb.append("\nTransactions Per Day:\n");
        Map<LocalDate, Long> perDay = transactionsPerDay();
        if (perDay.isEmpty()) {
            sb.append("  (none)\n");
        } else {
            perDay.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> sb.append("  %s : %d transaction(s)%n".formatted(e.getKey(), e.getValue())));
        }

        sb.append("==========================================================\n");
        return sb.toString();
    }
}
