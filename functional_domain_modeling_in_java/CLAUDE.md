# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

『関数型ドメインモデリング』(Domain Modeling Made Functional by Scott Wlaschin) をJavaで実装する学習プロジェクト。受注システムをサンプルドメインとして、関数型アプローチによるドメインモデリングをSpring Boot + Javaで実践する。

## Commands

```bash
# Build
./mvnw clean compile

# Run tests
./mvnw test

# Run single test class
./mvnw test -Dtest=DemoApplicationTests

# Run the application
./mvnw spring-boot:run

# Package
./mvnw package
```

## Architecture

**Hexagonal Architecture (Ports & Adapters)**

```
com.example.fdmj/
├── core/
│   ├── model/     # Domain models (sealed interfaces, records for value objects)
│   └── service/   # Domain services / workflow implementations
└── shell/
    ├── adapter/
    │   ├── in/    # Input adapters (REST controllers, event consumers)
    │   └── out/   # Output adapters (repositories, external service clients)
    └── application/  # Application orchestration (use case coordinators)
```

- `core/` has zero dependency on `shell/` — domain logic must not depend on infrastructure
- `shell/` depends on `core/` via interfaces defined in `core/`

## Domain Design (docs/book.md)

The target domain is an **order placement system (受注)** with these key concepts:

**Order Lifecycle types** (state encoded in the type system):
- `UnvalidatedOrder` → `ValidatedOrder` → `PricedOrder` → `PlacedOrderAcknowledgement`

**Main workflow: 注文を確定する (PlaceOrder)**
1. `ValidateOrder` — validates addresses, product codes; returns `ValidatedOrder | ValidationError`
2. `PriceOrder` — looks up product prices, computes line totals; returns `PricedOrder`
3. `SendAcknowledgementToCustomer` — sends acknowledgement letter

**Value object constraints** (enforce via constructor validation or factory methods):
- `ProductCode`: sealed — `WidgetCode` (W + 4 digits) or `GizmoCode` (G + 3 digits)
- `OrderQuantity`: sealed — `UnitQuantity` (1–1000, Integer) or `KilogramQuantity` (0.05–100.00, BigDecimal)

**Bounded context relationships** (docs/book.md §3.3):
- AddressCheck → AntiCorruptionLayer → 受注
- 製品カタログ → 受注 (conformist)
- 受注 → 発送 (shared kernel)
- 受注 → 請求 (consumer-driven)

## Java Patterns for Functional Modeling

- Use **`sealed interface` + `record`** for algebraic data types (sum types / product types)
- Use **`Optional` / custom `Result<T, E>`** for error handling instead of exceptions in domain logic
- Workflow steps are **pure functions**: `Function<Input, Output>` with dependencies injected as function parameters
- Avoid domain events *within* a bounded context (§3.4.2); emit events only at context boundaries
