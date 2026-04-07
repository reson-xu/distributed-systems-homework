# AGENTS.md

## 1. Project Overview

This repository is intended for a distributed product inventory and flash-sale system.

The initial business scope includes:

- User Service
- Product Service
- Order Service
- Inventory Service

The system is expected to support high-concurrency purchase scenarios, especially flash-sale events, while maintaining correctness for stock deduction, order creation, and user purchase constraints.

## 2. Primary Goals

All contributors and coding agents should optimize for the following goals:

- Correctness first, especially for stock consistency and order integrity
- Clear service boundaries and maintainable modular design
- High availability and graceful degradation under traffic spikes
- Observability, traceability, and operational simplicity
- Clean, readable, and production-oriented Java code

## 3. Suggested Tech Stack

Unless the repository evolves in a different direction, prefer the following stack:

- Java 17+ if not constrained otherwise
- Spring Boot for service development
- MyBatis for data access
- Redis for cache, distributed coordination, rate limiting, and flash-sale protections
- MySQL as the primary relational database
- RocketMQ for asynchronous order creation, buffering, and compensation-oriented event flow
- OpenAPI 3 for API documentation
- Maven as the default build tool unless Gradle is explicitly introduced

## 4. Current Repository State

At the time this document is initialized, the repository does not yet contain the actual Java project skeleton, build files, or runtime modules.

Until the codebase is bootstrapped:

- Treat this file as the baseline collaboration contract
- Keep future project initialization aligned with the service boundaries and standards defined here
- Update this file once actual module names, package names, and build commands are finalized

### 4.1 Collaboration Assets

The repository should keep a lightweight collaboration structure:

```text
agents/
  product.md
  architect.md
  engineer.md
  reviewer.md
docs/
  prd.md
  architecture.md
  api.md
```

Purpose of these directories:

- `agents/` stores role-specific prompts and execution expectations
- `docs/` stores the working product, architecture, and API design documents

### 4.2 Agent Ownership Mapping

Use the following ownership model by default:

- Product Agent -> PRD definition and refinement in `docs/prd.md`
- Architect Agent -> system architecture design in `docs/architecture.md`
- Engineer Agent -> implementation and API contract alignment, including `docs/api.md`
- Reviewer Agent -> review of requirements, architecture, code quality, risks, and test coverage

### 4.3 Standard Collaboration Flow

The expected delivery sequence is:

1. Product Agent clarifies scope, business rules, user journeys, constraints, and acceptance criteria
2. Architect Agent translates the PRD into service boundaries, data flow, consistency design, and deployment-oriented technical decisions
3. Engineer Agent implements the solution based on approved requirements and architecture, and keeps the API specification aligned with the implementation
4. Reviewer Agent evaluates correctness, maintainability, concurrency safety, test quality, and documentation completeness

Collaboration rules:

- Do not skip PRD clarification for ambiguous requirements
- Do not start implementation before core architecture decisions are documented
- Update `docs/api.md` whenever an externally exposed API is added or changed
- Reviewer findings take precedence over convenience when correctness or stability is at risk
- Keep prompts and documents synchronized with actual repository practices

## 5. Recommended Service Boundaries

Use the following service responsibilities as the baseline:

### 5.1 User Service

- User registration, login, profile, and identity-related capabilities
- User eligibility checks for flash-sale activities
- User-level purchase restrictions and anti-abuse support

### 5.2 Product Service

- Product basic information and product detail query
- Product status management
- Flash-sale product metadata management

### 5.3 Order Service

- Order creation, query, status transitions, and idempotent processing
- Order timeout, cancellation, and compensation handling
- Integration with inventory reservation and deduction results

### 5.4 Inventory Service

- Available stock management
- Reserved stock and deduction workflow
- Anti-oversell control
- High-concurrency inventory protection during flash-sale events

## 6. Architecture and Design Principles

Follow these principles by default:

- Prefer clear layered architecture: controller, application/service, domain, infrastructure, persistence
- Keep business rules in service/domain layers, not in controllers or mapper XML
- Design for idempotency in all critical write operations
- Avoid overselling under any circumstance
- Separate normal inventory flow from flash-sale hot-path optimizations when needed
- Use Redis and database together carefully; do not assume cache equals truth
- Make trade-offs explicit when consistency, throughput, and latency are in tension

For flash-sale scenarios, contributors should explicitly consider:

- Pre-deduction or reservation strategy
- Duplicate order prevention
- User purchase limit enforcement
- Request throttling and rate limiting
- Message-driven async processing if introduced later
- Compensation and recovery logic for partial failures

## 7. Code Organization Conventions

When the repository structure is created, prefer a layout similar to:

```text
src/main/java/io/github/resonxu/seckill
  /common
  /config
  /user
  /product
  /order
  /inventory
src/main/resources
  /mapper
  /application.yml
src/test/java
```

General module guidance:

- Put shared utilities in `common`, but do not turn it into a dumping ground
- Keep each bounded context internally cohesive
- Use package names that reflect business capability, not vague technical labels
- Keep DTO, VO, DO, and entity naming consistent across services
- Keep collaboration documents in `docs/` and role prompts in `agents/`

## 8. Java and Coding Standards

All code should follow:

- Alibaba Java Development Manual
- Standard Java best practices
- Spring Boot project conventions already adopted in the repository

Key expectations:

- Use meaningful class, method, and variable names
- Keep methods focused and short where practical
- Avoid deeply nested conditionals; prefer early return
- Do not use magic numbers; extract constants with meaningful names
- Minimize duplicated logic
- Prefer composition over overly complex inheritance
- Use enums for bounded business states
- Handle null values explicitly and defensively
- Log with context, but never log secrets or sensitive data

Formatting and style:

- Keep code readable and consistent rather than clever
- Use Lombok only when it clearly reduces boilerplate without harming readability
- Avoid unnecessary static utility classes for business logic
- Do not mix controller request models with persistence models

## 9. API and OpenAPI Standards

All externally exposed APIs should be documented with OpenAPI-compatible annotations and clear comments.

Requirements:

- Add clear endpoint summaries and descriptions
- Document request and response models
- Document validation constraints and business limitations
- Keep field names precise and stable
- Use explicit error codes and messages

Recommended annotation direction:

- Use `@Operation` for endpoint summary and description
- Use `@Schema` for request and response fields
- Use `@Tag` to group controllers by business domain

API design guidelines:

- Prefer RESTful naming where practical
- Keep write APIs idempotent whenever possible
- Standardize response structure if the project adopts a common wrapper
- Clearly distinguish business failure from system failure
- Keep `docs/api.md` aligned with controller definitions and OpenAPI annotations

## 10. Database and Persistence Guidelines

For MySQL and MyBatis usage:

- Keep SQL explicit and readable
- Do not hide complex business logic inside mapper XML
- Add indexes intentionally based on query patterns
- Design unique constraints to support idempotency and business invariants
- Use transactions only where necessary and keep transaction scopes tight
- Be explicit about isolation and locking strategy in inventory deduction paths

Inventory and order tables should be designed with care for:

- Stock correctness
- Order uniqueness
- User purchase constraints
- Timeout and cancellation recovery

## 11. Redis Usage Guidelines

Redis may be used for:

- Hot data caching
- Flash-sale token control
- Rate limiting
- Distributed locking only when truly necessary
- Deduplication or idempotency markers

Important rules:

- Do not rely on Redis alone for final consistency
- Set TTLs intentionally and document why they exist
- Prevent cache breakdown, penetration, and avalanche where relevant
- Prefer atomic operations or Lua scripts for critical high-concurrency paths

## 12. Error Handling and Observability

All services should:

- Use consistent exception handling
- Return stable and documented error responses
- Log enough context to troubleshoot production issues
- Include trace or request correlation where infrastructure supports it

For flash-sale and inventory paths, logs and metrics should make it possible to inspect:

- Stock deduction attempts
- Reservation success or failure
- Duplicate order interception
- Rate limit triggers
- Compensation execution

## 13. Build, Run, and Verification Guidance

Once the project skeleton is created, prefer standard Maven commands such as:

```text
mvn clean test
mvn spotless:check
mvn spring-boot:run
```

If the repository adopts different plugins or build tooling later, update this section immediately.

Before opening a PR, contributors should run the relevant local verification for:

- Compilation
- Unit tests
- Integration tests where available
- Static checks or formatting checks if configured

## 14. Testing Expectations

Testing is required for all important business logic.

Prefer the following:

- Unit tests for core business rules
- Integration tests for database and Redis interactions
- Concurrency tests for inventory deduction and flash-sale flows
- API tests for critical endpoints

At minimum, verify:

- No overselling
- Duplicate order prevention works
- Idempotency logic works
- Timeout and cancellation paths are correct
- Failure and retry paths are safe

## 15. Security and Compliance Basics

All contributors should:

- Validate input strictly
- Never trust client-side stock or price data
- Avoid exposing internal implementation details in API responses
- Protect sensitive information in logs and configs
- Keep credentials out of source code

## 16. Commit Conventions

Use clear, atomic, and review-friendly commits.

Recommended format:

```text
type(scope): short summary
```

Examples:

```text
feat(order): add idempotent order creation flow
fix(inventory): prevent oversell during concurrent stock deduction
refactor(product): simplify product query service
test(seckill): add concurrent purchase integration tests
docs(api): document order creation endpoint
```

Recommended commit types:

- `feat`
- `fix`
- `refactor`
- `test`
- `docs`
- `chore`

Commit rules:

- One logical change per commit where practical
- Write summaries in imperative mood
- Do not mix broad refactoring with unrelated feature work
- Include tests in the same commit when they directly validate the change

## 17. Pull Request Conventions

Every PR should be easy to review and include:

- Clear problem statement
- Summary of the solution
- Scope and impacted modules
- Testing performed
- Risk notes and rollback considerations when applicable

PR checklist:

- Code follows Alibaba and Java style expectations
- OpenAPI annotations and comments are updated if APIs changed
- Tests are added or updated
- Backward compatibility is considered
- Configuration and schema changes are documented

## 18. Definition of Done

A task is not complete unless all of the following are true:

- Business logic is implemented correctly
- Edge cases and failure paths are considered
- Necessary tests are added or updated
- API documentation is updated if relevant
- Logging and error handling are adequate
- The change is small enough to review effectively

## 19. Collaboration Notes for Coding Agents

When working in this repository:

- Preserve clear service boundaries
- Favor correctness over premature optimization
- Call out consistency risks explicitly
- Do not introduce infrastructure complexity unless justified by the business scenario
- Keep the codebase easy for human developers to understand and extend
- Follow the role prompts under `agents/` when operating in a specialized agent role
- Treat `docs/prd.md`, `docs/architecture.md`, and `docs/api.md` as living documents, not one-time artifacts

If the repository later introduces concrete frameworks, module names, or architectural constraints, update this file to match the actual implementation rather than keeping this document generic.
