# ADR-0015 — Consulta em Lote (Bulk) ao Hub para Enriquecimento de Listagens

- **Status:** Aceito
- **Data:** 2026-07-11
- **Autor:** danielsismer
- **Depende de:** [ADR-0007](0007-integracao-hub-ports-adapters.md)
- **Relacionado:** [ADR-0011](0011-persistencia-jsonb-schema-respostas.md)

---

## Contexto

Várias respostas do checklist precisam de dados que vivem no Hub (nome da turma, da sala), que o
checklist só guarda como `UUID`. Ao renderizar uma **lista** (histórico de execuções, lista de
templates), enriquecer item a item chamando `findById` por linha gera o problema clássico **N+1**:
uma listagem de 50 itens dispara ~50 chamadas HTTP ao Hub — lento e frágil.

---

## Decisão

Adicionar métodos **bulk** às portas de integração (`HubClassProvider.findByIds`,
`HubRoomProvider.findByIds`) e usá-los no enriquecimento de listagens: coletar todos os IDs
distintos da página, fazer **uma** chamada em lote ao Hub, montar um mapa `id → referência` e
resolver cada item em memória (O(1)).

- Itens únicos (ex.: um template) continuam usando `findById` — uma chamada para um item é correto.
- O adaptador deduplica (`distinct`), ignora nulos e envolve `FeignException` em
  `HubIntegrationException` (ADR-0007).

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| `findById` em loop (N+1) | Latência e carga no Hub crescem linearmente com a listagem. |
| Cache local das referências do Hub | Resolve repetição entre requests, mas não o N+1 dentro de uma listagem; adiciona invalidção. |
| Desnormalizar nome da turma/sala no checklist | Duplica dado do Hub e cria problema de sincronização; fere a fronteira de contexto. |

---

## Consequências

### Positivas

- Uma listagem = **uma** chamada de enriquecimento, independentemente do tamanho.
- Menos carga no Hub e resposta mais previsível.

### Pontos de atenção / negativas

- Se a chamada bulk falhar, o enriquecimento **falha o request inteiro** (sem degradação graciosa).
  Decisão consciente: "falhar explícito > lista com dados incompletos". Resiliência (devolver a
  lista sem enriquecimento) fica como evolução futura.
- Exige que o Hub exponha um endpoint bulk correspondente (contrato).

---

## Referências

- `shared/integration/hub/adapter/classes/HttpHubClassProvider.java` (`findByIds`)
- `shared/integration/hub/adapter/room/HttpHubRoomProvider.java` (`findByIds`)
- Mappers `ChecklistExecutionMapper` / `ChecklistTemplateMapper` (enriquecimento em lote)
- PR #181 (`feature/#152-consulta-bulk`)
