package com.example.customer.application;

import com.example.customer.domain.Customer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerApplicationService implements CustomerUseCases.Service {
    private final CustomerUseCases.Repository repository;
    private final JdbcClient jdbc;
    private final ObjectMapper json;
    public CustomerApplicationService(CustomerUseCases.Repository repository, JdbcClient jdbc, ObjectMapper json) {
        this.repository = repository; this.jdbc = jdbc; this.json = json;
    }
    @Override @Transactional
    public Customer create(String email, String name, String country) {
        var customer = repository.save(Customer.create(email, name, country));
        jdbc.sql("insert into outbox_events(id,aggregate_type,aggregate_id,event_type,payload,occurred_at) values (:id,'customer',:aggregateId,'CustomerCreated',cast(:payload as jsonb),:at)")
            .param("id", UUID.randomUUID()).param("aggregateId", customer.id()).param("payload", payload(customer)).param("at", Instant.now()).update();
        return customer;
    }
    @Override public Optional<Customer> get(UUID id) { return repository.findById(id); }
    @Override public List<Customer> list() { return repository.findAll(); }
    private String payload(Customer customer) {
        try { return json.writeValueAsString(customer); }
        catch (JacksonException e) { throw new IllegalStateException("Cannot serialize customer event", e); }
    }
}
