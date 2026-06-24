# ADR-0005 — Autenticação via Token do Hub

- **Status:** Aceito — pendente ajuste fino do endpoint de validação de usuário
- **Data:** 2026-06-11 (revisado em 2026-06-24)
- **Autor:** Daniel
- **Relacionado:** [ADR-0006](0006-autorizacao-local-checklist.md), [ADR-0007](0007-integracao-hub-ports-adapters.md), [Riscos](../riscos.md)

---

## Contexto

A plataforma Portal Conecta tem vários módulos. O Checklist **não deve implementar login próprio** —
isso duplicaria a regra de autenticação e criaria inconsistência entre serviços. A autenticação é
**centralizada no Hub**: o Hub autentica, emite o access token, e os demais módulos apenas validam
esse token antes de aplicar suas regras locais.

---

## Decisão

O Checklist consome **apenas o access token emitido pelo Hub**. Fluxo:

```text
1. Usuário faz login no Hub
2. Hub valida credenciais e usuário ativo
3. Hub emite access token JWT
4. Frontend envia o token ao Checklist
5. Checklist valida assinatura, expiração e claims obrigatórias
6. Checklist monta o RequestContext
7. Checklist aplica autorização local (ver ADR-0006)
```

O Checklist **não** implementa: login, registro, refresh token, emissão de token, nem sessão.

### Contrato do access token

```json
{
  "jti": "uuid-do-token",
  "sub": "uuid-do-usuario",
  "userType": "SENAI",
  "classes": [ { "classId": "uuid-da-turma", "role": "TEACHER" } ],
  "iat": 1779835463,
  "exp": 1780440263
}
```

| Claim | Uso no Checklist |
|---|---|
| `jti` | Identificador único do token |
| `sub` | ID (UUID) do usuário autenticado |
| `userType` | Perfil global |
| `classes[].classId` | Turma vinculada |
| `classes[].role` | Papel do usuário naquela turma |
| `iat` / `exp` | Emissão / expiração |

### Decisões específicas

- **`sub` é o usuário autenticado** — padrão JWT; o Hub extrai via `claims.getSubject()`.
- **No token usa-se `classes[].role`, não `classRole`.** `classRole` aparece apenas em respostas HTTP
  como `/me/courses`, nunca no token.
- **Sem `permissionVersion` no fluxo atual** — ideia reservada para invalidação mais forte de
  permissões no futuro; fora de escopo.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Login próprio no Checklist | Duplica autenticação; inconsistência entre serviços |
| Receber `userId` no body | Inseguro; frontend poderia forjar usuário |
| `permissionVersion` / `POST /authorization/check` | Fora de escopo atual |

---

## Consequências

### Positivas

- Identidade centralizada no Hub; o Checklist não confia no frontend para identificar usuário.
- Use cases recebem o usuário pelo `SecurityContext`, não pelo corpo da requisição.

### Pontos de atenção

- O Checklist precisa validar o usuário do token contra o Hub.
- O endpoint historicamente usado (`GET /users/{userId}`) **não está documentado** no OpenAPI atual
  do Hub. Recomendação: contrato contextual `GET /me`. Ver [Riscos](../riscos.md) (RISK-AUTH-001).

---

## Atualização 2026-06-24

Foi adicionado o adaptador `HubMeProvider` / `HubMeClient` em `shared/integration/hub/.../me`, hoje
expondo `GET /me/courses`. **Ainda não confirmado** um `GET /me` para validar o usuário autenticado —
RISK-AUTH-001 segue **em aberto** até verificarmos por qual endpoint o `HubUserProvider`/validação
de usuário passa em `develop`.

---

## Referências

- `shared/security/` (filter, token, config)
- `shared/context/RequestContext`
- [ADR-0007 — Integração com o Hub](0007-integracao-hub-ports-adapters.md)
