# ADR-0010 — Responsabilidade de Notificações e (Não) Módulo Local

- **Status:** Proposto
- **Data:** 2026-07-05
- **Autor:** Daniel
- **Depende de:** [ADR-0009](0009-mensageria-eventos-notificacao.md)
- **Relacionado:** [ADR-0007](0007-integracao-hub-ports-adapters.md)

---

## Contexto

Com a mensageria de eventos ([ADR-0009](0009-mensageria-eventos-notificacao.md)), surge a pergunta de
fronteira: **a que serviço pertence a notificação?** Havia a expectativa de um pacote
`modules/notification` dentro do Checklist para orquestrar notificações localmente. Ao mesmo tempo, o
Hub já é dono de usuários, preferências e canais de entrega.

Hoje o Checklist apenas **emite intenção** (`NotificationEvent` com `filters`/`scope`) e o Hub
**resolve destinatários e entrega**. Não existe `modules/notification` implementado — nem no `develop`,
nem na branch `#182`.

---

## Decisão (proposta)

**Notificação é responsabilidade do Hub.** O Checklist não deve manter um módulo de notificações que
resolva destinatários, gerencie canais ou armazene estado de entrega. O papel do Checklist termina em
**publicar o evento de intenção** no broker.

Um pacote local `modules/notification` só se justifica se o Checklist precisar de **estado próprio de
notificação** (ex.: histórico/leitura de notificações específicas do domínio checklist que o Hub não
guarde). Enquanto esse requisito não existir, **não criar o módulo** — evitar código especulativo.

> Esta decisão está **Proposta**: registra a direção atual e a fronteira, mas depende de confirmação
> do PO/Hub sobre onde vive o estado de notificação.

---

## Alternativas consideradas

| Alternativa | Por que (ainda) não |
|---|---|
| Criar `modules/notification` agora | Sem requisito de estado local; duplicaria responsabilidade do Hub |
| Checklist resolver destinatários/entregar | Fere a fronteira de dados (usuários/preferências são do Hub) |
| Hub dono total da notificação (**direção atual**) | Aceita; só falta confirmação formal e definição de estado |

---

## Consequências

### Positivas

- Mantém o Checklist coeso no seu domínio; menos superfície para manter.
- Sem código especulativo enquanto não há requisito real.

### Pontos de atenção / negativas

- Se o negócio pedir histórico/leitura de notificações no escopo checklist, será preciso reabrir esta
  decisão (novo ADR que a substitua).
- A fronteira exata (o que é "intenção" vs "entrega") precisa ficar acordada com o Hub.

---

## Referências

- [ADR-0009 — Mensageria de eventos](0009-mensageria-eventos-notificacao.md)
- `shared/messaging/event/NotificationEvent` (contrato de intenção)
