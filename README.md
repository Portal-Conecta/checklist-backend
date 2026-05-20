# Checklist API

Checklist API is a Spring Boot service for the Portal Conecta ecosystem. It is responsible for checklist templates, checklist executions, answers, compliance calculation, and issues generated from non-compliant items.

The service must keep its own business rules and data ownership. The Hub remains responsible for central data such as users, classes, courses, rooms, authentication, and global permissions.

## Tech Stack

- Java 21
- Spring Boot 4.0.6
- Maven
- PostgreSQL
- Docker and Docker Compose
- Spring Web
- Spring Validation
- Spring Security
- Spring Data JPA
- Spring Actuator
- OpenFeign
- Micrometer / Observability
- Springdoc OpenAPI
- JUnit and Mockito
- JJWT `0.12.6`

## Architecture

The project follows a business-module structure.

```text
src/main/java/br/senai/centroweg/checklist
|- ChecklistApplication.java
|- module
|  |- checklist
|  |  |- domain
|  |  |- application
|  |  |- presentation
|  |  |- infrastructure
|  |- issue
|     |- domain
|     |- application
|     |- presentation
|     |- infrastructure
|- shared
   |- config
   |- exception
   |- security
   |- context
```

### Modules

`checklist`

Handles checklist templates, checklist executions, drafts, submitted checklists, answers, validation, and compliance calculation.

`issue`

Handles issues generated from non-compliant checklist items, including status, priority, assignee, resolution, and reopening rules when required.

`shared`

Contains cross-cutting code used by the service, such as security configuration, request context, global exception handling, and general configuration.

## Access Rules

The service follows the official Hub access profiles:

- `APRENDIZ`
- `REPRESENTANTE`
- `DOCENTE`
- `PERFIL_SENAI`
- `PERFIL_WEG`
- `ADMINISTRADOR`

Initial Checklist rules:

- `REPRESENTANTE` can view and create checklists for their own class.
- `DOCENTE` can view and create checklists for linked classes.
- `PERFIL_SENAI` can view dashboards and edit completed checklists within SENAI scope.
- `PERFIL_WEG` can view dashboards and edit completed checklists within WEG scope.
- `APRENDIZ` has no Checklist access.
- `ADMINISTRADOR` has Hub administration permissions, but operational Checklist access still requires confirmation.

Legacy roles such as `GESTOR`, `INSTRUTOR`, `PROFESSOR`, `ALUNO`, and `ADMIN` must not be used in new Checklist rules.

## Local Setup

### Requirements

- Java 21
- Maven or Maven Wrapper
- Docker and Docker Compose
- PostgreSQL, if not using Docker Compose

### Environment Variables

Suggested local variables:

```env
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8080
DB_HOST=localhost
DB_PORT=5432
DB_NAME=checklist_db
DB_USER=checklist_user
DB_PASSWORD=checklist_password
JWT_SECRET=change-me
HUB_API_URL=http://localhost:8081
```

### Run PostgreSQL

```bash
docker compose up -d
```

### Run the API

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

If Maven Wrapper is not available:

```bash
mvn spring-boot:run
```

## API Documentation

Swagger UI should be available at:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON should be available at:

```text
http://localhost:8080/v3/api-docs
```

## Health Check

Actuator health endpoint:

```text
http://localhost:8080/actuator/health
```

## Testing

Run all tests:

```bash
mvn test
```

Minimum expected test coverage for the first version:

- business rules for checklist submission;
- required-answer validation;
- compliance calculation;
- issue creation from non-compliant items;
- access rules for main profiles;
- controller validation for important requests.

## Integration With Hub

Authentication is centralized in the Hub. Checklist API must validate the platform token before executing protected actions.

When Checklist API needs Hub data, it should call Hub through HTTP contracts, preferably from the infrastructure layer using OpenFeign.

The Checklist service must not directly access Hub database tables.

## Database Ownership

Checklist API owns only Checklist data, such as:

- checklist templates;
- checklist executions;
- checklist answers;
- checklist issues;
- compliance-related fields.

Central entities such as users, classes, courses, rooms, and global roles belong to the Hub.

## Development Guidelines

- Keep controllers thin.
- Keep business rules out of DTOs and controllers.
- Put use cases in the application layer.
- Keep domain code independent from Spring, JPA, HTTP, and Feign when possible.
- Put external HTTP clients in `infrastructure/client`.
- Use OpenAPI annotations to keep contracts clear.
- Use validation annotations for request DTOs.
- Use clear error responses through the global exception handler.
- Avoid adding dependencies before there is a real need.

## Team Responsibilities

- Daniel: project setup, architecture, security, authorization, integration, dashboard, and completed-checklist edit rules.
- Kael: domain model, persistence, checklist business rules, issues, and core tests.
- Murilo: DTOs, controllers, validation, OpenAPI documentation, request examples, and technical README updates.

## Pending Decisions

- Whether `PERFIL_SENAI` and `PERFIL_WEG` can create checklists.
- Who can create, edit, activate, and disable checklist templates.
- How SENAI and WEG scopes will be resolved.
- Whether issues will have a full workflow in the MVP.
- Whether the project will use Flyway, Liquibase, or simple SQL scripts for schema control.
