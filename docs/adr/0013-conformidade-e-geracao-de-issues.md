# ADR-0013 — Cálculo de Conformidade e Geração de Pendências

- **Status:** Implementado
- **Data:** 2026-07-05
- **Autor:** danielsismer
- **Depende de:** [ADR-0012](0012-versionamento-imutabilidade-template.md)
- **Relacionado:** [ADR-0002](0002-redefinicao-tipos-checklist.md), [Fluxo operacional](../dominio/fluxo-operacional.md)

---

## Contexto

O valor do checklist não é só registrar respostas — é **medir conformidade** e **gerar ação** sobre o
que está fora do padrão. Ao submeter (ou editar) uma execução, o sistema precisa produzir de forma
determinística: (1) uma nota de conformidade e (2) pendências rastreáveis para os itens não conformes.

Esse comportamento é compartilhado entre o fluxo de **submissão** (`SubmitChecklistExecutionUseCase`)
e o de **edição de checklist submetido** (`UpdateChecklistExecutionAnswersUseCase`), então precisa
viver em serviços reutilizáveis, não duplicado nos use cases.

---

## Decisão

### Nota de conformidade (`compliance_score`)

Calculada em `ChecklistExecutionScoringService`:

```text
score = (itens COMPLIANT / itens respondidos) * 100
```

- Considera apenas respostas com `value != null` como "respondidas".
- `COMPLIANT` conta como conforme; `NON_COMPLIANT` não.
- Resultado em `BigDecimal`, **2 casas decimais**, `RoundingMode.HALF_UP` (`0.00`–`100.00`).
- Sem itens respondidos → `0.00`.
- Persistido em `checklist_execution.compliance_score` (`DECIMAL(5,2)`).

### Geração de pendências (issues)

Em `ChecklistIssueService.createIssuesForNonCompliantAnswers`:

- Cada resposta `NON_COMPLIANT` gera uma `ChecklistIssue` atrelada à execução.
- **Deduplicação por `itemKey`**: não cria issue para item que já tem pendência naquela execução
  (idempotente entre submit e edições).
- Valores no momento da criação:
  - `status = OPEN`, `priority = MEDIUM`;
  - `dueAt = agora + 7 dias`;
  - `assignedUserReference = usuário da execução`;
  - `title` grava o título do item **no momento da submissão** (ver
    [ADR-0012](0012-versionamento-imutabilidade-template.md)), com prefixo `"Pendencia: "`;
  - textos truncados aos limites das colunas (`title` 100, `description` 500).
- A `description` vem da **observação** da resposta — coerente com a regra de que `NON_COMPLIANT`
  exige observação (validada na submissão).

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Score ponderado por peso de item | Sem requisito de pesos no MVP; proporção simples é suficiente |
| Contar não respondidos como não conformes | Distorce a nota; itens obrigatórios já são barrados na validação |
| Prioridade/prazo por severidade do item | Template não define severidade hoje; padrão `MEDIUM`/7 dias |
| Recriar issues a cada edição | Perderia estado (ex.: já `RESOLVED`); dedupe por `itemKey` preserva |

---

## Consequências

### Positivas

- Cálculo determinístico e reutilizado entre submit e edição.
- Não conformidade sempre gera ação rastreável, sem duplicar pendências.
- Snapshot do título em `title` mantém a issue legível após evolução do template.

### Pontos de atenção / negativas

- Prioridade e prazo são fixos (`MEDIUM`, 7 dias) — se o negócio exigir severidade, vira novo ADR.
- Dedupe é por `itemKey` **dentro da execução**; não considera reabertura entre execuções distintas.
- Score ignora itens não respondidos; a completude é garantida antes, na validação de obrigatórios.

---

## Referências

- `module/checklist/application/service/execution/ChecklistExecutionScoringService`
- `module/checklist/application/service/execution/ChecklistIssueService`
- `module/checklist/application/usecase/execution/command/submit/SubmitChecklistExecutionUseCase`
- `module/checklist/application/usecase/execution/command/update/UpdateChecklistExecutionAnswersUseCase`
- `module/issues/domain/model/ChecklistIssue`
