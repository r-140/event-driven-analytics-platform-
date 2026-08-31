package com.example.customer.adapter.in.web;

import com.example.customer.application.CustomerUseCases;
import com.example.customer.domain.Customer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerUseCases.Service customers;
    public CustomerController(CustomerUseCases.Service customers) { this.customers = customers; }
    public record CreateCustomer(@NotBlank @Email String email, @NotBlank @Size(max=200) String fullName, @Pattern(regexp="[A-Za-z]{2}") String countryCode) {}
    @PostMapping public ResponseEntity<Customer> create(@Valid @RequestBody CreateCustomer request) {
        var customer = customers.create(request.email(), request.fullName(), request.countryCode());
        return ResponseEntity.created(URI.create("/api/customers/" + customer.id())).body(customer);
    }
    @GetMapping("/{id}") public Customer get(@PathVariable UUID id) { return customers.get(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)); }
    @GetMapping public List<Customer> list() { return customers.list(); }
}
