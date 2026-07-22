# Checklist API

Serviço Spring Boot do ecossistema **Portal Conecta**, responsável por checklists
de vistoria de salas: templates versionados, execuções (rascunho e envio),
respostas, janelas de envio, cálculo de conformidade e *issues* geradas a partir
de itens não conformes.

O serviço mantém suas próprias regras de negócio e é dono apenas dos dados de
checklist. O **Hub** (core-backend) continua responsável pelos dados centrais —
usuários, turmas, cursos, salas, autenticação e permissões globais. A Checklist
API nunca acessa o banco do Hub diretamente: consome-o via contratos HTTP.

---

## Sumário

- [Stack técnica](#stack-técnica)
- [Arquitetura](#arquitetura)
- [Domínio e conceitos](#domínio-e-conceitos)
- [Endpoints da API](#endpoints-da-api)
- [Regras de acesso](#regras-de-acesso)
- [Setup local](#setup-local)
- [Perfis e seed de desenvolvimento](#perfis-e-seed-de-desenvolvimento)
- [Migrations (Flyway)](#migrations-flyway)
- [Observabilidade](#observabilidade)
- [Documentação da API (Swagger/OpenAPI)](#documentação-da-api-swaggeropenapi)
- [Testes](#testes)
- [Integração com o Hub](#integração-com-o-hub)
- [Fluxo ponta a ponta](#fluxo-ponta-a-ponta)
- [Ownership de dados](#ownership-de-dados)
- [Diretrizes de desenvolvimento](#diretrizes-de-desenvolvimento)
- [Documentação do projeto](#documentação-do-projeto)

---

## Stack técnica

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Build | Maven (via Maven Wrapper) |
| Banco | PostgreSQL 15 |
| Migrations | Flyway (`spring-boot-starter-flyway` + `flyway-database-postgresql`) |
| Web | Spring Web MVC + Spring Validation |
| Segurança | Spring Security + JJWT `0.12.x` (validação de token HS256) |
| Persistência | Spring Data JPA / Hibernate (JSONB para schema/respostas) |
| Integração | Spring Cloud OpenFeign (contratos HTTP com o Hub) |
| Mensageria | Spring AMQP / RabbitMQ (opcional, para notificações — ver `RABBITMQ_ENABLED`) |
| Observabilidade | Spring Actuator, Micrometer, Prometheus, tracing OTLP, logs JSON com MDC |
| Documentação | Springdoc OpenAPI (Swagger UI) |
| Testes | JUnit 5, Mockito, Spring Boot Test, Testcontainers |
| Log corporativo | `com.portal.conecta:portal-logging` (GitHub Packages, privado) |
| Containerização | Docker e Docker Compose |

> A dependência **`portal-logging`** é privada (GitHub Packages). Builds locais
> fora do Docker (`mvn`/IDE) exigem `MAVEN_USERNAME` e `MAVEN_PASSWORD` no
> ambiente — ver [Variáveis de ambiente](#variáveis-de-ambiente).

---

## Arquitetura

Arquitetura **modular em camadas**. As capacidades são agrupadas por módulo de
negócio; as dependências fluem da apresentação/infraestrutura em direção aos
contratos de aplicação e domínio.

```text
src/main/java/com/portal/conecta/checklist
├── Application.java
├── module
│   ├── checklist              # templates, execuções, janelas, conformidade, stats
│   │   ├── application         # use cases, comandos/queries, portas (ports/out)
│   │   ├── domain              # enums, model, schema (JSONB), value objects, regras
│   │   ├── infrastructure      # persistência (JPA) e integrações concretas
│   │   └── presentation        # controllers, DTOs, mappers
│   └── issues                 # módulo de negócio independente (máquina de estados)
└── shared                     # transversais
    ├── config                  # OpenAPI, beans, seed de dev
    ├── context                 # contexto da requisição (usuário autenticado)
    ├── exception               # tratamento global de erros (ApiError)
    ├── integration             # clientes/adapters do Hub (OpenFeign / RestClient)
    ├── messaging               # mensageria (notificações via RabbitMQ)
    └── security                # validação de token e autorização
```

Regras de fronteira e dependência: ver [ADR-0001 — Arquitetura modular](docs/adr/0001-arquitetura-modular.md)
e [ADR-0020 — Issues como módulo de negócio independente](docs/adr/0020-issues-como-modulo-de-negocio-independente.md).

### Módulos

- **`checklist`** — templates (criação, edição, ativação, versionamento imutável),
  execuções (rascunho, autosave, envio, cancelamento, histórico), respostas,
  janelas de envio por turno/tipo, cálculo de conformidade e endpoints agregados
  de estatística/dashboard.
- **`issues`** — *issues* geradas a partir de itens não conformes, com máquina de
  estados completa (iniciar, resolver, validar, reabrir, retomar, cancelar).
- **`shared`** — código transversal: segurança, contexto de requisição, tratamento
  global de exceções, integração com o Hub, mensageria e configuração.

---

## Domínio e conceitos

| Conceito | Descrição |
|---|---|
| **Template** | Modelo de checklist de uma sala. Guarda o schema (seções/itens) em JSONB. É **versionado e imutável**: editar gera nova versão; só um template pode estar `ACTIVE` por sala. |
| **ChecklistType** | Momento operacional do checklist: `ARRIVAL` (chegada) e `POST_BREAK` (pós-intervalo). |
| **ChecklistCategory** | Recorte do checklist por tipo de item/patrimônio da sala: `ELETRONICOS`, `MOVEIS`, `ILUMINACAO`, `CLIMATIZACAO`, `INFRAESTRUTURA`, `HIGIENE`, `GERAL`. |
| **Shift** | Turno da turma: `FULL_AM_PM`, `FULL_PM_NT`. |
| **Execução** | Preenchimento de um template. Passa por `DRAFT` (com autosave incremental) até `SUBMITTED`; pode ser cancelada. Só o autor envia. |
| **Resposta** | Valor por item: `COMPLIANT` (não exige observação) ou `NON_COMPLIANT` (exige observação e gera *issue*). |
| **Janela de envio** | *Submission window* por turma + `ChecklistType`, definindo horário de abertura (`openAt`) e duração (`durationMinutes`). O envio é validado contra a janela aberta. |
| **Conformidade** | Percentual calculado a partir das respostas do checklist enviado. |
| **Issue** | Ocorrência de item não conforme, com ciclo de vida próprio (ver máquina de estados). |

Referências: [ADR-0002 (tipos)](docs/adr/0002-redefinicao-tipos-checklist.md) ·
[ADR-0004 (janela por turno)](docs/adr/0004-janela-de-envio-por-shift.md) ·
[ADR-0011 (JSONB)](docs/adr/0011-persistencia-jsonb-schema-respostas.md) ·
[ADR-0012 (versionamento)](docs/adr/0012-versionamento-imutabilidade-template.md) ·
[ADR-0013 (conformidade/issues)](docs/adr/0013-conformidade-e-geracao-de-issues.md) ·
[ADR-0014 (máquina de estados da issue)](docs/adr/0014-maquina-estados-issue.md).

---

## Endpoints da API

Base local: `http://localhost:8083`. Todos os endpoints de negócio exigem o
token do Hub (`Authorization: Bearer <token>`).

### Templates — `/api/checklist-templates`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/` | Cria template (SENAI/WEG). |
| `GET` | `/?roomId=&status=` | Lista templates, com filtro por sala e status (`ACTIVE`, …). |
| `GET` | `/{templateId}` | Detalha um template. |
| `PATCH` | `/{templateId}` | Edita (gera nova versão conforme regra de imutabilidade). |
| `PATCH` | `/{templateId}/activate` | Ativa o template (torna-o o ativo da sala). |
| `POST` | `/{templateId}/new-version` | Cria nova versão a partir de um template. |
| `GET` | `/items/search?query=` | Busca itens por texto. |
| `GET` | `/items/search?category=` | Busca itens por categoria. |

### Execuções — `/api/checklist-executions`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/drafts` | Cria rascunho de execução. |
| `PATCH` | `/{executionId}/answers` | Autosave incremental de respostas do rascunho. |
| `PATCH` | `/{executionId}/draft` | Atualiza o rascunho. |
| `POST` | `/{executionId}/submit` | Envia o checklist (valida obrigatórios, janela e conformidade). |
| `PATCH` | `/{executionId}/cancel` | Cancela a execução. |
| `GET` | `/{executionId}` | Detalha uma execução. |
| `GET` | `/history/class/{classId}` | Histórico de execuções da turma (com filtros). |

### Janelas de envio — `/api/submission-windows`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/classes/{classId}` | Consulta as janelas de uma turma. |
| `PUT` | `/classes/{classId}/{checklistType}` | Cria/atualiza a janela (`openAt`, `durationMinutes`) — SENAI/WEG. |

### Issues — `/api/checklist-issues`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/execution/{executionId}` | Lista as issues de uma execução. |
| `PATCH` | `/{issueId}/start` | Inicia o tratamento. |
| `PATCH` | `/{issueId}/resolve` | Marca como resolvida. |
| `PATCH` | `/{issueId}/validate` | Valida a resolução (SENAI/WEG). |
| `PATCH` | `/{issueId}/reopen` | Reabre (SENAI/WEG). |
| `PATCH` | `/{issueId}/restart-progress` | Retoma o progresso. |
| `PATCH` | `/{issueId}/cancel` | Cancela a issue. |

### Estatísticas e dashboard

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/checklist-stats/dashboard` | Dashboard composto (com cache), restrito à gestão. |
| `GET` | `/api/checklist-executions/stats/*` | `completion-rate`, `avg-fill-time`, `with-issues-rate`, `heatmap`, `compliance-trend`. |
| `GET` | `/api/checklist-issues/stats/*` | `resolution-split`, `resolution-rate`, `avg-resolution-time`, `overdue`, `per-execution`. |
| `GET` | `/api/submission-windows/stats/avg-duration` | Duração média das janelas. |
| `GET` | `/api/checklist-templates/stats/*` | Agregações de templates. |

Ver [ADR-0016 (agregações)](docs/adr/0016-endpoints-agregacao-stats.md) e
[ADR-0017 (dashboard composto com cache)](docs/adr/0017-dashboard-composto-cache.md).

---

## Regras de acesso

O serviço segue o contrato de token do Hub. O acesso global vem de `userType`; o
acesso por turma vem de `classes[].role`.

Perfis: `STUDENT`, `REPRESENTATIVE`, `TEACHER`, `SENAI`, `WEG`, `ADMIN`.

| Perfil | Acesso ao Checklist |
|---|---|
| `REPRESENTATIVE` | Cria/visualiza execuções **da própria turma**. |
| `TEACHER` | Cria/visualiza execuções das turmas vinculadas. |
| `SENAI` | Gerencia templates, janelas e issues; vê dashboards; edita checklists concluídos (escopo SENAI). |
| `WEG` | Paridade com SENAI para validar/reabrir issues, gerenciar templates, dashboards e edição (escopo WEG). |
| `STUDENT` | Sem acesso operacional (sem papel de representante). |
| `ADMIN` | Administração no Hub; **sem acesso operacional** ao Checklist por padrão. |

Perfis sem permissão recebem **`403 Forbidden`** (e não `200`). Ver
[ADR-0006 (autorização local)](docs/adr/0006-autorizacao-local-checklist.md).

---

## Setup local

### Requisitos

- Java 21
- Docker e Docker Compose (o Postgres local sobe automaticamente no perfil padrão)
- Maven Wrapper (`./mvnw`) — já incluso no repositório

### Variáveis de ambiente

Copie `.env.example` para `.env` na raiz e preencha os valores. O arquivo é
carregado antes do Spring Boot; variáveis já definidas no SO/linha de comando têm
prioridade. O `.env` real é ignorado pelo Git.

```env
# Build local (mvn/IDE fora do Docker): acesso ao GitHub Packages (portal-logging)
MAVEN_USERNAME=<seu-usuario-github>
MAVEN_PASSWORD=<PAT classic com escopo read:packages>

SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8083

DB_HOST=localhost
DB_PORT=5433                 # host 5433 -> container 5432, evita conflito com Postgres local
DB_NAME=checklist_db
DB_USER=checklist_user
DB_PASSWORD=checklist_password

# Obrigatório: mesmo segredo Base64 HS256 do Hub local. Nunca versione.
JWT_SECRET=<base64-hs256>
HUB_API_URL=http://localhost:8080

# Mensageria (opcional)
RABBITMQ_ENABLED=true
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

> **`MAVEN_USERNAME`/`MAVEN_PASSWORD`** são lidos por `.mvn/settings.xml` para
> autenticar no GitHub Packages e baixar `com.portal.conecta:portal-logging`. Sem
> eles, `mvn test`/`mvn install` falham na resolução de dependências.
>
> **`JWT_SECRET`** precisa ser idêntico ao do Hub local. Exemplo para gerar um
> segredo Base64 de 32 bytes (PowerShell):
> ```powershell
> [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
> ```
>
> Em ambientes implantados, configure valores sensíveis (`JWT_SECRET`,
> `DB_PASSWORD`, `DB_USER`) como **GitHub Environment Secrets**, nunca versionados.

`DB_USER`/`DB_PASSWORD` do `.env.example` são credenciais exclusivas do container
local e descartável — não valem para ambientes compartilhados ou de produção.

### Primeira execução

1. Instale e abra o Docker Desktop.
2. Copie `.env.example` para `.env` e configure `JWT_SECRET` (e `MAVEN_*` se for
   buildar fora do Docker).
3. Suba o Hub (core-backend) — necessário para autenticação e dados centrais.
4. Rode a API. No perfil padrão (`local`), o Spring Boot sobe o PostgreSQL do
   `docker-compose.yml` e aguarda o health check.

Linux/macOS:
```bash
./mvnw spring-boot:run
```

Windows:
```powershell
.\mvnw.cmd spring-boot:run
```

Com um perfil específico:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Na primeira execução a imagem do PostgreSQL é baixada; as próximas reutilizam o
container e o volume. A aplicação não encerra o banco ao finalizar.

### Infraestrutura local manualmente

Normalmente nenhum comando manual do Docker é necessário. Use apenas para
inspecionar/gerenciar:

```bash
docker compose up -d postgres      # sobe só o Postgres
docker compose ps                  # status dos containers
docker compose down                # para o banco
docker compose down -v             # remove o volume (zera os dados)
docker compose --profile observability up -d   # Grafana local (opcional)
```

---

## Perfis e seed de desenvolvimento

| Perfil | Uso |
|---|---|
| `local` | Padrão (`spring.profiles.default`). Desenvolvimento na máquina, Postgres via Compose. |
| `dev` | Ambiente de desenvolvimento compartilhado; habilita o **seed de dev** e `flyway.baseline-on-migrate` opcional. |
| `prod` | Produção. |
| `test` | Testes automatizados (Flyway habilitado, `ddl-auto: none`, Testcontainers). |

### Seed de dev (`@Profile("dev")`)

`ChecklistDevSeedInitializer` popula, via API HTTP, os dados mínimos para um
checklist ponta a ponta:

- **Templates**: um template `ACTIVE` por sala do Hub. A **sala 214** fica de
  propósito sem template, para testar o estado "sala sem checklist".
- **Janelas de envio** (`ARRIVAL`, 00:00–23:59) para as turmas **`MI78`, `MI77`,
  `MT78`**. A turma **`MT77`** fica de propósito sem janela, para testar o estado
  "sem janela configurada".

Os nomes de turma seguem a convenção do Hub (`códigoDoCurso + número`) e precisam
existir no seed do core-backend (`DevDataInitializer`). O seeder autentica como o
admin de dev (`admin@portal.test`). Falhas (Hub fora do ar, admin ausente) são
logadas como aviso e não impedem a subida do serviço. É idempotente.

---

## Migrations (Flyway)

O schema é versionado por Flyway em `src/main/resources/db/migration`:

| Versão | Conteúdo |
|---|---|
| `V1` | Estrutura inicial do checklist |
| `V2` | `version` em `checklist_issue` |
| `V3` | Índices de estatística |
| `V4` | `submitted_by`/`canceled_by` em `checklist_execution` |
| `V5` | `category` em template e execução |
| `V6` | Remove `item_title_snapshot` de `checklist_issue` |
| `V7` | Índices únicos e *check* de status |
| `V8` | `version` em `checklist_execution` |

Em `dev`, `FLYWAY_BASELINE_ON_MIGRATE` permite baseline sobre bancos já existentes.

---

## Observabilidade

- **Health**: `GET http://localhost:8083/actuator/health` (não exige token).
- **Métricas Prometheus**: `GET http://localhost:8083/actuator/prometheus` (JVM, HTTP, latência).
- **Tracing OTLP**: opcional, via `MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT`.
- **Logs estruturados**: JSON (logstash) com MDC, campos `service`/`environment`.
- **Grafana local**: `docker compose --profile observability up -d`.

Ver [ADR-0018 (observabilidade Prometheus)](docs/adr/0018-observabilidade-prometheus.md).

---

## Documentação da API (Swagger/OpenAPI)

- Swagger UI: `http://localhost:8083/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8083/v3/api-docs`

As operações usam `bearerAuth` (token do Hub) no OpenAPI. Por padrão o Swagger não
é público (`checklist.security.swagger-public: false`).

---

## Testes

```bash
./mvnw test
```

Os testes de repositório usam **Testcontainers** (PostgreSQL real) com Flyway.
Lembre-se de exportar `MAVEN_USERNAME`/`MAVEN_PASSWORD` para resolver a lib
privada `portal-logging`.

Cobertura esperada: regras de envio de checklist, validação de respostas
obrigatórias, cálculo de conformidade, geração de issues, regras de acesso por
perfil e validação de controllers.

---

## Integração com o Hub

Autenticação e dados centrais ficam no Hub. A Checklist API valida o token da
plataforma antes de qualquer ação protegida e, quando precisa de dados do Hub,
chama-o por contratos HTTP (camada de infraestrutura). **Nunca** acessa as
tabelas do Hub diretamente. Ver
[ADR-0007 (integração ports/adapters)](docs/adr/0007-integracao-hub-ports-adapters.md).

### Autenticação por token do Hub

Endpoints protegidos esperam:

```text
Authorization: Bearer <hub-token>
```

O ator é sempre resolvido a partir do token — a API não recebe `userId` em corpo
ou path. Claims esperadas:

```json
{
  "jti": "abc-xyz-789",
  "sub": "11111111-1111-1111-1111-111111111111",
  "userType": "REPRESENTATIVE",
  "classes": [
    { "classId": "22222222-2222-2222-2222-222222222222", "role": "REPRESENTATIVE" }
  ],
  "iat": 1710000000,
  "exp": 1710003600
}
```

A API valida localmente: assinatura HS256 com `JWT_SECRET`, `exp`, claims
obrigatórias (`jti`, `sub`, `userType`, `iat`, `exp`), `sub` e
`classes[].classId` como UUID, e o papel de turma. O contexto do usuário é
resolvido pelo contrato `/me/courses` do Hub. Ver
[ADR-0005 (autenticação token Hub)](docs/adr/0005-autenticacao-token-hub.md).

> O Hub usa nomes diferentes para o papel de turma conforme a superfície: no JWT é
> `classes[].role`; em `/me/courses` é `classes[].classRole`. O gerador de token
> atual não emite `permissionVersion` — se vier no futuro, deve ser tratado como
> nova decisão de integração.

---

## Fluxo ponta a ponta

Suba o **Hub** e depois a **Checklist API** (use identificadores que existem no
Hub). Sequência típica de um checklist:

1. **Autenticar no Hub** e obter o access token (JWT completo `header.payload.signature`).
2. **(SENAI/WEG)** `POST /api/checklist-templates` → criar template da sala e
   `PATCH /api/checklist-templates/{id}/activate` → ativar.
3. **(SENAI/WEG)** `PUT /api/submission-windows/classes/{classId}/ARRIVAL` →
   configurar a janela de envio (`openAt`, `durationMinutes`).
4. **(TEACHER/REPRESENTATIVE)** `POST /api/checklist-executions/drafts` → criar
   rascunho; `PATCH /{id}/answers` → autosave; `POST /{id}/submit` → enviar.
5. Itens `NON_COMPLIANT` geram **issues**, tratáveis em `/api/checklist-issues`.

Regras validadas no envio: só `DRAFT` pode ser enviado; todos os itens
obrigatórios respondidos; `NON_COMPLIANT` exige observação e gera issue; janela de
envio precisa estar aberta; template precisa estar ativo; só o autor envia; não há
checklist duplicado para a mesma turma/sala/período/dia/tipo.

Erros comuns:

- **`401`** — token como JSON cru em vez de JWT; `JWT_SECRET` divergente do Hub;
  `exp` no passado; `sub`/`jti`/`classId` não-UUID.
- **`403`** — token válido mas perfil sem permissão (ex.: `STUDENT` sem papel de
  representante, `ADMIN` tentando operar checklist).
- **`404` / "Sala/Turma não encontrada no Hub"** — `roomId`/`classId` inexistente
  no Hub ou não vinculado ao usuário.

---

## Ownership de dados

A Checklist API é dona **apenas** de dados de checklist: templates, execuções,
respostas, issues, janelas de envio e campos de conformidade. Entidades centrais
(usuários, turmas, cursos, salas, papéis globais) pertencem ao **Hub**.

---

## Diretrizes de desenvolvimento

- Controllers finos; regra de negócio fora de DTOs e controllers.
- Casos de uso na camada de aplicação.
- Domínio independente de Spring, JPA, HTTP e Feign sempre que possível.
- Clientes HTTP externos na infraestrutura (`shared/integration`).
- Anotações OpenAPI para contratos claros; validação nos DTOs de request.
- Erros padronizados pelo handler global (`ApiError` — ver
  [ADR-0008](docs/adr/0008-contrato-de-erro-apierror.md)).
- Evite novas dependências sem necessidade real.

Convenções de commit, branch e PR: ver [CONTRIBUTING.md](CONTRIBUTING.md).

---

## Documentação do projeto

- [Índice de documentação](docs/README.md)
- [Visão geral de arquitetura](docs/arquitetura/visao-geral.md)
- [Fluxo operacional do domínio](docs/dominio/fluxo-operacional.md)
- [Riscos](docs/riscos.md) · [Changelog](docs/CHANGELOG.md)
- [Índice de ADRs](docs/adr/README.md) — decisões arquiteturais 0001–0020
