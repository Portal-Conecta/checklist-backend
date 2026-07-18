# ADR-0003 — Modelagem da Execução POST_BREAK

- **Status:** Implementado
- **Data:** 2026-05-28 (revisado em 2026-06-24)
- **Autor:** danielsismer
- **Depende de:** [ADR-0002](0002-redefinicao-tipos-checklist.md)
- **Relacionado:** [Fluxo operacional](../dominio/fluxo-operacional.md), [Riscos](../riscos.md)

---

## Contexto

Com a substituição de `DEPARTURE` por `POST_BREAK` ([ADR-0002](0002-redefinicao-tipos-checklist.md)),
o Checklist precisa representar dois momentos independentes — chegada inicial e retorno do intervalo.
O retorno pode acontecer em **outra sala**, então o modelo não deve forçar relação obrigatória entre
`ARRIVAL` e `POST_BREAK`.

---

## Decisão

`POST_BREAK` é modelado como uma **execução normal** de checklist, diferenciada apenas pelo campo
`checklistType`. O backend **não** cria entidade separada, grupo de execução ou dependência
obrigatória entre chegada e pós-intervalo.

### Modelo conceitual

Uma execução é identificada pelo conjunto:

```text
classId + roomId + period + checklistType + dia
```

Isso permite `ARRIVAL` em uma sala e `POST_BREAK` em outra, ambos no mesmo dia, para a mesma turma,
sem conflito indevido.

### Template

O template continua vinculado à sala. Não existe template diferente por tipo de checklist no escopo
atual. Se o produto pedir perguntas diferentes para `ARRIVAL` e `POST_BREAK`, isso vira nova decisão.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| `POST_BREAK` depender de `ARRIVAL` | PO confirmou independência |
| Criar grupo de execução | Complexidade sem ganho atual |
| Template por tipo de checklist | Fora do escopo atual |
| Reutilizar `DEPARTURE` | Nome não representa mais a regra real |

---

## Consequências

### Positivas

- Modelo simples; uma só entidade `ChecklistExecution` cobre os dois tipos.
- Suporta troca de sala no intervalo sem regra especial.

### Pontos de atenção

- A unicidade depende de derivar `period` corretamente (ver [ADR-0004](0004-janela-de-envio-por-shift.md)).
- Proteção contra duplicidade concorrente é tema de risco aberto (ver [Riscos](../riscos.md)).

---

## Referências

- `modules/checklist/domain/model/ChecklistExecution`
- [Fluxo operacional](../dominio/fluxo-operacional.md)
