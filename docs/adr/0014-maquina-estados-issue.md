# ADR-0014 — Máquina de Estados da Pendência (ChecklistIssue) com Optimistic Locking

- **Status:** Implementado
- **Data:** 2026-07-11
- **Autor:** danielsismer
- **Depende de:** [ADR-0013](0013-conformidade-e-geracao-de-issues.md)
- **Relacionado:** [ADR-0006](0006-autorizacao-local-checklist.md), [ADR-0008](0008-contrato-de-erro-apierror.md)

---

## Contexto

As pendências (`ChecklistIssue`) geradas por não-conformidade (ADR-0013) precisam de um ciclo de vida
explícito: alguém assume o atendimento, resolve, o SENAI valida ou reprova, e há como cancelar. Sem
regras claras de transição, o `status` viraria um campo livre, sujeito a estados inválidos
(ex.: "validar" uma pendência ainda aberta) e a corrida entre dois atendentes editando a mesma
pendência ao mesmo tempo.

---

## Decisão

Modelar as transições como uma **máquina de estados encapsulada no domínio** (`ChecklistIssue`), com
um método por transição que **valida o estado atual** antes de mudar, lançando
`InvalidIssueTransitionException` (→ HTTP 422) quando a transição é inválida:

- `OPEN → IN_PROGRESS` (`startProgress`)
- `IN_PROGRESS → RESOLVED` (`resolve`)
- `RESOLVED → VALIDATED` (`validate`)
- `RESOLVED → REOPENED` (`reopen`)
- `REOPENED → IN_PROGRESS` (`restartProgress`)
- `OPEN | IN_PROGRESS → CANCELED` (`cancel`)

Concorrência é tratada com **optimistic locking** via `@Version` (coluna `version` em
`checklist_issue`, adicionada por migration Flyway). Autorização segue o ADR-0006: `start`,
`restart`, `resolve` e `cancel` exigem `canManageIssues` (SENAI/WEG); `validate` e `reopen` exigem
`canOnlySenaiManageIssues` (só SENAI).

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| `status` como setter livre, validado no service | Regra espalhada e fácil de furar; o domínio não garante os invariantes. |
| Biblioteca de state machine (Spring StateMachine) | Peso desproporcional para 6 transições; encapsular no agregado é mais simples e testável. |
| Pessimistic lock (SELECT FOR UPDATE) | Trava linhas desnecessariamente; optimistic cobre o caso (conflito raro) sem contenção. |

---

## Consequências

### Positivas

- Transições inválidas são impossíveis de persistir — o domínio é a fonte de verdade.
- `@Version` protege contra dois atendentes sobrescrevendo a mesma pendência.
- Erros de transição têm resposta padronizada (422, ADR-0008).

### Pontos de atenção / negativas

- A coluna `version` **precisa existir no schema real** (Flyway), não só no `schema-postgresql.sql`
  de dev — caso contrário toda transição falha em produção. Corrigido por migration dedicada.
- Cliente que receber `409/version conflict` (optimistic) deve reconsultar e reenviar.

---

## Referências

- `module/issues/domain/model/ChecklistIssue.java` (transições + `@Version`)
- `module/issues/domain/exception/InvalidIssueTransitionException.java`
- Migration `V2__add_version_to_checklist_issue.sql`
- PR #208 (`feature/#151-maquina-estados-completa-checklistIssue`)
