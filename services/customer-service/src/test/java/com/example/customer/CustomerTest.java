package com.example.customer;
import com.example.customer.domain.Customer;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class CustomerTest {
    @Test void normalizesBusinessKeys() {
        var customer=Customer.create(" ADA@Example.COM "," Ada Lovelace ","gb");
        assertThat(customer.email()).isEqualTo("ada@example.com");
        assertThat(customer.fullName()).isEqualTo("Ada Lovelace");
        assertThat(customer.countryCode()).isEqualTo("GB");
    }
}
