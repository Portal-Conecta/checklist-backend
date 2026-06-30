# Notificações — Topologia RabbitMQ e Contrato de Payload

## Status atual

| Lado | Situação |
|------|----------|
| **checklist-backend (publisher)** | Implementado — Issue #182 |
| **core-backend / Hub (consumer)** | Implementado — `NotificationMessageConsumer` ativo nos perfis `dev` e `prod` |
| **Fluxo ponta a ponta** | Pendente de teste integrado entre os dois serviços |

---

## Topologia RabbitMQ

Declarada no core-backend (`NotificationRabbitMqConfig`). Criada automaticamente ao subir o core-backend.

| Recurso | Nome | Tipo | Detalhes |
|---------|------|------|----------|
| Exchange | `notifications.exchange` | `topic` | durable, non-auto-delete |
| Fila principal | `notifications.dispatch.q` | durable | redireciona rejeitadas para a DLQ |
| DLQ | `notifications.dispatch.dlq` | durable | recebe mensagens que excederam o retry |
| Routing key | `notification.requested` | — | usada pelo publisher para rotear ao exchange |

### Binding

```
notifications.exchange  --[notification.requested]-->  notifications.dispatch.q
```

### Retry configurado no Hub

| Parâmetro | Valor padrão |
|-----------|-------------|
| `max-delivery-attempts` | 3 |
| `initial-interval` | 1 000 ms |
| `multiplier` | 2× |
| `max-interval` | 10 000 ms |
| `default-requeue-rejected` | false |

### Subir localmente (checklist-backend ou core-backend)

```bash
docker compose up rabbitmq -d
# Management UI: http://localhost:15672  (guest / guest)
```

---

## O que o checklist-backend publica

O publisher envia uma mensagem JSON ao exchange `notifications.exchange` com routing key `notification.requested`.

### Campos do payload

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `messageId` | `string` | ✅ | ID único da mensagem. Garante idempotência no Hub — reprocessamentos com o mesmo ID reutilizam a notificação existente. |
| `correlationId` | `string` | ❌ | ID de correlação para rastreamento entre serviços. |
| `source` | `string` | ✅ | Identificador do serviço produtor (ex: `checklist-backend`). |
| `eventType` | `string` | ✅ | Tipo do evento de negócio (ex: `CHECKLIST_ITEM_COMPLETED`). |
| `occurredAt` | `string` (ISO-8601) | ✅ | Data/hora em que o evento ocorreu (ex: `2025-06-01T14:30:00Z`). |
| `title` | `string` | ✅ | Título da notificação exibida ao usuário. |
| `body` | `string` | ✅ | Corpo/descrição da notificação. |
| `filters` | `array<Filter>` | ❌ | Restringe quem recebe dentro do escopo. Se vazio, todos os membros do escopo são notificados. |
| `scope` | `array<Scope>` | ✅ (mín. 1) | Define as entidades afetadas (turma, curso, usuário). |
| `metadata` | `object` | ❌ | Dados auxiliares para o front-end (ex: links de navegação). Não sensíveis. |

### Objeto `Scope`

| Campo | Tipo | Valores aceitos | Descrição |
|-------|------|-----------------|-----------|
| `type` | `string` | `USER`, `CLASS`, `COURSE` | Tipo do escopo. `ROOM` e `GLOBAL` são ignorados pelo Hub (aviso no log). |
| `correlationId` | `string` (UUID) | — | ID da entidade referenciada (userId, classId ou courseId). |

### Objeto `Filter`

| Campo | Tipo | Valores aceitos | Descrição |
|-------|------|-----------------|-----------|
| `type` | `string` | `ROLE` | Único tipo suportado. |
| `value` | `string` | `STUDENT`, `TEACHER`, `REPRESENTATIVE` | Restringe a notificação por papel dentro do escopo. |

---

## Exemplos de Payload

### Notificar todos os alunos de uma turma

```json
{
  "messageId": "checklist-abc123-20250601",
  "correlationId": "task-789",
  "source": "checklist-backend",
  "eventType": "CHECKLIST_ITEM_COMPLETED",
  "occurredAt": "2025-06-01T14:30:00Z",
  "title": "Item do checklist concluído",
  "body": "O item 'Entregar relatório' foi marcado como concluído.",
  "filters": [
    { "type": "ROLE", "value": "STUDENT" }
  ],
  "scope": [
    { "type": "CLASS", "correlationId": "f47ac10b-58cc-4372-a567-0e02b2c3d479" }
  ],
  "metadata": {
    "checklistItemId": "item-456",
    "route": "/classes/f47ac10b-58cc-4372-a567-0e02b2c3d479/checklist"
  }
}
```

### Notificar um usuário diretamente

```json
{
  "messageId": "checklist-direct-20250601-001",
  "correlationId": null,
  "source": "checklist-backend",
  "eventType": "CHECKLIST_ASSIGNED",
  "occurredAt": "2025-06-01T09:00:00Z",
  "title": "Novo checklist atribuído a você",
  "body": "Você recebeu um novo checklist para a turma de Matemática.",
  "filters": [],
  "scope": [
    { "type": "USER", "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890" }
  ],
  "metadata": {
    "classId": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
  }
}
```

### Notificar professores de um curso inteiro

```json
{
  "messageId": "checklist-course-event-20250601",
  "correlationId": "course-op-321",
  "source": "checklist-backend",
  "eventType": "COURSE_CHECKLIST_UPDATED",
  "occurredAt": "2025-06-01T11:15:00Z",
  "title": "Checklist do curso atualizado",
  "body": "O checklist do curso foi atualizado pelo coordenador.",
  "filters": [
    { "type": "ROLE", "value": "TEACHER" }
  ],
  "scope": [
    { "type": "COURSE", "correlationId": "d290f1ee-6c54-4b01-90e6-d701748f0851" }
  ],
  "metadata": {}
}
```

---

## O que acontece no Hub ao receber a mensagem

1. `NotificationMessageConsumer` recebe e valida o payload.
2. `ProcessNotificationRequestUseCase` verifica idempotência pelo `messageId`.
3. Cria o registro em `notifications` (tabela).
4. `NotificationRecipientPortAdapter` resolve os destinatários:
   - `USER` → insere diretamente em `user_notifications`.
   - `CLASS` → busca membros da turma filtrados por role e insere em lote.
   - `COURSE` → busca membros do curso filtrados por role e insere em lote.
5. Destinatários consultam notificações via `GET /notifications`.

---

## O que ainda falta para o fluxo funcionar ponta a ponta

- [ ] Teste integrado real: checklist-backend publica → core-backend consome → `user_notifications` populada.
- [ ] Validar que o publisher usa exatamente `notification.requested` como routing key.
- [ ] Definir suporte futuro para escopos `ROOM` e `GLOBAL` (atualmente ignorados com warning no Hub).
