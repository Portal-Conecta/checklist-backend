# ADR-0007 — Integração com o Hub via Portas e Adaptadores

- **Status:** Implementado — com pendências de contrato
- **Data:** 2026-06-11 (revisado em 2026-06-24)
- **Autor:** danielsismer
- **Relacionado:** [ADR-0001](0001-arquitetura-modular.md), [ADR-0004](0004-janela-de-envio-por-shift.md), [ADR-0005](0005-autenticacao-token-hub.md), [Riscos](../riscos.md)

---

## Contexto

O Checklist depende de dados que **pertencem ao Hub**: usuários, salas, turmas, cursos e vínculos
acadêmicos. Esses dados **não** devem ser duplicados no banco do Checklist — o Checklist mantém apenas
**referências e snapshots** necessários às suas regras.

---

## Decisão

O Checklist acessa o Hub por meio de **providers** (portas de saída). O domínio e os casos de uso
dependem de **interfaces**, nunca de Feign/HTTP/DTO externo diretamente:

```text
UseCase  ->  Hub{Resource}Provider (porta)
                -> HttpHub{Resource}Provider (adaptador)
                     -> Hub{Resource}Client (@FeignClient)
                          -> Hub Core API
```

Em desenvolvimento/testes, mocks por profile substituem os adaptadores HTTP.

### Providers atuais

| Provider | Responsabilidade |
|---|---|
| `HubUserProvider` | validar usuário autenticado |
| `HubRoomProvider` | validar sala / obter referência de sala |
| `HubClassProvider` | validar turma / obter `shift`, `courseId`, nome, número |
| `HubCourseProvider` | validar curso retornado pela turma |
| `HubMeProvider` | dados do usuário autenticado (`/me/courses`) — **adicionado** |

### Diferença `role` vs `classRole`

- Token: `classes[].role`.
- Resposta `/me/courses`: `courses[].classes[].classRole`.

O Checklist mantém essa diferença porque vem do contrato atual do Hub.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Duplicar dados do Hub no banco do Checklist | Quebra a fonte única de verdade; sincronização frágil |
| Casos de uso chamarem Feign diretamente | Acopla domínio ao contrato HTTP do Hub |

---

## Consequências

### Positivas

- Domínio desacoplado do Hub; mudança de contrato fica isolada nos adaptadores.
- Mocks permitem desenvolver enquanto o Hub evolui.

### Pontos de atenção

- Se o Hub mudar nomes de campos, ajusta-se o DTO Feign do adaptador.
- Chamadas unitárias podem virar gargalo em listagens (ver RISK-BULK-001 em [Riscos](../riscos.md)).

### Pendências de contrato

1. Definir endpoint oficial para validar usuário autenticado (recomendado `GET /me`).
2. Confirmar se `GET /courses/{courseId}` pode ser chamado por perfil operacional.
3. Confirmar porta/base URL oficial do Hub em ambiente local (`HUB_API_URL`).
4. Documentar no Hub a diferença `classes[].role` (token) vs `classRole` (`/me/courses`).

---

## Atualização 2026-06-24

A integração foi reorganizada para `shared/integration/hub/` com a nomenclatura **adapter** explícita:

```text
shared/integration/hub/client/{classes,course,me,room}/    @FeignClient + DTOs de resposta
shared/integration/hub/adapter/{classes,course,me,room}/   HttpHub{Resource}Provider (adaptadores)
shared/integration/hub/config/                             configuração Feign
shared/integration/hub/exception/                          HubIntegrationException (-> 503)
```

Isso alinha a integração ao padrão de ports & adapters do [ADR-0001](0001-arquitetura-modular.md).

---

## Referências

- `shared/integration/hub/`
- [ADR-0001 — Arquitetura modular](0001-arquitetura-modular.md)
