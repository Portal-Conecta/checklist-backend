# ADR-0002 — Redefinição dos Tipos de Checklist

- **Status:** Implementado
- **Data:** 2026-05-28 (revisado em 2026-06-24)
- **Autor:** danielsismer
- **Relacionado:** [ADR-0003](0003-modelagem-execucao-post-break.md), [ADR-0004](0004-janela-de-envio-por-shift.md), [Fluxo operacional](../dominio/fluxo-operacional.md)

---

## Contexto

A regra inicial previa checklist de chegada e de saída (`DEPARTURE`). O PO redefiniu o processo
para refletir melhor o uso real das salas: o segundo checklist acontece no **retorno do intervalo**,
não na saída. Isso importa porque uma turma pode **trocar de sala durante o intervalo**.

---

## Decisão

O enum de tipo de checklist usa apenas:

```text
ARRIVAL
POST_BREAK
```

`DEPARTURE` foi **removido** do fluxo atual.

### Semântica

| Tipo | Momento | Sala |
|---|---|---|
| `ARRIVAL` | Chegada inicial na sala | Sala de chegada |
| `POST_BREAK` | Retorno do intervalo | Sala atual no retorno |

### Regras

- `POST_BREAK` **não depende** de existir `ARRIVAL` no mesmo dia.
- `ARRIVAL` e `POST_BREAK` são execuções independentes.
- O template usado é o template **ativo** da sala informada na execução.
- A unicidade considera `classId + roomId + period + checklistType + dia`.
- Execuções `CANCELED` não bloqueiam nova execução equivalente.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Manter `DEPARTURE` | Nome não representa mais a regra real (retorno ≠ saída) |
| `POST_BREAK` depender de `ARRIVAL` | PO confirmou independência entre os momentos |

---

## Consequências

### Positivas

- O modelo reflete melhor a operação real.
- Troca de sala no intervalo fica suportada.
- O backend não precisa inferir relação obrigatória entre chegada e pós-intervalo.

### Pontos de atenção

- Frontend não deve enviar `DEPARTURE`.
- Dados legados com `DEPARTURE`, se existirem em ambiente antigo, precisam de tratamento.

---

## Referências

- `module/checklist/domain/enums/ChecklistType`
- [Fluxo operacional](../dominio/fluxo-operacional.md)
