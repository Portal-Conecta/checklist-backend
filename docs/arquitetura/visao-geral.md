# Visão de Arquitetura — Checklist Backend

> **Documento vivo.** Reflete o estado de `develop` em 2026-07-18. Atualize ao mudar a estrutura.
> A *decisão* por trás desta organização está em [ADR-0001](../adr/0001-arquitetura-modular.md).
> A separação entre `checklist` e `issues` como módulos pares está na [ADR-0020](../adr/0020-issues-como-modulo-de-negocio-independente.md).

---

## Em uma frase

Monólito **modular** em Spring Boot, organizado por **módulos de negócio**, cada um com camadas
`domain / application / infrastructure / presentation`, seguindo **ports & adapters** e um
**CQRS leve** (comandos e consultas separados na aplicação).

---

## Regra de dependência

```text
presentation  ─┐
                ├─►  application  ──►  domain
infrastructure ─┘        ▲
                         └── implementa as portas (port/out) definidas na aplicação
```

- **domain** não depende de nada externo (sem Spring/JPA/HTTP).
- **application** depende só de domain; define **portas** (interfaces) para o mundo externo.
- **infrastructure** implementa as portas (adaptadores de banco, fila, etc.).
- **presentation** traduz HTTP ↔ aplicação.

---

## Mapa de pacotes (`com.portal.conecta.checklist`)

```text
module/
  checklist/                         MÓDULO CHECKLIST
    domain/
      enums/         ChecklistType(ARRIVAL,POST_BREAK) · Period(MORNING,AFTERNOON,NIGHT)
                     Shift(FULL_AM_PM,FULL_PM_NT) · ChecklistExecutionStatus(DRAFT,SUBMITTED,CANCELED)
                     ChecklistTemplateStatus(DRAFT,ACTIVE,INACTIVE) · ConformityAnswerValue(COMPLIANT,NON_COMPLIANT)
      exception/     SubmissionWindowViolationException
      model/         ChecklistExecution · ChecklistTemplate · ChecklistSubmissionWindow
      schema/        modelo do schema (ChecklistItem, seções, itens)
      service/       PeriodResolver (serviço de domínio puro)
      valueobject/   ClassReference · RoomReference · CourseReference
    application/
      port/out/
        persistence/   portas de persistência (ex.: ChecklistItemSearchPort)
        integration/   portas para dados externos (Hub)
        messaging/     NotificationEventPublisher
        issue/         portas para o módulo issues: IssueCreationPort · IssueStatsPort (ver ADR-0020)
      service/
        execution/     AnswerValidation · Scoring · Issue · DataMapper (submit decomposto)
        window/        SubmissionWindowValidator
      usecase/
        execution/{command,query}    Create/Submit/Cancel/UpdateAnswers · History
        template/{command,query}     Create/Activate/Edit/NewVersion · Find/List/SearchItem
        window/{command,query}       Upsert · List
    infrastructure/
      persistence/   repositórios JPA + adaptadores (ex.: ChecklistItemQueryRepository)
      messaging/     RabbitMQNotificationPublisher (adapter de NotificationEventPublisher)
      adapter/issues/ ExecutionAccessAdapter (implementa a porta de issues, ver ADR-0020)
    presentation/
      controller/    ChecklistExecutionController · ChecklistTemplateController · SubmissionWindowController
      dto/{execution,template,window}/{request,response}
      mapper/
      port/          ExecutionIssuesQueryPort (porta para issues, vive aqui e não em application — ver ADR-0020)
  issues/                             MÓDULO ISSUES (par de checklist, ver ADR-0020)
    domain/        enums(IssuePriority[LOW,MEDIUM,HIGH,CRITICAL], IssueStatus[OPEN,IN_PROGRESS,RESOLVED,VALIDATED,REOPENED,CANCELED])
                   model(ChecklistIssue) · valueobject(UserReference)
    application/   port/out/persistence · port/out/execution(ExecutionAccessPort) · usecase/{command,query}
    infrastructure/persistence · adapter/checklist/ (IssueCreationAdapter · IssueStatsAdapter)
    presentation/  controller · dto/response · mapper · adapter/checklist/ (ExecutionIssuesQueryAdapter)
  notification/                      MÓDULO RESERVADO (pacotes criados, sem implementação ainda)

shared/
  config/        configuração transversal (Jackson, env)
  context/       RequestContext + provider/ (Spring e Mock)
  exception/     ApiError · GlobalHandlerException (ver ADR-0008)
  stats/         StatsEntryDTO (formato genérico {label,value}, usado por checklist e issues)
  integration/
    hub/
      client/{classes,course,me,room}/    @FeignClient + DTOs de resposta
      adapter/{classes,course,me,room}/    HttpHub{Resource}Provider (adaptadores)
      config/                              configuração Feign
      exception/                           HubIntegrationException (-> 503)
  messaging/
    config/        RabbitMQConfig · RabbitMQProperties · TestRabbitMQConfig
    event/         NotificationEvent
  security/        config · error(SecurityErrorResponseWriter) · filter · token
  utils/
```

---

## Módulos

| Módulo | Estado | Responsabilidade |
|---|---|---|
| `module/checklist` | Ativo | Templates, execuções, janela de envio, schema |
| `module/issues` | Ativo | Issues geradas por não conformidades — módulo par de `checklist`, comunicação por portas (ver ADR-0020) |
| `module/notification` | **Reservado** | Pacotes criados; ainda sem implementação |

---

## Integrações externas

- **Hub (Portal Conecta Core)** via OpenFeign — ver [ADR-0007](../adr/0007-integracao-hub-ports-adapters.md).
- **RabbitMQ** para eventos de notificação — porta `NotificationEventPublisher`,
  adaptador `RabbitMQNotificationPublisher`, evento `NotificationEvent`.
  *(Decisão ainda a formalizar — ver ADR-0009 planejado no [índice de ADRs](../adr/README.md).)*

---

## Convenções de nomenclatura

- **Porta de saída:** interface em `application/port/out/...`, sufixo `Port` ou `Publisher`/`Provider`.
- **Adaptador:** implementação em `infrastructure/...` (ou `shared/integration/hub/adapter/...`).
- **Caso de uso:** `application/usecase/<recurso>/<command|query>/<Verbo>...UseCase`.
- **Comando** muda estado; **consulta** apenas lê.
- **Serviço de domínio:** lógica pura sem IO, em `domain/service`.
- **Serviço de aplicação:** orquestração reutilizável, em `application/service`.
