# Registro de Riscos — Checklist Backend

> **Atualizado em:** 2026-06-24 · Base: `develop @ 20d30a0`
> **Documento vivo.** Provenance marcada por linha: ✔ verificado agora no código · ↪ herdado do
> vault (2026-06-11), **reconfirmar** contra `develop` atual.

---

## Riscos abertos

| Código | Risco | Impacto | Prioridade | Origem |
|---|---|---|---|---|
| RISK-PERF-001 | `ChecklistItemQueryRepository` varre **todos** os templates ativos e desserializa cada item em memória (sem SQL/JSONB nem índice GIN) | Busca degrada conforme cresce o nº de templates/itens | Média | ✔ |
| RISK-AUTH-004 | `SearchChecklistItemUseCase` lança `IllegalArgumentException` para falta de permissão → HTTP **400** em vez de **403** | Contrato de erro inconsistente ([ADR-0008](adr/0008-contrato-de-erro-apierror.md)) | Média | ✔ |
| RISK-DOC-001 | `SubmissionWindowViolationException` retorna **400**, mas notas antigas diziam **422**; Javadoc do handler também desalinhado | Confusão em integração/testes | Baixa | ✔ |
| RISK-AUTH-001 | Validação de usuário autenticado por `GET /users/{userId}`, não documentado no OpenAPI do Hub (`me/` hoje só expõe `/me/courses`) | Token real pode ser recusado | Alta | ↪ |
| RISK-HUB-001 | `GET /courses/{courseId}` pode retornar `403` para perfil operacional | Criação de draft pode falhar para professor/representante | Média | ↪ |
| RISK-OPENAPI-001 | Swagger registra `bearerAuth`, mas não aplica `security` nas operações | Testes manuais/integração confusos | Média | ↪ |
| RISK-AUTH-002 | `ADMIN` existe, mas sem permissão gerencial no Checklist | Perfil autentica e recebe `403` em fluxos administrativos | Média | ↪ |
| RISK-AUTH-003 | Representante depende de `userType=REPRESENTATIVE` **e** `classes[].role=REPRESENTATIVE` | Negado se o Hub emitir `userType=STUDENT` | Média | ↪ |
| RISK-BULK-001 | Providers não expõem bulk para histórico/dashboard | Muitas chamadas unitárias ao Hub em listagens | Baixa | ↪ |
| RISK-WINDOW-001 | Listagem de janelas aberta a qualquer autenticado | Pode expor configuração se a regra exigir escopo | Baixa | ↪ |

---

## Mitigados / absorvidos

| Risco | Situação |
|---|---|
| `period` informado pelo cliente | Mitigado — backend deriva via `PeriodResolver` ([ADR-0004](adr/0004-janela-de-envio-por-shift.md)) |
| Race condition de unicidade no banco | Mitigado — índice parcial `uidx_execution_no_duplicate` + handler `409` em `GlobalHandlerException` |
| `DEPARTURE` legado | Mitigado — tipo atual é `POST_BREAK` ([ADR-0002](adr/0002-redefinicao-tipos-checklist.md)) |
| Falta de `shift` no fluxo | Mitigado — `GET /classes/{classId}` retorna `shift` |
| WEG/SENAI criando draft | Mitigado — perfis gerenciais não acessam fluxo operacional |

---

## Pendências técnicas (a confirmar em `develop`)

- **Flyway** — schema versionado por migrations (era TECH-003 pendente; confirmar se já existe).
- Remover possíveis campos/helpers redundantes herdados de notas antigas (`active` em template, etc.).

---

## Próximas ações recomendadas

1. **RISK-PERF-001:** migrar a busca de itens para operador JSONB (`jsonb_path_query` / `@>`) com índice GIN, ou query nativa, em vez do scan em memória.
2. **RISK-AUTH-004 / RISK-DOC-001:** padronizar erros — autorização → `AccessDeniedException` (403); decidir status canônico para regra de negócio (400 vs 422) e alinhar Javadoc.
3. **RISK-AUTH-001:** definir/usar contrato `GET /me` no Hub e ajustar a validação de usuário.
4. **RISK-OPENAPI-001:** aplicar `SecurityRequirement` na config OpenAPI.
5. Reconfirmar os riscos marcados ↪ contra o código atual e atualizar a coluna de origem.

---

## Referências

- [ADRs](adr/README.md) · [Fluxo operacional](dominio/fluxo-operacional.md) · [Visão de arquitetura](arquitetura/visao-geral.md)
