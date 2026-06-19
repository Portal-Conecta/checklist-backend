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

The project follows a modular layered architecture. Business capabilities are
grouped by module, while dependencies flow from presentation and infrastructure
toward application and domain contracts.

```text
src/main/java/com/portal/conecta/checklist
|- Application.java
|- modules
|  |- checklist
|     |- application
|     |  |- port/out
|     |  |- service
|     |  |- usecase
|     |- domain
|     |  |- enums
|     |  |- exception
|     |  |- model
|     |  |- schema
|     |  |- service
|     |  |- valueobject
|     |- infrastructure
|     |- issues
|     |- presentation
|- shared
   |- context
   |- exception
   |- integration
   |- security
```

See [ADR-0001](docs/adr/0001-modular-layered-architecture.md) for the
architectural boundaries and dependency rules.

### Modules

`checklist`

Handles checklist templates, checklist executions, drafts, submitted checklists, answers, validation, and compliance calculation.

`issue`

Handles issues generated from non-compliant checklist items, including status, priority, assignee, resolution, and reopening rules when required.

`shared`

Contains cross-cutting code used by the service, such as security configuration, request context, global exception handling, and general configuration.

## Access Rules

The service follows the Hub token contract. Global user access comes from `userType`, and class-specific access is read from `classes[].role`:

- `STUDENT`
- `REPRESENTATIVE`
- `TEACHER`
- `SENAI`
- `WEG`
- `ADMIN`

Initial Checklist rules:

- `REPRESENTATIVE` can view and create checklists for their own class.
- `TEACHER` can view and create checklists for linked classes.
- `SENAI` can view dashboards and edit completed checklists within SENAI scope.
- `WEG` can view dashboards and edit completed checklists within WEG scope.
- `STUDENT` without representative class role has no operational Checklist access.
- `ADMIN` has Hub administration permissions, but no operational Checklist access by default.

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
SERVER_PORT=8083
DB_HOST=localhost
DB_PORT=5432
DB_NAME=checklist_db
DB_USER=your_db_user
DB_PASSWORD=your_db_password
JWT_SECRET=your_base64_hs256_secret
HUB_API_URL=http://localhost:8080
```

`JWT_SECRET` must use the same Base64-encoded HS256 secret configured for Checklist token validation. For local development, generate a temporary value and keep it only in `.env` or in your shell environment.

PowerShell example to generate a local 32-byte Base64 secret:

```powershell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

You can also create a local `.env` file at the project root. The application loads this file before Spring Boot starts, and values already defined in the operating system or command line keep priority.

Use `.env.example` as the local template. The real `.env` file is ignored by Git.

For deployed environments, configure sensitive values as GitHub Environment Secrets, not as repository variables or committed files:

- `JWT_SECRET`
- `DB_PASSWORD`
- `DB_USER`, if the database username is sensitive in your environment

### First Run

1. Install and open Docker Desktop.
2. Copy `.env.example` to `.env` and set `JWT_SECRET` to the Base64 HS256 secret configured by the local Hub. This value is required and must never be committed.
3. Start the API from IntelliJ or with Maven. With the `local` profile, Spring Boot starts PostgreSQL from `docker-compose.yml` automatically and waits until it is healthy.

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The first execution downloads the PostgreSQL image. Future runs reuse the existing container and database volume. The application does not stop the database when it exits.

The local PostgreSQL port is `5433` by default to avoid colliding with a database already installed on the machine. Change `DB_PORT` in `.env` only when that port is also unavailable.

`DB_USER` and `DB_PASSWORD` in `.env.example` are credentials exclusively for the disposable local PostgreSQL container. They are not valid credentials for any shared or production environment.

### Manage Local Infrastructure Manually

Normally, no manual Docker command is needed. Use these commands only when you want to inspect or manage the local infrastructure directly.

```bash
docker compose up -d postgres
```

Check the container status:

```bash
docker compose ps
```

Stop the local database:

```bash
docker compose down
```

Remove the local database volume:

```bash
docker compose down -v
```

To start Grafana as an optional local observability tool:

```bash
docker compose --profile observability up -d
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

Using a specific profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## API Documentation

Swagger UI should be available at:

```text
http://localhost:8083/swagger-ui.html
```

OpenAPI JSON should be available at:

```text
http://localhost:8083/v3/api-docs
```

## Health Check

Actuator health endpoint:

```text
http://localhost:8083/actuator/health
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

### Hub Token Authentication

Protected endpoints expect the Hub token in the request header:

```text
Authorization: Bearer <hub-token>
```

The Checklist API does not receive `userId` in request bodies or paths to identify the actor. The authenticated user is always resolved from the Hub token.

Expected token claims:

```json
{
  "jti": "abc-xyz-789",
  "sub": "11111111-1111-1111-1111-111111111111",
  "userType": "REPRESENTATIVE",
  "classes": [
    {
      "classId": "22222222-2222-2222-2222-222222222222",
      "role": "REPRESENTATIVE"
    }
  ],
  "iat": 1710000000,
  "exp": 1710003600
}
```

Current persistence uses UUIDs for users and classes, so `sub` and `classes[].classId` must be UUID strings. The Checklist API validates the token locally with the shared Base64 HS256 secret and then applies module authorization rules from the authenticated context.

The Hub currently uses two names for class role data depending on the contract surface:

- JWT access token: `classes[].role`;
- `/me/courses` response: `classes[].classRole`.

The current Hub token generator does not emit `permissionVersion`. If the Hub later adds that claim or an authorization-check endpoint, that flow should be introduced as a new integration decision instead of being assumed by this service.

### Testing With Postman

The Checklist API uses only data returned by the Hub. Start the Hub before the
Checklist API and use identifiers that exist in the Hub database.

#### What Is Being Tested

JWT is the token format used by the Hub. It has three parts:

```text
header.payload.signature
```

The payload is the JSON with `sub`, `userType`, `classes`, `iat`, and `exp`, but Postman must send the full signed token, not the raw JSON.

Checklist API validates:

- JWT signature using HS256 and `JWT_SECRET`;
- expiration date from `exp`;
- required claims: `jti`, `sub`, `userType`, `iat`, `exp`;
- `sub` as a valid user UUID;
- `classes[].classId` as valid class UUIDs;
- `classes[].role` as `STUDENT`, `TEACHER`, or `REPRESENTATIVE`;
- authenticated user context through `HubMeProvider` using the Hub `/me/courses` contract;
- room/class existence through Hub providers when the endpoint needs it.

#### Run API With The Hub

Start PostgreSQL and the Hub, then run the Checklist API:

```powershell
docker compose up -d
```

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
$env:SERVER_PORT="8083"
$env:JWT_SECRET="<BASE64_HS256_SECRET>"
$env:HUB_API_URL="http://localhost:8080"
mvn spring-boot:run
```

Health check does not require token:

```text
GET http://localhost:8083/actuator/health
```

#### Create A Postman Environment

Create an environment called `Checklist Local` with:

```text
BASE_URL=http://localhost:8083
HUB_URL=http://localhost:8080
ROOM_ID=<room UUID returned by the Hub>
TEACHER_CLASS_ID=<class UUID linked to the authenticated user>
```

#### Configure Authorization

In the Postman collection, open `Authorization`:

```text
Type: Bearer Token
Token: {{hubToken}}
```

Authenticate through the Hub login endpoint and save the returned access token
in the `hubToken` environment variable. The Checklist API does not issue tokens
and does not maintain a fallback user list.

The token sent to Postman must be the full signed JWT in this format:

```text
header.payload.signature
```

Do not paste only the JSON payload as the token.

#### First Request: Validate Authentication

```text
GET {{BASE_URL}}/api/checklist-templates
Authorization: Bearer {{hubToken}}
```

Expected result with the example token:

```text
200 OK
```

If the token is missing:

```text
401 Unauthorized
```

If the token is expired, unsigned, signed with the wrong secret, or has invalid claims:

```text
401 Unauthorized
```

If the token is valid but the user does not have permission for the action:

```text
403 Forbidden
```

#### Create A Template

Template management is allowed for `SENAI` or `WEG`. Use a token issued by the
Hub for a user with one of these types.

Request:

```text
POST {{BASE_URL}}/api/checklist-templates
Authorization: Bearer {{hubToken}}
Content-Type: application/json
```

Body:

```json
{
  "roomId": "{{ROOM_ID}}",
  "title": "Checklist Padrao",
  "description": "Template para teste local",
  "schemaJson": {
    "sections": [
      {
        "key": "estrutura",
        "title": "Estrutura",
        "order": 1,
        "items": [
          {
            "key": "quadro",
            "title": "Quadro em bom estado?",
            "description": "Verificar quadro",
            "required": true,
            "order": 1
          },
          {
            "key": "iluminacao",
            "title": "Iluminacao adequada?",
            "description": "Verificar luzes",
            "required": true,
            "order": 2
          }
        ]
      }
    ]
  }
}
```

Save the returned `id` manually as a Postman environment variable:

```text
TEMPLATE_ID=<id returned by API>
```

#### Activate The Template

Request:

```text
PATCH {{BASE_URL}}/api/checklist-templates/{{TEMPLATE_ID}}/activate
Authorization: Bearer {{hubToken}}
```

Expected result:

```text
200 OK
```

#### Create A Checklist Draft

Use a Hub token whose authenticated user has `TEACHER` or `REPRESENTATIVE` in
`TEACHER_CLASS_ID`.

Request:

```text
POST {{BASE_URL}}/api/checklist-executions/drafts
Authorization: Bearer {{hubToken}}
Content-Type: application/json
```

Body:

```json
{
  "templateId": "{{TEMPLATE_ID}}",
  "roomId": "{{ROOM_ID}}",
  "classId": "{{TEACHER_CLASS_ID}}",
  "checklistType": "ARRIVAL"
}
```

Save the returned `id` manually as:

```text
EXECUTION_ID=<id returned by API>
```

#### Submit The Checklist

Request:

```text
POST {{BASE_URL}}/api/checklist-executions/{{EXECUTION_ID}}/submit
Authorization: Bearer {{hubToken}}
Content-Type: application/json
```

Body:

```json
{
  "answers": [
    {
      "itemKey": "quadro",
      "value": "COMPLIANT",
      "observation": null,
      "answeredAt": "2026-05-27T12:00:00Z"
    },
    {
      "itemKey": "iluminacao",
      "value": "NON_COMPLIANT",
      "observation": "Lampada queimada",
      "answeredAt": "2026-05-27T12:01:00Z"
    }
  ]
}
```

Rules validated here:

- only `DRAFT` checklists can be submitted;
- all required items must be answered;
- `COMPLIANT` does not require observation;
- `NON_COMPLIANT` requires observation;
- `NON_COMPLIANT` generates an issue;
- only the user that created the execution can submit it.

#### Common Problems

`401 Token do Hub invalido ou expirado.`

- Token was pasted as raw JSON instead of JWT.
- `JWT_SECRET` in Postman is different from the API.
- `exp` is in the past.
- `sub`, `jti`, or `classes[].classId` is not a UUID.
- The Hub rejects the authenticated user or the token cannot access `/me/courses`.

`403 Acesso negado.`

- Token is valid, but `userType` or `classes[].role` does not allow the action.
- Example: `STUDENT` without `TEACHER` or `REPRESENTATIVE` class role trying to create a checklist draft.
- Example: `STUDENT` trying to create or activate checklist templates.

`404` or `Sala/Turma nao encontrada no Hub.`

- `ROOM_ID` does not exist in the Hub.
- `classId` does not exist in the Hub or is not linked to the authenticated user.

Duplicate checklist error.

- The API does not allow duplicated checklist for the same class, room, period, day, and type.
- To test again, change `checklistType`, class, or reset the local database.

Initial authorization rules implemented:

- A class representative can create checklist executions only for their own class.
- A linked teacher can create checklist executions for linked classes.
- `SENAI` and `WEG` can manage templates, view dashboards, and edit completed checklists.

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

- Whether `SENAI` and `WEG` can create checklist executions.
- Who can create, edit, activate, and disable checklist templates.
- How SENAI and WEG scopes will be resolved.
- Whether issues will have a full workflow in the MVP.
- Whether the project will use Flyway, Liquibase, or simple SQL scripts for schema control.
