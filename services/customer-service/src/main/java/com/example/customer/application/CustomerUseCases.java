package com.example.customer.application;

import com.example.customer.domain.Customer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CustomerUseCases {
    private CustomerUseCases() {}
    public interface Repository {
        Customer save(Customer customer);
        Optional<Customer> findById(UUID id);
        List<Customer> findAll();
    }
    public interface Service {
        Customer create(String email, String name, String country);
        Optional<Customer> get(UUID id);
        List<Customer> list();
    }
}
