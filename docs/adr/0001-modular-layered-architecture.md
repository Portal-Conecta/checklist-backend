# ADR-0001: Modular Layered Architecture

- Status: Accepted
- Date: 2026-06-12

## Context

The Checklist API contains checklist templates, executions, submission windows,
issues, security rules, persistence, and integration with the Hub. These
responsibilities previously used inconsistent package structures, with use
cases, services, ports, DTOs, and integration code placed at different levels.
That made ownership and dependency direction difficult to identify.

## Decision

The application adopts a business-module structure with explicit layers:

- `domain`: business models, value objects, schemas, exceptions, enums, and
  pure domain services.
- `application/usecase`: commands and queries that orchestrate business flows.
- `application/service`: reusable application coordination and validation.
- `application/port/out`: contracts required for persistence and external
  integrations.
- `infrastructure`: implementations of persistence contracts.
- `presentation`: controllers, transport DTOs, and HTTP mapping.
- `shared/integration`: Hub clients, adapters, mocks, and integration
  configuration shared by the module.

The `issues` capability remains inside the Checklist module because issues are
currently created and managed as part of checklist executions. It keeps its own
internal application, domain, infrastructure, and presentation boundaries.

Pure rules that combine domain concepts without external dependencies are
placed in `domain/service`. For example, `PeriodResolver` derives the execution
period from `Shift` and `ChecklistType`.

Dependencies must point inward:

```text
presentation -> application -> domain
infrastructure -> application ports -> domain
shared integration -> application ports
```

The domain must not depend on Spring MVC, persistence adapters, Hub clients, or
presentation DTOs. Application use cases depend on ports rather than concrete
repositories or HTTP clients.

## Consequences

### Positive

- Package names communicate the responsibility of each class.
- Use cases are separated from reusable services and integration contracts.
- Infrastructure can be replaced without changing business orchestration.
- Domain rules remain testable without Spring or external services.
- Empty placeholder packages are unnecessary and should not be kept.

### Trade-offs

- Package paths and imports become longer.
- Small capabilities may initially have only one class in a layer.
- Moving issues into an independent top-level module later will require a
  dedicated application port between Checklist and Issues.

## Validation

Architecture tests verify package consistency and prevent application code from
depending directly on presentation or infrastructure packages. The complete
test suite must pass after structural changes.
