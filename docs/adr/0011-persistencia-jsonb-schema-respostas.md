# ADR-0011 — Persistência em JSONB do Schema e das Respostas

- **Status:** Implementado
- **Data:** 2026-07-05
- **Autor:** danielsismer
- **Relacionado:** [ADR-0001](0001-arquitetura-modular.md), [ADR-0012](0012-versionamento-imutabilidade-template.md), [ADR-0013](0013-conformidade-e-geracao-de-issues.md)

---

## Contexto

Um checklist não tem estrutura fixa: cada template define suas próprias **seções** e **itens**, e
esse formato muda por sala e evolui ao longo do tempo. Modelar cada item como coluna/tabela
relacional exigiria migração de schema a cada novo formato de checklist, além de um modelo
`template → seção → item → resposta` com muitos joins para ler um único formulário.

As respostas seguem a mesma forma dinâmica do template: a lista de itens respondidos varia conforme
o template usado. Precisávamos guardar template e respostas de forma flexível, sem acoplar o banco à
forma de cada checklist, e mantendo consulta eficiente por sala/turma.

---

## Decisão

O **schema do template** e as **respostas da execução** são persistidos como **`JSONB`** no
PostgreSQL, mapeados via Hibernate com `@JdbcTypeCode(SqlTypes.JSON)`:

- `checklist_template.schema_json` (`JSONB`, `NOT NULL`) → `Map<String, Object> schemaJson`.
- `checklist_execution.answers_json` (`JSONB`, `NOT NULL`) → `Map<String, Object> answersJson`.

O `schema_json` guarda a árvore `sections[].items[]` (chave, título, descrição, `required`, ordem). A
estrutura é **livre** e interpretada pelo cliente em tempo de execução. O restante — identidade,
status, score, tipo, período, datas — permanece em **colunas relacionais tipadas**, porque são campos
consultáveis e com regra de negócio.

> Regra prática: **conteúdo dinâmico do formulário → JSONB; metadados e chaves de consulta →
> colunas.** IDs de sala/turma/usuário, status, tipo e período nunca entram no JSON.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Tabelas relacionais `section`/`item`/`answer` | Muitos joins para ler um formulário; migração a cada novo formato; complexidade sem ganho no MVP |
| Coluna `TEXT` com JSON serializado | Perde validação/consulta nativa de JSON do Postgres; parsing manual |
| Banco de documentos (ex.: Mongo) | Introduz segunda tecnologia de dados; o restante do domínio é relacional e transacional |

---

## Consequências

### Positivas

- Novos formatos de checklist **não exigem migração** de banco.
- Leitura do formulário e das respostas em **um único registro**, sem joins.
- Colunas relacionais preservam consultas e regras (status, score, unicidade, paginação).
- `JSONB` permite indexação/consulta nativa no Postgres caso surja necessidade futura.

### Pontos de atenção / negativas

- O banco **não valida** o conteúdo do JSON — a validação de itens obrigatórios e chaves é
  responsabilidade da aplicação (`ChecklistExecutionAnswerValidationService`).
- Mudança na forma do JSON exige compatibilidade retroativa na leitura (ver
  [ADR-0012](0012-versionamento-imutabilidade-template.md) sobre snapshot/versão).
- `Map<String, Object>` é fracamente tipado; o contrato real vive no `ChecklistSchema` da aplicação.
- Testes de repositório dependem de Postgres real (Testcontainers), pois H2 não reproduz `JSONB`.

---

## Referências

- `modules/checklist/domain/model/ChecklistTemplate` (`schemaJson`)
- `modules/checklist/domain/model/ChecklistExecution` (`answersJson`)
- `modules/checklist/domain/schema/ChecklistSchema`
- `modules/checklist/application/service/execution/ChecklistExecutionAnswerValidationService`
- Migration `db/migration/V1__criar_estrutura_inicial_checklist.sql`
