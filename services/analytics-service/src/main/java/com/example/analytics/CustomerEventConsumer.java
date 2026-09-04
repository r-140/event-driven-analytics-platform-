package com.example.analytics;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Component
public class CustomerEventConsumer {
    private final JdbcClient jdbc; private final ObjectMapper json;
    public CustomerEventConsumer(JdbcClient jdbc, ObjectMapper json) { this.jdbc=jdbc; this.json=json; }
    @KafkaListener(topics="outbox.event.customer") @Transactional
    public void consume(String message) throws Exception {
        JsonNode payload=unwrap(json.readTree(message));
        UUID aggregateId=UUID.fromString(payload.path("id").asText()); UUID eventId=aggregateId;
        String type="CustomerCreated";
        int inserted=jdbc.sql("insert into bronze.customer_events(event_id,aggregate_id,event_type,payload,occurred_at) values (:e,:a,:t,cast(:p as jsonb),:o) on conflict do nothing")
            .param("e",eventId).param("a",aggregateId).param("t",type).param("p",payload.toString()).param("o",sqlTimestamp(payload.path("createdAt").asText())).update();
        if (inserted==0) return;
        jdbc.sql("insert into silver.customers(customer_id,email,full_name,country_code,created_at) values (:id,:email,:name,:country,:created) on conflict(customer_id) do update set email=excluded.email,full_name=excluded.full_name,country_code=excluded.country_code")
            .param("id",aggregateId).param("email",payload.path("email").asText()).param("name",payload.path("fullName").asText()).param("country",payload.path("countryCode").asText()).param("created",sqlTimestamp(payload.path("createdAt").asText())).update();
        jdbc.sql("insert into gold.customer_summary(country_code,customer_count,refreshed_at) select country_code,count(*),now() from silver.customers group by country_code on conflict(country_code) do update set customer_count=excluded.customer_count,refreshed_at=excluded.refreshed_at").update();
    }

    private JsonNode unwrap(JsonNode value) throws Exception {
        if (value.has("payload")) value=value.path("payload");
        return value.isTextual() ? json.readTree(value.asText()) : value;
    }
    private java.sql.Timestamp sqlTimestamp(String value) { return java.sql.Timestamp.from(Instant.parse(value)); }
}
