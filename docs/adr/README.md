# Architecture Decision Records (ADRs)

Registro das decisões de arquitetura e de regras de negócio críticas do Checklist Backend.
Cada ADR captura **uma decisão**, seu **contexto**, as **alternativas** e as **consequências**.

---

## Índice

| # | Decisão | Status | Atualizado |
|---|---|---|---|
| [0001](0001-arquitetura-modular.md) | Arquitetura modular em camadas (ports & adapters + CQRS leve) | Implementado | 2026-06-24 |
| [0002](0002-redefinicao-tipos-checklist.md) | Redefinição dos tipos de checklist (`ARRIVAL` + `POST_BREAK`) | Implementado | 2026-06-24 |
| [0003](0003-modelagem-execucao-post-break.md) | Modelagem da execução `POST_BREAK` independente | Implementado | 2026-06-24 |
| [0004](0004-janela-de-envio-por-shift.md) | Janela de envio configurável por turma/shift | Implementado | 2026-06-24 |
| [0005](0005-autenticacao-token-hub.md) | Autenticação centralizada no Hub (validação do access token) | Aceito (pendência `GET /me`) | 2026-06-24 |
| [0006](0006-autorizacao-local-checklist.md) | Autorização local por `userType` + `classes[].role` | Implementado | 2026-06-24 |
| [0007](0007-integracao-hub-ports-adapters.md) | Integração com o Hub via portas e adaptadores (OpenFeign) | Implementado (pendências de contrato) | 2026-06-24 |
| [0008](0008-contrato-de-erro-apierror.md) | Contrato de erro padronizado (`ApiError`) | Implementado | 2026-06-24 |
| [0009](0009-mensageria-eventos-notificacao.md) | Mensageria de eventos de notificação (RabbitMQ) | Aceito (código na branch `#182`) | 2026-07-05 |
| [0010](0010-modulo-notificacoes.md) | Responsabilidade de notificações e (não) módulo local | Proposto | 2026-07-05 |
| [0011](0011-persistencia-jsonb-schema-respostas.md) | Persistência em `JSONB` do schema e das respostas | Implementado | 2026-07-05 |
| [0012](0012-versionamento-imutabilidade-template.md) | Versionamento e imutabilidade de templates (snapshot) | Implementado | 2026-07-05 |
| [0013](0013-conformidade-e-geracao-de-issues.md) | Cálculo de conformidade e geração de pendências | Implementado | 2026-07-05 |
| [0014](0014-maquina-estados-issue.md) | Máquina de estados da pendência + optimistic locking | Implementado | 2026-07-11 |
| [0015](0015-consulta-bulk-hub.md) | Consulta em lote (bulk) ao Hub para enriquecimento | Aceito | 2026-07-11 |
| [0016](0016-endpoints-agregacao-stats.md) | Endpoints de agregação (stats) para dashboards | Implementado | 2026-07-11 |
| [0017](0017-dashboard-composto-cache.md) | Dashboard composto, cache e atualização por polling | Aceito | 2026-07-11 |
| [0018](0018-observabilidade-prometheus.md) | Observabilidade: exposição de métricas Prometheus | Implementado | 2026-07-11 |
| [0019](0019-build-portal-logging-github-packages.md) | Dependência `portal-logging` via GitHub Packages | Aceito | 2026-07-11 |
| [0020](0020-issues-como-modulo-de-negocio-independente.md) | `issues` como módulo de negócio independente (substitui parte da ADR-0001) | Implementado | 2026-07-18 |

> **Histórico de numeração:** o ADR-0001 substitui e traduz o antigo
> `0001-modular-layered-architecture.md` (inglês) e absorve a proposta paralela de ports & adapters.
> Os ADRs 0002–0007 foram migrados do vault `back-recorders` (numeração original 001–006) e
> renumerados para a sequência coerente acima. Os ADRs 0009–0013 foram adicionados em 2026-07-05
> (mensageria, notificações, JSONB, versionamento/snapshot e conformidade/issues). Os ADRs 0014–0019
> foram adicionados em 2026-07-11 (máquina de estados de issue, consulta bulk ao Hub, endpoints de
> agregação, dashboard composto/cache, observabilidade Prometheus e build via GitHub Packages). O
> ADR-0020 foi adicionado em 2026-07-18 para reverter a decisão de aninhar `issues` dentro de
> `checklist` (ADR-0001), formalizando `issues` como módulo par comunicando-se por portas.

---

## Processo

### Ciclo de vida do status

```text
Proposto  ->  Aceito  ->  Implementado
                              |
                              +-> Substituído por ADR-XXXX  (uma nova decisão o sucede)
                              +-> Descontinuado             (deixa de valer sem substituta)
```

- **Proposto** — em discussão, ainda não decidido.
- **Aceito** — decisão tomada; implementação pode estar pendente.
- **Implementado** — decisão refletida no código de `develop`.
- **Substituído** — sucedido por outro ADR; manter o arquivo com o link para o sucessor.
- **Descontinuado** — não vale mais; manter como histórico.

### Regras

1. **ADR é imutável após aceito.** Para mudar a decisão, crie um ADR novo que a substitua.
   Correções factuais pequenas (drift doc↔código) podem ser anotadas numa seção
   `## Atualização <data>` ao final, sem reescrever a decisão original.
2. **Numeração sequencial** de quatro dígitos (`0001`, `0002`, …). Não reutilizar números.
3. **Um arquivo por decisão.** Nome: `NNNN-titulo-curto-em-kebab-case.md`.
4. **Sempre em português**, seguindo o [template](0000-template.md).
5. **Links relativos** entre documentos do repo (nunca caminhos absolutos de máquina).

### Criar um ADR novo

1. Copie [`0000-template.md`](0000-template.md) para `NNNN-titulo.md` com o próximo número.
2. Preencha as seções; marque o status inicial.
3. Adicione a linha no índice acima.
4. Abra/atualize no mesmo PR da mudança que motivou a decisão.
