# Changelog

Mudanças relevantes do Checklist Backend. Formato baseado em
[Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/).
O projeto ainda não publica releases versionadas; as mudanças vivem sob `develop`.

Tipos de entrada: `Adicionado`, `Alterado`, `Corrigido`, `Removido`, `Documentação`.

---

## [Não publicado] — `develop`

### Documentação
- Criada a documentação oficial versionada em `docs/`: índice, ADRs 0001–0008, visão de
  arquitetura, fluxo operacional, registro de riscos e este changelog (2026-06-24).
- Consolidado o ADR-0001 (arquitetura modular + ports & adapters + CQRS leve) em pt-BR, unificando o
  antigo `0001-modular-layered-architecture.md` (inglês) com a proposta de ports & adapters.
- Migrados os 6 ADRs do vault `back-recorders` (numeração original 001–006), renumerados para
  ADR-0002 a ADR-0007, e adicionado o ADR-0008 (contrato de erro `ApiError`).

### Adicionado
- **#147** Busca de itens de template por título/descrição:
  `GET /api/checklist-templates/items/search?query=`, com `SearchChecklistItemUseCase`,
  porta `ChecklistItemSearchPort` e adaptador `ChecklistItemQueryRepository`.

### Alterado
- **#143** Contrato de erro padronizado para o record `ApiError(timestamp, status, error, message, path)`;
  `GlobalHandlerException` e `SecurityErrorResponseWriter` adaptados; Swagger dos controllers atualizado.
- Integração com o Hub reorganizada em `shared/integration/hub/` com nomenclatura `adapter/` explícita
  e novo recurso `me/` (`/me/courses`).
- Janela de envio passou a ser configurada **por turma + tipo** (`UNIQUE(class_id, checklist_type)`),
  guardando `shift` — antes era por shift global.

### Removido
- **#143** `ErrorResponseDTO` (substituído por `ApiError`).

---

## Como manter este arquivo

- Toda mudança relevante de comportamento/contrato entra aqui **no mesmo PR**, sob `[Não publicado]`.
- Ao cortar uma release/tag, mover as entradas de `[Não publicado]` para uma seção `[x.y.z] — data`.
- Referenciar a issue/PR (`#NNN`) e, quando aplicável, o ADR relacionado.
