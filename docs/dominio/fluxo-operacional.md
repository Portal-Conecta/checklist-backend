# Fluxo Operacional do Checklist

> **Documento vivo.** Estado de `develop` em 2026-06-24.
> Decisões: [ADR-0002](../adr/0002-redefinicao-tipos-checklist.md), [ADR-0004](../adr/0004-janela-de-envio-por-shift.md), [ADR-0006](../adr/0006-autorizacao-local-checklist.md).

---

## Ciclo de vida

```text
Template DRAFT ─► Template ACTIVE
                    └─► Execution DRAFT ─► Execution SUBMITTED ─► Issues (OPEN … RESOLVED)
                                       └─► Execution CANCELED
```

| Recurso | Estados (enum verificado) |
|---|---|
| Template (`ChecklistTemplateStatus`) | `DRAFT`, `ACTIVE`, `INACTIVE` |
| Execution (`ChecklistExecutionStatus`) | `DRAFT`, `SUBMITTED`, `CANCELED` |
| Issue (`IssueStatus`) | `OPEN`, `IN_PROGRESS`, `RESOLVED`, `VALIDATED`, `REOPENED`, `CANCELED` |
| Issue prioridade (`IssuePriority`) | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| Resposta (`ConformityAnswerValue`) | `COMPLIANT`, `NON_COMPLIANT` |

---

## Templates

Gestão restrita a `SENAI` / `WEG` ([ADR-0006](../adr/0006-autorizacao-local-checklist.md)).

- Template nasce como `DRAFT`; é validado (seções/itens, chaves estáveis e únicas) e vinculado à sala.
- Template `ACTIVE` **não é editado diretamente** — cria-se **nova versão** em `DRAFT` para alterar
  conteúdo. Versões antigas preservam histórico.

---

## Criação de draft de execução

`POST /api/checklist-executions/drafts` — `REPRESENTATIVE`/`TEACHER` da turma.

Validações:

1. Template existe, está `ACTIVE` e pertence à `roomId`.
2. Sala existe no Hub.
3. Turma existe no Hub e retorna `shift`.
4. Curso existe se a turma trouxer `courseId`.
5. Usuário pode operar checklist naquela turma.
6. Janela de envio permite a criação.
7. Não existe execução duplicada (`classId + roomId + period + checklistType + dia`, exceto `CANCELED`).

Derivações server-side: `period` (via `PeriodResolver`), `filledBy` (usuário autenticado),
`shift` (turma no Hub — gravado como snapshot).

---

## Submit

`POST /api/checklist-executions/{executionId}/submit` — criador vinculado à turma.

Validações (serviços em `application/service/execution`):

1. Execução existe e está em `DRAFT`.
2. Usuário pode operar a turma da execução.
3. Janela de envio ainda permite submissão (revalidação).
4. Todos os itens obrigatórios respondidos.
5. Sem `itemKey` duplicado; sem resposta para item fora do schema.
6. `COMPLIANT` não exige observação; `NON_COMPLIANT` segue regra de observação/issue.

Efeitos: status → `SUBMITTED`; `complianceScore` calculado; issues criadas para não conformidades.

---

## Cancelamento e edição de submetido

- `PATCH /api/checklist-executions/{executionId}/cancel` — criador ou `SENAI`/`WEG`.
  `CANCELED` é final e não conta para unicidade.
- `PATCH /api/checklist-executions/{executionId}/answers` — edição de respostas de checklist
  submetido; reutiliza a validação do submit; score e issues recalculados.
- `GET /api/checklist-executions/history/class/{classId}` — histórico por turma.

---

## Janela de envio

Configurada **por turma + tipo** (`classId + checklistType`), guardando `shift`
([ADR-0004](../adr/0004-janela-de-envio-por-shift.md)).

- `GET /api/submission-windows` · `GET /api/submission-windows/classes/{classId}`
- `PUT /api/submission-windows/classes/{classId}/{checklistType}` — `SENAI`/`WEG`.
- Sem janela → permite. Com janela → valida `LocalTime.now(America/Sao_Paulo)` no intervalo.
  Janela não pode cruzar meia-noite.

---

## Busca de itens (#147)

`GET /api/checklist-templates/items/search?query=<termo>` — perfis com `canAccessChecklistModule()`.

- Caso de uso `SearchChecklistItemUseCase` (consulta) → porta `ChecklistItemSearchPort`.
- Adaptador `ChecklistItemQueryRepository`: carrega templates `ACTIVE`, percorre o schema JSONB e
  filtra itens cujo `title`/`description` contêm o termo (case-insensitive). Termo vazio → lista vazia.
- ⚠️ **Hoje a varredura é em memória, não em SQL/JSONB** — ver RISK-PERF-001 em [Riscos](../riscos.md).
- ⚠️ Falta de permissão lança `IllegalArgumentException` (→ HTTP 400 em vez de 403) — RISK-AUTH-004.

---

## Issues

`GET /api/checklist-issues/execution/{executionId}` · `PATCH /api/checklist-issues/{issueId}/resolve` (`SENAI`/`WEG`).

Issues nascem no submit para cada item `NON_COMPLIANT`, vinculadas à execução.

---

## Integrações externas usadas no fluxo

| Dado | Origem | Referência |
|---|---|---|
| Usuário autenticado / token | Hub | [ADR-0005](../adr/0005-autenticacao-token-hub.md) |
| Sala, Turma/shift, Curso | Hub | [ADR-0007](../adr/0007-integracao-hub-ports-adapters.md) |
| Eventos de notificação | RabbitMQ | `NotificationEventPublisher` |
