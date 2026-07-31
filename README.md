# Event-Driven Analytics Platform

A production-like reference implementation of a modern event-driven analytics platform built with Java and Spring Boot.

The project demonstrates how transactional microservices can evolve into a scalable analytical platform using Change Data Capture (CDC), event streaming, and modern data engineering practices.

The primary goal is educational: to showcase software architecture, data engineering patterns, and production-ready design principles rather than build a business application.

---

## Project Goals

* Build production-like Spring Boot microservices
* Apply Hexagonal Architecture and SOLID principles
* Demonstrate Database-per-Service architecture
* Generate realistic operational workloads
* Introduce Change Data Capture (CDC)
* Build an event-driven streaming pipeline
* Design an analytical platform using modern data engineering concepts
* Implement dimensional data models and ELT pipelines
* Demonstrate monitoring, testing, and operational best practices

---

## Technology Stack

### Backend

* Java 21
* Spring Boot 3
* Maven
* Spring Data JPA
* Spring Validation

### Database

* PostgreSQL
* Flyway

### Infrastructure

* Docker
* Docker Compose

### Testing

* JUnit 5
* Testcontainers

### Planned Components

* Apache Kafka
* Kafka Connect
* Debezium
* Schema Registry
* Snowflake
* dbt
* Power BI (or equivalent BI tooling)

---

## Project Structure

```text
event-driven-analytics-platform/

├── services/
│   ├── customer-service/
│   ├── invoice-service/
│   ├── payment-service/
│   ├── identity-service/
│   └── data-generator/
│
├── infrastructure/
│
├── docs/
│
└── pom.xml
```

---

## Development Roadmap

### Phase 1 — Operational Platform

* Multi-module Maven project
* Spring Boot microservices
* Hexagonal Architecture
* REST APIs
* PostgreSQL
* Docker Compose

### Phase 2 — Event-Driven Integration

* Domain events
* Outbox Pattern
* Apache Kafka
* Consumer applications

### Phase 3 — Change Data Capture

* PostgreSQL WAL
* Debezium
* Kafka Connect
* Schema Registry

### Phase 4 — Analytical Platform

* Data ingestion
* Bronze / Silver / Gold architecture
* ELT pipelines
* Dimensional modeling
* Data quality

### Phase 5 — Analytics

* Dashboards
* Reporting
* Performance optimization
* Cost optimization

---

## Architecture Principles

The project follows several architecture principles commonly used in enterprise systems:

* Hexagonal Architecture
* SOLID principles
* Domain-Driven Design (selected concepts)
* Database per Service
* Event-Driven Architecture
* Loose Coupling
* High Cohesion
* Infrastructure as Code (planned)

---

## Current Status

🚧 Work in progress.

The project is currently focused on building the operational microservices that will later serve as the source of truth for the analytical platform.

---

## License

This project is licensed under the MIT License.
