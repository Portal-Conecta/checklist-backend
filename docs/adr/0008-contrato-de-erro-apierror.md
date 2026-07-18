# ADR-0008 — Contrato de Erro Padronizado (`ApiError`)

- **Status:** Implementado
- **Data:** 2026-06-24 (PR #143)
- **Autor:** danielsismer
- **Relacionado:** [ADR-0006](0006-autorizacao-local-checklist.md), [ADR-0001](0001-arquitetura-modular.md), [Riscos](../riscos.md)

---

## Contexto

A API expunha erros de forma inconsistente (`ErrorResponseDTO` antigo, mensagens variadas, mapeamento
de status disperso). Clientes (frontend e integração) precisam de um **corpo de erro estável e único**
para todas as respostas de falha, e de um **mapa previsível** entre exceção e status HTTP.

---

## Decisão

Todo erro retornado pela API usa o record único `ApiError`, montado centralmente em
`shared/exception/GlobalHandlerException` (`@RestControllerAdvice`). O DTO antigo `ErrorResponseDTO`
foi **removido**.

### Formato do corpo

```java
public record ApiError(
    String timestamp,  // Instant.now().toString() — ISO-8601 UTC
    int    status,     // código HTTP, ex.: 404
    String error,      // reason phrase, ex.: "Not Found"
    String message,    // mensagem amigável/contextual
    String path        // request URI que originou o erro
) {}
```

Exemplo:

```json
{
  "timestamp": "2026-06-24T13:50:00.123Z",
  "status": 409,
  "error": "Conflict",
  "message": "Ja existe checklist ativo para esta turma, sala, periodo, dia e tipo.",
  "path": "/api/checklist-executions/drafts"
}
```

### Mapa exceção → status (estado atual em `develop`)

| Exceção | Status | Observação |
|---|---|---|
| `MethodArgumentNotValidException` | **400** | mensagem do primeiro campo inválido |
| `MethodArgumentTypeMismatchException` | **400** | parâmetro de URL com tipo errado |
| `HttpMessageNotReadableException` | **400** | JSON malformado |
| `IllegalArgumentException` | **400** | argumento inválido de negócio |
| `SubmissionWindowViolationException` | **400** | fora da janela de envio |
| `AccessDeniedException` | **403** | sem permissão |
| `EntityNotFoundException` | **404** | recurso ausente |
| `NoResourceFoundException` | **404** | rota inexistente |
| `HttpRequestMethodNotSupportedException` | **405** | método HTTP não suportado |
| `DataIntegrityViolationException` | **409** | conflito; trata índice `uidx_execution_no_duplicate` com mensagem dedicada |
| `OptimisticLockingFailureException` | **409** | registro alterado por outro usuário |
| `IllegalStateException` | **409** | regra de negócio violada |
| `DataAccessResourceFailureException` | **503** | banco indisponível |
| `HubIntegrationException` | **503** | falha de integração com o Hub |
| `Exception` (fallback) | **500** | rede de segurança; não vaza stacktrace |

A segurança (filtro JWT) usa `SecurityErrorResponseWriter`, alinhado ao mesmo formato `ApiError`
para respostas 401/403 fora do `@RestControllerAdvice`.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Manter `ErrorResponseDTO` | Formato inconsistente; mensagens dispersas |
| Padrão RFC 7807 (`application/problem+json`) | Mais verboso que o necessário para o escopo atual; pode virar ADR futuro |
| Tratar erro em cada controller | Duplicação; mapeamento divergente |

---

## Consequências

### Positivas

- Corpo de erro único e previsível para todos os endpoints.
- Mapeamento de status centralizado em um só lugar.

### Pontos de atenção (drift conhecido — ver [Riscos](../riscos.md))

1. **Autorização via `IllegalArgumentException` → 400.** `SearchChecklistItemUseCase` lança
   `IllegalArgumentException` para falta de permissão, resultando em **400** em vez de **403**.
   Deveria lançar `AccessDeniedException` (RISK-AUTH-004).
2. **Janela de envio retorna 400, não 422.** Documentação antiga citava `422` para
   `SubmissionWindowViolationException`; o código atual retorna **400**. Decidir o status canônico
   para "regra de negócio violada" (ver [ADR-0004](0004-janela-de-envio-por-shift.md)).
3. **Javadoc desalinhado** no handler de `SubmissionWindowViolationException` (comentário fala em
   `IllegalState`/409). Correção cosmética pendente.

---

## Referências

- `shared/exception/ApiError`
- `shared/exception/GlobalHandlerException`
- `shared/security/error/SecurityErrorResponseWriter`
