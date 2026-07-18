# ADR-0004 — Janela de Envio por Shift de Turma

- **Status:** Implementado
- **Data:** 2026-05-28 (revisado em 2026-06-24)
- **Autor:** danielsismer
- **Depende de:** [ADR-0002](0002-redefinicao-tipos-checklist.md), [ADR-0003](0003-modelagem-execucao-post-break.md)
- **Relacionado:** [ADR-0007](0007-integracao-hub-ports-adapters.md), [Fluxo operacional](../dominio/fluxo-operacional.md), [Riscos](../riscos.md)

---

## Contexto

O Checklist precisa limitar **quando** uma turma pode criar/submeter um checklist. O horário real
depende do **turno (`shift`) da turma**, não apenas do `Period`. Por isso a regra usa o campo `shift`
vindo do Hub.

---

## Decisão

A janela de envio é configurada **por turma + tipo de checklist** (`classId + checklistType`) e usa o
`shift` da turma (vindo do Hub) para derivar o período e dar contexto de turno. A turma vem do Hub com
`shift`, e o Checklist usa esse valor para derivar o período e validar a janela configurada.

> **Nota (2026-06-24):** a versão original deste ADR (2026-05) modelava a janela **por shift global**
> (`UNIQUE(shift, checklist_type)`). O código atual configura **por turma** (`UNIQUE(class_id,
> checklist_type)`), guardando `shift` como coluna. Ver a seção *Atualização* ao final.

### Derivação de período (`PeriodResolver`)

`period` **nunca é enviado pelo cliente** — é derivado no backend, em
`module/checklist/domain/service/PeriodResolver`:

| Shift | ChecklistType | Period |
|---|---|---|
| `FULL_AM_PM` | `ARRIVAL` | `MORNING` |
| `FULL_AM_PM` | `POST_BREAK` | `AFTERNOON` |
| `FULL_PM_NT` | `ARRIVAL` | `AFTERNOON` |
| `FULL_PM_NT` | `POST_BREAK` | `NIGHT` |

### Entidade `ChecklistSubmissionWindow`

```text
{ classId UUID, shift, checklistType }  ->  { openAt TIME, durationMinutes INT }
UNIQUE(class_id, checklist_type)   -- constraint uq_window_class_type
```

Campos: `id`, `classId`, `shift`, `checklistType`, `openAt`, `durationMinutes`, `createdAt`, `updatedAt`.

### Comportamento

- **Sem janela cadastrada** → sem restrição (permite por padrão).
- **Com janela** → valida `LocalTime.now(America/Sao_Paulo)` ∈ `[open_at, open_at + duration_minutes]`.
- Janela que **cruza meia-noite** → rejeitada no upsert.
- Validação na **criação de DRAFT** e **revalidação no submit**.
- Timezone explícito via `checklist.timezone=America/Sao_Paulo` — nunca `LocalDateTime.now()` sem zona.

### Snapshot de `shift` na execução

O `shift` da turma é gravado na criação do DRAFT (`ChecklistExecution.shift`), evitando nova chamada
ao Hub no submit.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Configurar janela dentro do template | Acopla janela ao conteúdo; janela é regra de turno |
| Cliente enviar `period` | Permite forjar duplicatas; `period` passou a ser derivado |
| Buscar `shift` de novo a cada leitura | Custo desnecessário; snapshot resolve |

---

## Consequências

### Positivas

- Janela coerente com o turno real da turma.
- `period` consistente e não-forjável.

### Pontos de atenção

- Depende de o Hub retornar `shift` em `GET /classes/{classId}` (ver [ADR-0007](0007-integracao-hub-ports-adapters.md)).
- Mensagem ao usuário fora da janela pode ser refinada.

---

## Atualização 2026-06-24

**1. Janela passou a ser por turma, não por shift global.** O modelo original
(`UNIQUE(shift, checklist_type)`) foi refinado para `UNIQUE(class_id, checklist_type)`
(constraint `uq_window_class_type`), permitindo configurar a janela de cada turma individualmente.
O `shift` continua armazenado e é usado na derivação de `period`. Endpoint atual:
`PUT /api/submission-windows/classes/{classId}/{checklistType}`.

**2. Drift doc↔código no status HTTP.** A violação de janela (`SubmissionWindowViolationException`) é
hoje mapeada para **`400 Bad Request`** no `GlobalHandlerException`, e **não** `422` como descrito em
notas antigas. O contrato de erro vigente está em [ADR-0008](0008-contrato-de-erro-apierror.md).
Decisão a confirmar: manter `400` ou voltar a `422` para "regra de negócio violada".

---

## Referências

- `module/checklist/domain/service/PeriodResolver`
- `module/checklist/application/service/window/SubmissionWindowValidator`
- `module/checklist/domain/model/ChecklistSubmissionWindow`
