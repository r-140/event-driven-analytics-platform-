# Architecture and delivery guarantees

## Why both outbox and CDC?

Publishing to Kafka after committing a customer creates a failure window; publishing before commit can expose rolled-back data. The API instead inserts both records in one PostgreSQL transaction. Debezium reads the committed WAL and the Outbox Event Router creates the Kafka record. The database is the source of truth and the service never coordinates a distributed transaction.

Delivery is at least once. Kafka or Connect can replay a record, so `bronze.customer_events.event_id` is a primary key and `on conflict do nothing` makes consumption idempotent. This reference currently has one create event per customer and uses the aggregate UUID as its analytical event key. When update events are added, preserve the actual outbox event id in a Kafka header and use that id instead.

## Topic per domain

Each source service owns its database, outbox, logical replication slot, and connector. Topics are owned at domain level, so multiple services in one domain converge on the same topic:

| Domain | Source service/database | Connector | Topic |
|---|---|---|---|
| Customer | Customer / `customers` | `customer-outbox` | `outbox.event.customer` |
| Invoice | Invoice / `invoices` | `invoice-outbox` | `outbox.event.invoice` |
| Invoice | Invoice adjustment / `invoice_adjustments` | `invoice-adjustment-outbox` | `outbox.event.invoice` |
| Payment | `payments` | `payment-outbox` | `outbox.event.payment` |
| Identity | `identities` | `identity-outbox` | `outbox.event.identity` |

This prevents unrelated schemas, retention policies, partition counts, access rules, and consumer lifecycles from being coupled to one shared topic without incorrectly forcing one service per domain. Analytics subscribes to all domain topics and uses the `eventType` Kafka header to dispatch different events within the Invoice topic.

## Data layers

| Layer | Contract | Recovery role |
|---|---|---|
| Bronze | Immutable event payload plus ingestion metadata | Replay/audit source |
| Silver | Typed, deduplicated business entity | Reusable conformed data |
| Gold | Consumer-oriented country aggregation | Fast API/BI query |

The Java consumer demonstrates seconds-level materialization. dbt demonstrates deterministic backfill and quality enforcement. In production, choose one owner for each Gold table rather than letting both jobs write the same relation.

## Production gaps kept explicit

This is a local educational platform. A production deployment additionally needs authentication/authorization, TLS and Kafka ACLs, a schema registry with compatibility policy, secret management, connector offset backups, dead-letter handling, retry/lag alerts, OpenTelemetry export, partition/retention planning, and an orchestrator for dbt.
