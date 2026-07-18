# ADR-0001 — Arquitetura Modular em Camadas (Ports & Adapters + CQRS Leve)

- **Status:** Implementado
- **Data:** 2026-06-12 (revisado e ampliado em 2026-06-24)
- **Autor:** danielsismer
- **Relacionado:** [ADR-0007](0007-integracao-hub-ports-adapters.md), [ADR-0008](0008-contrato-de-erro-apierror.md), [Visão de arquitetura](../arquitetura/visao-geral.md)

> Este ADR consolida em pt-BR o antigo `0001-modular-layered-architecture.md` (inglês) e absorve a
> proposta paralela de ports & adapters, passando a ser a **única** decisão de arquitetura do módulo.

---

## Contexto

A Checklist API reúne templates, execuções, janelas de envio, issues, regras de segurança,
persistência e integração com o Hub. Essas responsabilidades usavam estruturas de pacote
inconsistentes — casos de uso, serviços, portas, DTOs e código de integração em níveis diferentes —
o que dificultava identificar **propriedade** e **direção de dependência**.

---

## Decisão

A aplicação adota uma estrutura por **módulos de negócio** com **camadas explícitas** e
**ports & adapters (hexagonal)**, com um **CQRS leve** (comandos e consultas separados na aplicação).

### Camadas

- **`domain`** — modelos de negócio, value objects, schemas, exceções, enums e **serviços de domínio
  puros**. Regras que combinam conceitos de domínio sem dependência externa ficam em `domain/service`
  (ex.: `PeriodResolver` deriva o período a partir de `Shift` + `ChecklistType`).
- **`application/usecase`** — comandos e consultas que orquestram fluxos de negócio.
- **`application/service`** — coordenação e validação reutilizáveis da aplicação (ex.: submit
  decomposto em `AnswerValidation`, `Scoring`, `Issue`, `DataMapper`).
- **`application/port/out`** — contratos exigidos para persistência (`persistence`), integração
  externa (`integration`) e mensageria (`messaging`). Ex.: `ChecklistItemSearchPort`,
  `NotificationEventPublisher`.
- **`infrastructure`** — implementações dos contratos (adaptadores de persistência e mensageria).
- **`presentation`** — controllers, DTOs de transporte e mapeamento HTTP.
- **`shared/integration`** — clients, adaptadores e configuração de integração com o Hub.

### CQRS leve

Casos de uso são separados fisicamente em **comando** (muda estado) e **consulta** (lê):

```text
application/usecase/<recurso>/command   Create..., Submit..., Activate..., Upsert...
application/usecase/<recurso>/query      Find..., List..., Search...
```

Mesmo modelo e mesma persistência — apenas separação de responsabilidade na aplicação. **Não** há
event sourcing nem bancos de leitura/escrita separados.

### Submódulo `issues`

O `issues` permanece **dentro** do módulo Checklist, porque issues são criadas e geridas como parte
das execuções. Mantém suas próprias fronteiras internas (`domain`, `application`, `infrastructure`,
`presentation`).

### Regra de dependência (aponta para dentro)

```text
presentation     -> application -> domain
infrastructure   -> portas da aplicação (port/out) -> domain
shared/integration -> portas da aplicação
```

O domínio **não** depende de Spring MVC, adaptadores de persistência, clients do Hub ou DTOs de
apresentação. Casos de uso dependem de **portas**, nunca de repositórios concretos ou clients HTTP.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Camadas clássicas (controller → service → repository) sem portas | Acopla domínio a JPA/HTTP; difícil testar e trocar IO |
| Microsserviços por módulo | Complexidade operacional sem ganho para o escopo atual |
| CQRS completo (event sourcing, read models) | Custo alto; sem requisito de escala/auditoria que justifique |
| Facades sobre os casos de uso | Removidas anteriormente; indireção sem valor |

---

## Consequências

### Positivas

- Nomes de pacote comunicam a responsabilidade de cada classe.
- Casos de uso separados de serviços reutilizáveis e contratos de integração.
- Infraestrutura pode ser trocada sem mudar a orquestração de negócio.
- Regras de domínio testáveis sem Spring ou serviços externos.
- Pacotes-placeholder vazios são desnecessários e não devem ser mantidos.

### Pontos de atenção

- Caminhos de pacote e imports ficam mais longos.
- Capacidades pequenas podem ter só uma classe por camada inicialmente.
- Mover `issues` para um módulo de topo independente exigirá uma porta de aplicação dedicada entre
  Checklist e Issues.
- O nome "Repository" num adaptador pode esconder o custo real da implementação — ver
  `ChecklistItemQueryRepository`, que varre JSONB em memória (RISK-PERF-001 em [Riscos](../riscos.md)).

---

## Validação

Testes de arquitetura verificam a consistência de pacotes e impedem que o código de aplicação dependa
diretamente de pacotes de apresentação ou infraestrutura. A suíte completa de testes deve passar após
mudanças estruturais.

---

## Referências

- [Visão de arquitetura](../arquitetura/visao-geral.md) — mapa de pacotes atual
- `module/checklist/application/port/out/`, `module/checklist/infrastructure/`

---

## Atualização 2026-07-18

A subseção "Submódulo `issues`" foi substituída pela [ADR-0020](0020-issues-como-modulo-de-negocio-independente.md):
`issues` deixou de ser um submódulo aninhado e passou a ser um módulo de negócio par
(`module/issues`), comunicando-se com `module/checklist` apenas através de portas explícitas.
