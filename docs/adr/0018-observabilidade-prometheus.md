# ADR-0018 — Observabilidade: Exposição de Métricas Prometheus

- **Status:** Implementado
- **Data:** 2026-07-11
- **Autor:** danielsismer
- **Relacionado:** [ADR-0005](0005-autenticacao-token-hub.md), [ADR-0001](0001-arquitetura-modular.md)

---

## Contexto

A stack do Portal Conecta tem observabilidade central (Grafana + Prometheus + Alloy). Para o
checklist entrar nesse fluxo, precisa **expor métricas** num endpoint que o coletor (Alloy) consegue
raspar. O serviço é protegido por JWT em tudo (ADR-0005), o que conflita com um scraper que não
carrega token de usuário.

---

## Decisão

Expor métricas via **Micrometer + Prometheus registry** em `GET /actuator/prometheus`, e liberar esse
endpoint (junto de `/actuator/health/**` e `/actuator/info`) **sem autenticação**, seguindo o mesmo
contrato dos demais serviços do portal (hub/core, gateway). O isolamento é de **rede** (endpoint
exposto só na rede interna do compose), não por senha na aplicação.

- As demais rotas continuam exigindo JWT.
- A liberação inclui explicitamente os sub-paths de probe (`/actuator/health/**`), senão o healthcheck
  do docker-compose (`/actuator/health/readiness`) cai no filtro de segurança (401) e o container
  nunca fica `healthy`.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Basic Auth no `/actuator/prometheus` | O Alloy não envia credenciais e os outros serviços expõem sem auth; quebraria a coleta e divergiria do padrão. |
| Não expor métricas | Sem visibilidade operacional; fora do contrato de observabilidade do portal. |
| Push de métricas (pushgateway) | O padrão do portal é scrape (pull) via Alloy. |

---

## Consequências

### Positivas

- Checklist integrado à observabilidade do portal (mesmo contrato dos demais serviços).
- Healthcheck do compose funciona (readiness/liveness liberados).

### Pontos de atenção / negativas

- `/actuator/prometheus` **não pode** vazar para fora da rede interna — a segurança aqui é de rede.
- Para o dado realmente fluir, o scrape do checklist precisa estar **habilitado no Alloy**
  (`config.alloy`) e o target (`CHECKLIST_METRICS_TARGET`) definido no compose — responsabilidade do
  repositório de observability, não deste.

---

## Referências

- `shared/security/config/SecurityConfig.java` (paths públicos de actuator)
- `pom.xml` (`micrometer-registry-prometheus`)
- `application.yml` (`management.endpoints.web.exposure.include`)
- PRs #185 / #201 (`feature/#185-Expondo-metricas-prometheus`)
