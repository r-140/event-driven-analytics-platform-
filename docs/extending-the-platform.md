# Extension exercises

The repository now includes customer, invoice, payment, and identity bounded contexts. Further domains should reuse the boundary pattern, not shared persistence entities: define domain-specific APIs, migrations, outbox event names, connector slots, and topics. Correlate facts analytically through stable public identifiers.

Suggested progression:

1. Add `InvoiceCancelled`; evolve the current-state invoice projection to handle cancellation ordering.
2. Add refunds and partial payments; build allocation and outstanding-balance models.
3. Slowly changing customer attributes using a dbt snapshot or a valid-from/valid-to Silver model.
4. Avro or Protobuf plus Schema Registry compatibility checks.
5. Kafka Streams windowed revenue and late-event handling, with event time and grace periods.
6. Dead-letter topics, replay tooling, consumer lag metrics, and OpenTelemetry traces.
7. Replace local PostgreSQL analytics with Snowflake while preserving Bronze/Silver/Gold model contracts.
