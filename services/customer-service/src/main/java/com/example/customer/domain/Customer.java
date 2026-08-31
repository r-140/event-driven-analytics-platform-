package com.example.customer.domain;

import java.time.Instant;
import java.util.UUID;

public record Customer(UUID id, String email, String fullName, String countryCode, Instant createdAt, Instant updatedAt) {
    public Customer {
        email = email.trim().toLowerCase();
        fullName = fullName.trim();
        countryCode = countryCode.trim().toUpperCase();
    }
    public static Customer create(String email, String name, String country) {
        var now = Instant.now();
        return new Customer(UUID.randomUUID(), email, name, country, now, now);
    }
}
