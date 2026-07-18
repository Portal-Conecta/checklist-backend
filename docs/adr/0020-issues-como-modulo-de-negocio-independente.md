# ADR-0020 — `issues` como módulo de negócio independente

- **Status:** Implementado
- **Data:** 2026-07-18
- **Autor:** danielsismer
- **Depende de:** [ADR-0001](0001-arquitetura-modular.md)
- **Relacionado:** [ADR-0013](0013-conformidade-e-geracao-de-issues.md), [ADR-0014](0014-maquina-estados-issue.md)

---

## Contexto

A ADR-0001 decidiu manter `issues` **aninhado** dentro do módulo Checklist
(`modules/checklist/issues/**`), com a justificativa de que "issues são criadas e geridas como
parte das execuções". Na prática, `issues` já espelhava internamente as mesmas quatro camadas
hexagonais do módulo pai (`domain`, `application`, `infrastructure`, `presentation`) — ou seja, já
se comportava como um módulo à parte, só não formalizado como tal.

O acoplamento real entre os dois era direto, não por portas: `ChecklistIssue` (entidade de
`issues`) tinha uma relação JPA bidirecional `@ManyToOne`/`@OneToMany` com `ChecklistExecution`
(entidade de `checklist`); `ListIssuesByExecutionUseCase` (em `issues`) injetava
`ChecklistExecutionRepositoryPort` (porta de `checklist`) diretamente; `ChecklistIssueService` (em
`checklist`) construía instâncias de `ChecklistIssue` (entidade de `issues`) diretamente.

Historicamente essa mesma estrutura (`module/checklist` + `module/issues` como módulos pares,
nome de pacote base no singular) já existiu no projeto (commits `b3622f0`/`2647ac1`, issue #158) e
foi revertida para a forma aninhada atual — inclusive com um teste de regressão
(`ArchitectureRulesTest.legacyPackagesMustNotReturn`) adicionado para impedir o retorno dessa
estrutura. Times diferentes trabalhando no projeto hoje divergem sobre qual das duas formas é
correta. Esta ADR reverte a decisão da ADR-0001 novamente, desta vez formalizando a separação com
portas explícitas em vez de acoplamento direto, e substitui o guard-rail antigo por um novo que
impõe a fronteira em vez de proibir a estrutura.

---

## Decisão

`issues` passa a ser um **módulo de negócio par** de `checklist` — `module/issues/**`, no mesmo
nível de `module/checklist/**` — comunicando-se com ele **exclusivamente** através de quatro portas
estreitas:

| Porta | Dono | Implementada por | Propósito |
|---|---|---|---|
| `ExecutionAccessPort` (`module.issues.application.port.out.execution`) | issues | checklist (`ExecutionAccessAdapter`) | existência + turma de uma execução, o mínimo para autorizar consulta de pendências |
| `IssueCreationPort` (`module.checklist.application.port.out.issue`) | checklist | issues (`IssueCreationAdapter`) | criação de pendências de não-conformidade a partir de respostas de execução |
| `IssueStatsPort` (`module.checklist.application.port.out.issue`) | checklist | issues (`IssueStatsAdapter`) | agregados de issues consumidos pelo dashboard composto |
| `ExecutionIssuesQueryPort` (`module.checklist.presentation.port`) | checklist | issues (`ExecutionIssuesQueryAdapter`) | pendências de uma execução, já como DTO de resposta HTTP, para compor `ChecklistExecutionResponseDTO` |

A relação JPA bidirecional entre `ChecklistIssue` e `ChecklistExecution` foi substituída por uma
coluna crua `executionId` (mesma coluna `checklist_execution_id` do banco, sem migration) — a
issue referencia a execução por identificador, não por objeto JPA gerenciado. `ChecklistExecution`
não guarda mais uma coleção `issues`; sua listagem/exibição acontece via `ExecutionIssuesQueryPort`.

A fronteira é imposta por
`ArchitectureRulesTest.issuesAndChecklistMustOnlyCommunicateThroughPorts` — qualquer import de um
módulo para dentro do outro fora dos pacotes de porta acima falha o build.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Manter aninhado (ADR-0001 original) | Já havia acoplamento direto disfarçado por proximidade de pacote; dificultava ownership independente por times diferentes |
| Extrair para microsserviço/módulo Maven separado | Prematuro — sem requisito de escala/deploy independente que justifique (mesmo racional da ADR-0001 contra microsserviços) |
| Shared-kernel (DTOs/domínio compartilhados livremente, sem portas) | Reintroduziria o mesmo acoplamento direto que esta ADR remove |

---

## Consequências

### Positivas

- Fronteira entre os módulos é testável (`ArchitectureRulesTest`), não apenas convencional.
- `issues` pode evoluir sua máquina de estados, stats e controllers sem tocar em classes compiladas
  de `checklist`.
- `ChecklistExecution` fica menor — sem coleção JPA `issues` nem cascade para raciocinar.

### Pontos de atenção / negativas

- Quatro portas + adaptadores novos para manter.
- `ChecklistIssueStatsRepository.countByChecklistType()` continua fazendo `JOIN` SQL direto em
  `checklist_execution` — acoplamento no nível de banco, não de Java. Não foi resolvido por esta
  ADR; gap conhecido e aceito por ora.
- `ChecklistExecutionResponseDTO` (presentation de checklist) embute `ChecklistIssueResponseDTO`
  (presentation de issues) diretamente — sancionado explicitamente como parte do contrato de
  `ExecutionIssuesQueryPort`, não é uma brecha na regra.

---

## Referências

- `module/checklist/application/port/out/issue/IssueCreationPort.java`,
  `module/checklist/application/port/out/issue/IssueStatsPort.java`
- `module/checklist/presentation/port/ExecutionIssuesQueryPort.java`
- `module/issues/application/port/out/execution/ExecutionAccessPort.java`
- `module/issues/domain/model/ChecklistIssue.java`
- `src/test/java/.../unit/architecture/ArchitectureRulesTest.java`
