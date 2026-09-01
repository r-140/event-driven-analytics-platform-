# Demo guide

1. Start the stack and register CDC using the README commands.
2. Create one customer and inspect `select * from customers` and `select * from outbox_events` in the customer database.
3. List the four `outbox.event.*` topics. Generate data and consume `outbox.event.invoice`: both `InvoiceIssued` and `InvoiceAdjusted` arrive there even though they originate from separate databases and connectors.
4. Query `bronze.customer_events`, `silver.customers`, and `gold.customer_summary` in the analytics database.
5. Re-run a Kafka record and show that the Bronze primary key prevents double counting.
6. Run `dbt build` and introduce an invalid country code directly in Bronze to demonstrate the singular data-quality test.
7. Stop the analytics service, generate events, restart it, and show consumer-group catch-up.
8. Open Airflow and trigger `dbt_analytics`; inspect the run and model-level results on the dbt dashboard.
9. Stop `invoice-adjustment-service` or pause its connector, wait for `platform_health`, and show the connector/freshness anomaly in Grafana.
10. Leave generation stopped for more than 10 minutes to demonstrate CDC freshness-SLA breaches, then generate records and show automatic anomaly resolution.
11. Trigger `dbt_analytics`, then open `http://localhost:8090`. Run Customers by country and Invoice value by month; inspect the semantic request and generated parameterized SQL.

Useful database shells:

```bash
docker compose exec customer-db psql -U platform -d customers
docker compose exec analytics-db psql -U platform -d analytics
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:29092 --topic outbox.event.customer --from-beginning
```
