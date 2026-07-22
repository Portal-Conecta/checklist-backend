# ADR-0009 — Mensageria de Eventos de Notificação (RabbitMQ)

- **Status:** Aceito (implementação na branch `feature/#182-implementa-mensageria`, ainda fora do `develop`)
- **Data:** 2026-07-05
- **Autor:** danielsismer
- **Depende de:** [ADR-0007](0007-integracao-hub-ports-adapters.md)
- **Relacionado:** [ADR-0010](0010-modulo-notificacoes.md), [ADR-0013](0013-conformidade-e-geracao-de-issues.md)

---

## Contexto

Alguns fatos do Checklist precisam **notificar pessoas** (ex.: checklist submetido com não
conformidade, prazo perdido, três dias sem checklist). Entregar notificação (push/e-mail, resolução
de destinatários, preferências) **não é responsabilidade do Checklist** — é do Hub. Além disso, a
notificação não pode acoplar-se à transação de submissão: se o e-mail/entrega falha, o checklist não
pode falhar; e se a transação faz rollback, nada deve ser notificado.

Precisávamos de um mecanismo **assíncrono e desacoplado** entre o Checklist (produtor de eventos) e o
Hub (consumidor/entregador), resiliente a falha de consumo.

---

## Decisão

O Checklist **publica eventos de notificação** em um broker **RabbitMQ**; o Hub consome e resolve a
entrega. O Checklist descreve a **intenção** (título, corpo, filtros e escopo), não os destinatários
finais.

### Topologia (`RabbitMQConfig`)

- **Topic exchange** durável.
- **Fila** durável, com **dead-letter** (`x-dead-letter-exchange=""` + `x-dead-letter-routing-key`)
  apontando para uma **DLQ** durável — mensagem não processável não se perde.
- Binding por `routingKey`; payload em **JSON** (`JacksonJsonMessageConverter`).
- Toda a mensageria é **condicional**: `@ConditionalOnProperty(app.rabbitmq.enabled, matchIfMissing=true)`,
  permitindo desligar o broker em ambientes/testes sem RabbitMQ.
- Configuração externalizada em `app.rabbitmq.*` (`exchange`, `queue`, `dlq`, `routingKey`), alimentada
  pelas variáveis `RABBITMQ_*` do ambiente.

### Publicação transacional (não perde nem vaza)

- A publicação acontece via `@TransactionalEventListener(phase = AFTER_COMMIT)`
  (`ChecklistNonComplianceEventListener`): o evento só vai ao broker **depois** do commit da execução.
  Rollback ⇒ nada é publicado.
- Um evento de domínio interno (`ChecklistNonComplianceEvent`, carregando a `ChecklistExecution`) é
  disparado no fluxo de submissão e traduzido para o contrato externo `NotificationEvent`.

### Contrato do evento (`NotificationEvent`) — intenção, não destinatário

```text
messageId, correlationId, source, eventType, occurredAt,
title, body, filters[] (ex.: ROLE=TEACHER), scope[] (ex.: CLASS=<id>), metadata{}
```

O produtor diz **quem deve ser alcançado por filtro/escopo** (ex.: professores da turma X); o **Hub
resolve os destinatários reais**. `metadata` carrega dados de deep-link (ex.: rota para a execução).

### Porta e adaptador (coerente com [ADR-0007](0007-integracao-hub-ports-adapters.md))

- Porta (application): `NotificationEventPublisher`.
- Adaptador (infrastructure): `RabbitMQNotificationPublisher`.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Chamar o Hub por HTTP síncrono no submit | Acopla a submissão à disponibilidade do Hub; falha de entrega derruba a operação |
| Publicar dentro da transação (antes do commit) | Rollback publicaria evento fantasma; broker não participa da transação do banco |
| Checklist resolver destinatários e entregar | Duplica responsabilidade do Hub (usuários/preferências) e fere a fronteira de dados |
| Sem DLQ | Mensagem não processável seria perdida silenciosamente |

---

## Consequências

### Positivas

- Submissão desacoplada da entrega de notificação; resiliente a Hub/broker fora do ar.
- `AFTER_COMMIT` garante consistência (só notifica o que foi persistido).
- DLQ dá visibilidade e reprocessamento de falhas.
- Broker desligável por propriedade — dev/testes rodam sem RabbitMQ.

### Pontos de atenção / negativas

- Introduz uma dependência de infraestrutura (RabbitMQ) a operar e monitorar.
- Entrega **at-least-once**: o consumidor (Hub) deve ser idempotente por `messageId`.
- O contrato `NotificationEvent` é acordo entre Checklist e Hub — mudanças exigem coordenação.
- Ainda **fora do `develop`**: promover a "Implementado" quando a `#182` for mergeada.

---

## Atualização (pendências observadas na `#182`)

- `NotificationEventType` está com **package incorreto** (`main.java.com.portal...`) e o `eventType`
  publicado é uma string literal (`"checklist.non_compliance.created"`) em vez das constantes. Ajustar
  antes do merge — não altera a decisão desta ADR.

---

## Referências

- `shared/messaging/config/RabbitMQConfig`, `RabbitMQProperties`
- `shared/messaging/event/NotificationEvent`, `shared/messaging/notification/NotificationEventType`
- `module/checklist/application/port/out/messaging/NotificationEventPublisher`
- `module/checklist/infrastructure/messaging/RabbitMQNotificationPublisher`
- `module/checklist/infrastructure/messaging/ChecklistNonComplianceEventListener`
