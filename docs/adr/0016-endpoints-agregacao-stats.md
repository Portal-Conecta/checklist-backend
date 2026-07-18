# ADR-0016 — Endpoints de Agregação (Stats) para Dashboards

- **Status:** Implementado
- **Data:** 2026-07-11
- **Autor:** danielsismer
- **Depende de:** [ADR-0001](0001-arquitetura-modular.md)
- **Relacionado:** [ADR-0017](0017-dashboard-composto-cache.md), [ADR-0013](0013-conformidade-e-geracao-de-issues.md)

---

## Contexto

O produto precisa de painéis (Chart.js) com números agregados do checklist: execuções por dia/status,
não-conformidades por status/prioridade, taxa de conclusão, etc. Calcular isso na aplicação (carregar
entidades e somar em memória) é caro e não escala. A agregação é trabalho de banco.

---

## Decisão

Expor **endpoints de agregação** seguindo a arquitetura de portas/adaptadores (ADR-0001), com um
método de query dedicado por métrica e um formato de saída genérico
`StatsEntryDTO { label, value }` — consumível direto pelo Chart.js.

- Agregações são **queries nativas parametrizadas** (`GROUP BY status`, `CAST(started_at AS date)`,
  etc.) — **sem concatenar** o critério de agrupamento na SQL (evita SQL injection).
- Intervalo de datas por `from`/`to`; validação de período centralizada
  (`InvalidRequestException` → 400).
- **Índices** de suporte às agregações (status, tipo, turno, `started_at::date`, `due_at`, …)
  entregues via **migration Flyway**, para existirem em produção — e não só no `schema-postgresql.sql`
  do profile dev.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Agregar em memória na aplicação | Carrega dados demais; não escala com o volume. |
| Um endpoint único com `groupBy` dinâmico concatenado na SQL | Risco de SQL injection e query difícil de indexar. |
| View materializada | Ganho real só com volume alto; adiciona refresh/manutenção — postergado. |

---

## Consequências

### Positivas

- Números vêm prontos do banco, indexados, no formato do Chart.js.
- Queries por método → seguras (parametrizadas) e fáceis de otimizar/indexar.

### Pontos de atenção / negativas

- Muitos gráficos numa tela geram muitas requisições — resolvido pelo endpoint composto (ADR-0017).
- **Autorização:** acesso **restrito à gestão (SENAI/WEG)** — números agregados de compliance não são
  para qualquer usuário. Os endpoints granulares desta ADR precisam receber essa checagem (predicado
  `canManageIssues`), alinhando ao resto do módulo (ADR-0006). Ver ADR-0017.
- Índices precisam estar na migration Flyway, não só no schema de dev (armadilha "funciona no dev,
  lento na prod").

---

## Referências

- `modules/checklist/**/infrastructure/persistence/*StatsRepository.java` (queries nativas)
- `application/dto/stats/StatsEntryDTO.java`
- Migration `V3__add_stats_indexes.sql`
- PR #212 (`feature/#186-endpoints-de-agregacao`)
