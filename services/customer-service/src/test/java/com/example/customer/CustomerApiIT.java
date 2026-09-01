package com.example.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @Testcontainers(disabledWithoutDocker=true)
class CustomerApiIT {
    @Container static final PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:17-alpine");
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) { r.add("spring.datasource.url", DB::getJdbcUrl); r.add("spring.datasource.username", DB::getUsername); r.add("spring.datasource.password", DB::getPassword); }
    @Autowired MockMvc mvc;
    @Test void createsCustomerAndOutboxEvent() throws Exception {
        mvc.perform(post("/api/customers").contentType("application/json").content("{\"email\":\"Ada@Example.com\",\"fullName\":\"Ada Lovelace\",\"countryCode\":\"gb\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.email").value("ada@example.com")).andExpect(jsonPath("$.countryCode").value("GB"));
    }
    @Test void validatesInput() throws Exception { mvc.perform(post("/api/customers").contentType("application/json").content("{}" )).andExpect(status().isBadRequest()); }
}
