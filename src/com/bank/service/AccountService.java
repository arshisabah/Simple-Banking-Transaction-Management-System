package com.bank.service;

import com.bank.exception.AccountNotFoundException;
import com.bank.exception.CustomerNotFoundException;
import com.bank.exception.DuplicateAccountException;
import com.bank.exception.InvalidTransferException;
import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.AccountType;
import com.bank.model.CurrentAccount;
import com.bank.model.SavingsAccount;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.util.IdGenerator;

import java.math.BigDecimal;
import java.util.Collection;

public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Account openAccount(String customerId, AccountType type, BigDecimal openingBalance)
            throws CustomerNotFoundException, DuplicateAccountException {

        // Confirms customer exists before an account can ever be linked to them.
        customerRepository.findById(customerId);

        String accountNumber = IdGenerator.nextAccountNumber();

        // Pattern matching on a switch expression selects the concrete account subtype,
        // demonstrating exhaustive handling of the sealed Account hierarchy.
        Account account = switch (type) {
            case SAVINGS -> new SavingsAccount(accountNumber, customerId, openingBalance);
            case CURRENT -> new CurrentAccount(accountNumber, customerId, openingBalance);
        };

        accountRepository.save(account);
        return account;
    }

    public Account findAccount(String accountNumber) throws AccountNotFoundException {
        return accountRepository.findByNumber(accountNumber);
    }

    public Collection<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public BigDecimal getBalance(String accountNumber) throws AccountNotFoundException {
        return accountRepository.findByNumber(accountNumber).getBalance();
    }

    public void closeAccount(String accountNumber) throws AccountNotFoundException, InvalidTransferException {
        Account account = accountRepository.findByNumber(accountNumber);
        account.lock();
        try {
            if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                throw new InvalidTransferException(
                        "Cannot close account " + accountNumber + " - balance must be zero (current: "
                                + account.getBalance() + ")");
            }
            account.setStatus(AccountStatus.CLOSED);
        } finally {
            account.unlock();
        }
    }
}
