package com.bank.service;

import com.bank.exception.CustomerNotFoundException;
import com.bank.exception.DuplicateCustomerException;
import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.util.IdGenerator;

import java.util.Collection;
import java.util.List;

public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public CustomerService(CustomerRepository customerRepository, AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    public Customer addCustomer(String name, String email, String phone) throws DuplicateCustomerException {
        Customer customer = new Customer(IdGenerator.nextCustomerId(), name, email, phone);
        customerRepository.save(customer);
        return customer;
    }

    public Collection<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer findCustomer(String customerId) throws CustomerNotFoundException {
        return customerRepository.findById(customerId);
    }

    public List<Account> getAccountsOf(String customerId) throws CustomerNotFoundException {
        // Confirms the customer actually exists before listing (fails fast, clear error).
        customerRepository.findById(customerId);
        return accountRepository.findByCustomerId(customerId);
    }
}
