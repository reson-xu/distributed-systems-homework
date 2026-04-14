# AGENTS.md

## 1. Project Overview

This repository is a distributed flash-sale and product inventory system built around independent microservices.

The current core business services are:

- Gateway Service
- User Service
- Product Service
- Order Service
- Inventory Service
- Payment Service

The system targets high-concurrency seckill scenarios and emphasizes correctness, clear service boundaries, and event-driven consistency.

## 2. Current Repository State

This repository is already a Maven multi-module microservice project.

Current top-level structure:

```text
framework/
  framework-core/
  framework-web/
services/
  gateway-service/
  user-service/
  product-service/
  order-service/
  inventory-service/
  payment-service/
deploy/
docs/
resources/
pom.xml
```

Any new work should align with this actual structure rather than the old single-module assumptions.

## 3. Primary Goals

All contributors and coding agents should optimize for:

- Correctness first, especially for stock consistency, order integrity, and payment/order state correctness
- Clear microservice boundaries and maintainable modular design
- High availability and graceful degradation under traffic spikes
- Event-driven consistency where local database transactions cannot cross service boundaries
- Clean, readable, production-oriented Java code

## 4. Suggested Tech Stack

Unless the repository intentionally changes direction, prefer:

- Java 17+
- Spring Boot
- Spring Cloud / Spring Cloud Alibaba
- Spring Cloud Gateway
- OpenFeign for synchronous inter-service calls
- MyBatis for persistence
- Redis for hot-path protection, caching, and idempotency support
- MySQL as the primary relational database
- RocketMQ for asynchronous event flow and eventual consistency
- OpenAPI 3 for API documentation
- Maven as the build tool

## 5. Service Boundaries

### 5.1 Gateway Service

- Unified traffic entry
- Route forwarding
- JWT parsing
- User identity relay to downstream services

### 5.2 User Service

- User registration
- User login
- JWT issuance
- Future user eligibility and anti-abuse checks

### 5.3 Product Service

- Product basic information
- Product detail query
- Product availability query
- Redis-based product detail caching and hotspot protection

### 5.4 Order Service

- Seckill order submission
- Order query
- Order creation result handling
- Payment result consumption and order status transitions

### 5.5 Inventory Service

- Available stock management
- Stock deduction
- Inventory flow recording
- Inventory result event publishing

### 5.6 Payment Service

- Payment submission
- Payment order persistence
- Payment result event publishing

## 6. Architecture Principles

Follow these principles by default:

- Preserve strict service boundaries
- Do not let one service write another service's tables directly
- Use local transactions inside a single service only
- Use RocketMQ to drive cross-service consistency when needed
- Design all critical write paths for idempotency
- Avoid overselling under any circumstance
- Do not treat Redis as final truth
- Keep business rules out of controllers and mapper XML

For seckill scenarios, always consider:

- Redis pre-deduction or reservation
- Duplicate order prevention
- User purchase limits
- Message retry safety
- Compensation and recovery logic
- Order status machine clarity

## 7. Code Organization Conventions

Keep changes inside the right module boundary.

Module guidance:

- `framework-core`
  Shared base response models, business exceptions, annotations, and stable cross-service utilities
- `framework-web`
  Shared web-layer concerns such as global exception handling and common AOP
- `services/*`
  Business capabilities owned by each service

Inside a service, prefer packages that keep technical responsibilities understandable, such as:

- `application`
- `domain`
- `interfaces`
- `infrastructure`
- `config`

If a service starts getting crowded, further split by:

- `controller`
- `client` or `remote`
- `mq`
- `persistence`

Do not move business-specific constants such as Redis keys into shared framework modules unless they are truly cross-service.

## 8. API and Documentation Rules

All externally exposed APIs should remain documented with OpenAPI-compatible annotations.

Requirements:

- Use `@Tag` for controller grouping
- Use `@Operation` for endpoint summary and description
- Use `@Schema` for DTO and VO field documentation where appropriate
- Keep `docs/api.md` aligned with actual controller behavior

When adding or changing public endpoints:

- Update controller annotations
- Update `docs/api.md`
- Update runbook or deployment docs if service dependencies change

## 9. Database and Persistence Rules

For MySQL and MyBatis usage:

- Keep SQL explicit and readable
- Do not hide business decisions inside mapper XML
- Add indexes intentionally
- Use unique constraints to enforce idempotency and invariants
- Keep transaction scopes tight
- Be explicit about status transitions and conditional updates

Current important tables include:

- `t_user`
- `t_product`
- `t_inventory`
- `t_inventory_flow`
- `t_order`
- `t_payment_order`
- `t_mq_consume_record`

Schema updates should be reflected in `resources/database/init_schema.sql`.

## 10. Redis Usage Rules

Redis is appropriate for:

- Seckill stock pre-deduction
- One-user-one-order restriction
- Hot data caching
- Short-lived idempotency keys
- Hotspot protection locks when justified

Rules:

- Final consistency must still be guaranteed by service-local DB state plus event flow
- Use TTLs intentionally
- Prefer atomic Lua scripts on critical hot paths
- Keep Redis keys owned by the service that uses them

## 11. MQ and Consistency Rules

RocketMQ is the default mechanism for cross-service eventual consistency.

Current consistency chains in the repository include:

- `order-service -> inventory-service -> order-service`
- `payment-service -> order-service`

Expectations:

- Consumers must be idempotent
- Message retries must not create duplicate business data
- Status transitions must be conditionally updated
- Compensation paths should be explicit when failures can leave partial state behind

## 12. Build, Run, and Verification Guidance

Preferred commands:

```text
mvn clean test
mvn clean package -DskipTests
mvn -pl services/order-service spring-boot:run
docker-compose -f deploy/docker-compose.microservices.yml up -d --build
```

Before pushing significant changes, run the most relevant local verification for:

- Compilation
- Unit tests where available
- Service-module packaging
- Compose config validation if deployment files changed

## 13. Testing Expectations

Testing is required for important business logic.

Prefer:

- Unit tests for application and domain rules
- Integration tests for MyBatis, Redis, and MQ-adjacent logic where practical
- Concurrency tests for stock deduction and duplicate-order prevention
- API tests for critical entry points

At minimum, verify:

- No overselling
- Duplicate order prevention
- Idempotent MQ consumption
- Safe order status transitions
- Safe payment result handling

## 14. Security and Compliance Basics

All contributors should:

- Validate input strictly
- Never trust client-side stock, price, or order status
- Avoid exposing internal details in API responses
- Protect secrets in configs and logs
- Keep JWT and infrastructure credentials out of committed source files

## 15. Commit Conventions

Use clear, atomic, review-friendly commits.

Recommended format:

```text
type(scope): short summary
```

Examples:

```text
feat(order): add idempotent seckill submit flow
feat(payment): add payment result event publishing
fix(inventory): prevent duplicate stock deduction on retry
docs(api): update payment endpoint contract
refactor(gateway): simplify jwt relay filter
```

## 16. Definition of Done

A task is not complete unless:

- Business logic is correct
- Edge cases and retry paths are considered
- Necessary tests are added or updated where practical
- API docs are updated if relevant
- Deployment or runtime docs are updated when service topology changes
- The result is reviewable and keeps service boundaries clear

## 17. Collaboration Notes for Coding Agents

When working in this repository:

- Prefer correctness over premature optimization
- Preserve service ownership boundaries
- Call out consistency risks explicitly
- Do not dump business-specific logic into `framework`
- Keep README, runbook, and API docs aligned with actual implementation
