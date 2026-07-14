package com.bank.repository;

import com.bank.exception.CustomerNotFoundException;
import com.bank.exception.DuplicateCustomerException;
import com.bank.model.Customer;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for customers. ConcurrentHashMap gives us safe concurrent
 * reads/writes without any external synchronization in the service layer.
 */
public class CustomerRepository {

    private final Map<String, Customer> customers = new ConcurrentHashMap<>();

    public void save(Customer customer) throws DuplicateCustomerException {
        Customer existing = customers.putIfAbsent(customer.customerId(), customer);
        if (existing != null) {
            throw new DuplicateCustomerException(customer.customerId());
        }
    }

    public Customer findById(String customerId) throws CustomerNotFoundException {
        return Optional.ofNullable(customers.get(customerId))
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    public boolean exists(String customerId) {
        return customers.containsKey(customerId);
    }

    public Collection<Customer> findAll() {
        return customers.values();
    }
}
