# ADR-0017 — Dashboard Composto, Cache e Atualização por Polling

- **Status:** Aceito
- **Data:** 2026-07-11
- **Autor:** danielsismer
- **Depende de:** [ADR-0016](0016-endpoints-agregacao-stats.md)
- **Relacionado:** [ADR-0001](0001-arquitetura-modular.md)

---

## Contexto

O painel de checklist é um **conjunto fixo** de gráficos (definido pelo time, igual para todos). Se o
front chamar um endpoint de agregação (ADR-0016) por gráfico, monta-se uma tela com **N requisições**
(N estados de loading; e cada refresh recalcula N agregações no banco). Além disso, é desejável que o
painel "se atualize" sem F5.

---

## Decisão

**1. Endpoint composto (BFF/agregador).** Um único endpoint
`GET /api/checklist-stats/dashboard?from=&to=` que **orquestra** os use cases de stats da ADR-0016
(sem reimplementar query) e devolve todos os gráficos numa resposta só.

**2. Cache curto no servidor.** O resultado é `@Cacheable` (Caffeine, TTL ~60s, chave por período).
Multiplos acessos/pollings dentro da janela custam **uma** rodada de agregações. É o cache que faz a
solução escalar — sem ele, o composto só trocaria "N queries em N requests" por "N queries em 1
request".

**3. Atualização por polling (sem websocket).** O frescor é obtido com **short polling** no frontend
(intervalo de 30–60s) ou botão "atualizar" — não há websocket. Stats de checklist mudam devagar;
conexão stateful não se justifica.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Front dispara os granulares em paralelo | Válido (HTTP/2), mas são N estados de loading e sem cache cada refresh martela o banco. |
| Websocket (servidor empurra) | Complexidade alta (conexão stateful, reconexão, auth no socket, escala) para dado que muda devagar. |
| Composto **sem** cache | Não alivia o banco; só reduz o número de round-trips. |

---

## Consequências

### Positivas

- 1 requisição por tela; 1 estado de loading; carga no banco limitada pela janela de cache.
- Reaproveita 100% das agregações da ADR-0016.

### Pontos de atenção / negativas

- Dado pode ficar até o TTL (~60s) defasado — aceitável para o caso.
- Se o painel virar **configurável pelo usuário** (cada um escolhe seus gráficos), o composto perde
  sentido e volta-se ao granular+paralelo. Decisão vale enquanto o dashboard for **fixo**.
- **Autorização:** **restrito à gestão (SENAI/WEG)** — o use case composto exige o predicado de
  gestão (`canManageIssues`), coerente com a ADR-0016 e com o resto do módulo (ADR-0006).

---

## Referências

- `application/usecase/stats/ChecklistDashboardUseCase.java` (orquestração + `@Cacheable`)
- `shared/config/CacheConfig.java` (Caffeine, TTL)
- `presentation/controller/ChecklistDashboardController.java`
- PR #215 (`feature/#215-implementa-dashboard-composto`)
