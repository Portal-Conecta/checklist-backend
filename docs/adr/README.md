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

### Propostos / planejados

| # | Decisão | Status |
|---|---|---|
| 0009 | Mensageria de eventos de notificação (RabbitMQ) | A documentar — código existe (`NotificationEventPublisher` → `RabbitMQNotificationPublisher`) |
| 0010 | Módulo de notificações (`modules/notification`) | A documentar — pacote reservado, ainda sem implementação |

> **Histórico de numeração:** o ADR-0001 substitui e traduz o antigo
> `0001-modular-layered-architecture.md` (inglês) e absorve a proposta paralela de ports & adapters.
> Os ADRs 0002–0007 foram migrados do vault `back-recorders` (numeração original 001–006) e
> renumerados para a sequência coerente acima.

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
