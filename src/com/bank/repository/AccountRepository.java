package com.bank.repository;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.DuplicateAccountException;
import com.bank.model.Account;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AccountRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public void save(Account account) throws DuplicateAccountException {
        Account existing = accounts.putIfAbsent(account.getAccountNumber(), account);
        if (existing != null) {
            throw new DuplicateAccountException(account.getAccountNumber());
        }
    }

    public Account findByNumber(String accountNumber) throws AccountNotFoundException {
        return Optional.ofNullable(accounts.get(accountNumber))
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    public Collection<Account> findAll() {
        return accounts.values();
    }

    public List<Account> findByCustomerId(String customerId) {
        return accounts.values().stream()
                .filter(a -> a.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }
}
