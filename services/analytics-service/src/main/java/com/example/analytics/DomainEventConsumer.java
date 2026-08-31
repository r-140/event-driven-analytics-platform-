package com.example.analytics;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class DomainEventConsumer {
    private final JdbcClient db;
    private final ObjectMapper json;
    public DomainEventConsumer(JdbcClient db, ObjectMapper json) { this.db = db; this.json = json; }

    @KafkaListener(topics={"outbox.event.invoice","outbox.event.payment","outbox.event.identity"})
    @Transactional
    public void consume(ConsumerRecord<String,String> record) throws Exception {
        String domain = record.topic().substring(record.topic().lastIndexOf('.') + 1);
        JsonNode payload = json.readTree(record.value());
        UUID id = UUID.fromString(payload.path("id").asText());
        String eventType = eventType(record, domain, payload);
        Instant occurredAt = eventTime(eventType, payload);
        int inserted = db.sql("insert into bronze.domain_events(event_id,domain,event_type,payload,occurred_at) values(:id,:domain,:type,cast(:payload as jsonb),:at) on conflict do nothing")
            .param("id", id).param("domain", domain).param("type", eventType).param("payload", payload.toString()).param("at", occurredAt).update();
        if (inserted == 0) return;
        switch (eventType) {
            case "InvoiceIssued" -> invoice(payload, occurredAt);
            case "InvoiceAdjusted" -> adjustment(payload, occurredAt);
            case "PaymentReceived" -> payment(payload, occurredAt);
            case "IdentityRegistered" -> identity(payload, occurredAt);
            default -> throw new IllegalArgumentException("Unsupported event type " + eventType);
        }
    }

    private String eventType(ConsumerRecord<String,String> record, String domain, JsonNode payload) {
        var header = record.headers().lastHeader("eventType");
        if (header == null) header = record.headers().lastHeader("type");
        if (header != null) return new String(header.value(), StandardCharsets.UTF_8);
        return switch (domain) {
            case "invoice" -> payload.has("adjustedAt") ? "InvoiceAdjusted" : "InvoiceIssued";
            case "payment" -> "PaymentReceived";
            case "identity" -> "IdentityRegistered";
            default -> throw new IllegalArgumentException("Unsupported domain " + domain);
        };
    }

    private Instant eventTime(String eventType, JsonNode payload) {
        String field = switch (eventType) { case "InvoiceIssued" -> "issuedAt"; case "InvoiceAdjusted" -> "adjustedAt"; case "PaymentReceived" -> "receivedAt"; case "IdentityRegistered" -> "registeredAt"; default -> throw new IllegalArgumentException(eventType); };
        return Instant.parse(payload.path(field).asText());
    }

    private void invoice(JsonNode p, Instant at) {
        db.sql("insert into silver.invoices values(:id,:customer,:amount,:currency,:status,:at) on conflict do nothing").param("id",uuid(p,"id")).param("customer",uuid(p,"customerId")).param("amount",decimal(p,"amount")).param("currency",text(p,"currency")).param("status",text(p,"status")).param("at",at).update();
        refreshInvoiceNetAmount();
    }
    private void adjustment(JsonNode p, Instant at) {
        db.sql("insert into silver.invoice_adjustments values(:id,:invoice,:amount,:currency,:reason,:at) on conflict do nothing").param("id",uuid(p,"id")).param("invoice",uuid(p,"invoiceId")).param("amount",decimal(p,"amount")).param("currency",text(p,"currency")).param("reason",text(p,"reason")).param("at",at).update();
        refreshInvoiceNetAmount();
    }
    private void refreshInvoiceNetAmount() {
        db.sql("insert into gold.invoice_net_amount select i.invoice_id,i.amount,coalesce(sum(a.amount),0),i.amount-coalesce(sum(a.amount),0),now() from silver.invoices i left join silver.invoice_adjustments a using(invoice_id) group by i.invoice_id,i.amount on conflict(invoice_id) do update set issued_amount=excluded.issued_amount,adjusted_amount=excluded.adjusted_amount,net_amount=excluded.net_amount,refreshed_at=excluded.refreshed_at").update();
    }
    private void payment(JsonNode p, Instant at) {
        db.sql("insert into silver.payments values(:id,:invoice,:amount,:currency,:status,:at) on conflict do nothing").param("id",uuid(p,"id")).param("invoice",uuid(p,"invoiceId")).param("amount",decimal(p,"amount")).param("currency",text(p,"currency")).param("status",text(p,"status")).param("at",at).update();
        db.sql("insert into gold.payment_summary select currency,count(*),sum(amount),now() from silver.payments group by currency on conflict(currency) do update set payment_count=excluded.payment_count,total_amount=excluded.total_amount,refreshed_at=excluded.refreshed_at").update();
    }
    private void identity(JsonNode p, Instant at) {
        db.sql("insert into silver.identities values(:id,:customer,:login,:status,:at) on conflict do nothing").param("id",uuid(p,"id")).param("customer",uuid(p,"customerId")).param("login",text(p,"login")).param("status",text(p,"status")).param("at",at).update();
    }
    private UUID uuid(JsonNode p,String field){return UUID.fromString(text(p,field));}
    private BigDecimal decimal(JsonNode p,String field){return new BigDecimal(text(p,field));}
    private String text(JsonNode p,String field){return p.path(field).asText();}
}
