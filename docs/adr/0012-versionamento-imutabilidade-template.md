# ADR-0012 — Versionamento e Imutabilidade de Templates (Snapshot)

- **Status:** Implementado
- **Data:** 2026-07-05
- **Autor:** danielsismer
- **Depende de:** [ADR-0011](0011-persistencia-jsonb-schema-respostas.md)
- **Relacionado:** [ADR-0013](0013-conformidade-e-geracao-de-issues.md), [Fluxo operacional](../dominio/fluxo-operacional.md)

---

## Contexto

Um checklist submetido é um **registro histórico**: representa o que foi verificado naquele dia, com
aquele conjunto de itens. Se o template pudesse ser editado livremente depois, execuções antigas
passariam a "apontar" para itens que não existiam quando foram preenchidas — corrompendo o histórico
e as pendências geradas.

Ao mesmo tempo, os perfis gerenciais (SENAI/WEG) precisam **evoluir** os templates: corrigir itens,
adicionar seções, publicar melhorias. Era preciso conciliar histórico estável com evolução contínua.

---

## Decisão

Templates são **versionados e imutáveis após publicação**, e as execuções fazem **snapshot** do que
importa para o histórico.

### Versionamento

- Todas as versões de um mesmo template compartilham `template_group_id` (gerado na criação e herdado
  pelas versões seguintes) e têm `version` incremental.
- **Edição só é permitida em `DRAFT`** (`PATCH /api/checklist-templates/{id}`). Um template `ACTIVE`
  não é editado no lugar.
- Para evoluir um template ativo, cria-se uma **nova versão em `DRAFT`** a partir do `ACTIVE`
  (`POST /api/checklist-templates/{id}/new-version`, `CreateChecklistTemplateVersionUseCase`),
  preservando o histórico. A nova versão é ativada quando pronta.

### Vínculo da execução à versão

- A execução referencia a **linha específica** do template usado
  (`ChecklistExecution.checklistTemplate`, `@ManyToOne`). Como cada versão é uma linha própria, a
  execução fica presa à versão vigente no momento da criação, não a um "template vivo" mutável.
- `templateVersion` é exposto no `ChecklistExecutionResponseDTO`.

### Snapshot em pendências

- Ao gerar uma issue de item não conforme, o título do item (truncado) é gravado diretamente em
  `ChecklistIssue.title` no momento da submissão (ver [ADR-0013](0013-conformidade-e-geracao-de-issues.md)).
  Assim a pendência permanece legível mesmo que o template mude depois.
- **Atualização (#236):** a coluna dedicada `item_title_snapshot` foi removida por ser redundante com
  `title`, que já carrega o título do item no momento da criação da issue.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Editar o template ativo no lugar | Corrompe execuções e issues históricas |
| Copiar o `schema_json` inteiro para dentro da execução | Duplicação grande; a referência à versão já garante estabilidade |
| Não versionar (um template por sala) | Impede evoluir sem quebrar histórico |
| Soft-delete de itens | Não resolve mudança de título/semântica de um item existente |

---

## Consequências

### Positivas

- Execuções e pendências antigas continuam **coerentes** após evolução de templates.
- Histórico auditável: dá para saber qual `version` foi usada em cada execução.
- Evolução de template sem downtime nem migração de dados históricos.

### Pontos de atenção / negativas

- `active` + `status` precisam ser mantidos consistentes (só uma versão ativa por grupo é o esperado).
- Criar versão gera novas linhas — o crescimento de `checklist_template` é por versão, não por edição.
- Consultas de "template atual da sala" devem filtrar por `active`/`ACTIVE`, não pelo grupo inteiro.

---

## Referências

- `module/checklist/domain/model/ChecklistTemplate` (`templateGroupId`, `version`, `status`, `active`)
- `module/checklist/application/usecase/template/command/create/CreateChecklistTemplateVersionUseCase`
- `module/checklist/domain/model/ChecklistExecution` (`checklistTemplate`)
- `module/issues/domain/model/ChecklistIssue` (`title`)
