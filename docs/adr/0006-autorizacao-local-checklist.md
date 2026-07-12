# ADR-0006 — Autorização Local do Módulo Checklist

- **Status:** Implementado
- **Data:** 2026-06-11 (revisado em 2026-06-24)
- **Autor:** danielsismer
- **Depende de:** [ADR-0005](0005-autenticacao-token-hub.md)
- **Relacionado:** [Fluxo operacional](../dominio/fluxo-operacional.md)

---

## Contexto

Depois que o Hub autentica e o Checklist valida o token, o módulo ainda precisa decidir **quais ações
aquele usuário pode executar**. Autenticação responde "quem é o usuário?"; autorização responde "o que
ele pode fazer dentro do Checklist?".

---

## Decisão

A autorização operacional é aplicada **localmente**, a partir do `RequestContext` montado do token.
O `RequestContext` combina `userId`, `userType` (perfil global) e `classes[]` (vínculos com turmas).

### Perfis globais (`userType`)

| `userType` | Papel no Checklist |
|---|---|
| `STUDENT` | Sem acesso operacional |
| `REPRESENTATIVE` | Opera checklist nas turmas com role permitida |
| `TEACHER` | Opera checklist nas turmas com role permitida |
| `SENAI` / `WEG` | Perfis gerenciais |
| `ADMIN` | Reservado para administração ampla |

### Papéis por turma (`classes[].role`)

| `role` | Uso |
|---|---|
| `STUDENT` | Vínculo acadêmico sem permissão operacional |
| `REPRESENTATIVE` / `TEACHER` | Permite operar checklist da turma |

### Regras

- **Operação de checklist** (criar/submeter/cancelar): `userType ∈ {REPRESENTATIVE, TEACHER}`
  **e** vínculo na turma **e** `classes[].role ∈ {REPRESENTATIVE, TEACHER}`.
- **Gestão de templates** (criar/editar/versionar/ativar): `userType ∈ {SENAI, WEG}`.
- **Dashboard e edição de checklist submetido**: `SENAI` / `WEG`.
- **Bloqueio de acesso operacional para gerenciais:** mesmo que um token `SENAI`/`WEG` traga
  `classes[].role`, esses perfis **não** criam/submetem checklist operacional.

Métodos no `RequestContext`: `canManageChecklistTemplates()`, `canAccessChecklistModule()`,
`canOperateChecklistExecutionForClass(classId)`, `canCancelChecklistExecution(...)`.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Autorizar só por `userType` | Não considera escopo por turma |
| Autorizar só por `classes[].role` | Não diferencia perfil global |
| Permitir `SENAI`/`WEG` operar checklist | Mistura responsabilidade gerencial e operacional |

---

## Consequências

### Positivas

- Regras explícitas dentro do módulo; o frontend não decide permissão.
- A turma vira parte obrigatória da autorização operacional.

### Pontos de atenção

- Mudanças de permissão no Hub só entram no Checklist quando o usuário recebe **novo token**
  (sem `permissionVersion`).
- Casos de borda de perfil (`ADMIN`, `REPRESENTATIVE` vs `STUDENT`) estão no [Riscos](../riscos.md)
  (RISK-AUTH-002/003).

---

## Atualização 2026-06-24

Falhas de autorização devem retornar **`403 Forbidden`** (via `AccessDeniedException`, conforme
[ADR-0008](0008-contrato-de-erro-apierror.md)). Há pelo menos um ponto que lança
`IllegalArgumentException` para falta de permissão (`SearchChecklistItemUseCase`), o que hoje retorna
`400`. Padronizar para `403` — rastreado em [Riscos](../riscos.md) (RISK-AUTH-004).

---

## Referências

- `shared/context/RequestContext`
- [ADR-0005 — Autenticação](0005-autenticacao-token-hub.md)
