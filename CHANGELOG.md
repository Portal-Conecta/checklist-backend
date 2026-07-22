# Changelog

Todas as mudanças relevantes da **Checklist API** são registradas neste arquivo.
Formato baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e
versionamento semântico ([SemVer](https://semver.org/lang/pt-BR/)).

Tipos de entrada: `Adicionado`, `Alterado`, `Corrigido`, `Removido`, `Documentação`.

> Este é o changelog **canônico** do projeto. As decisões arquiteturais ficam nos
> [ADRs](docs/adr/README.md).


## [1.0.0] — 2026-07-22

> 🚀 **Marco do projeto — a maior entrega até aqui.** Reta final e mais intensa do
> desenvolvimento: a Checklist API vira um serviço **completo e estável**, pronto
> para produção. Dezenas de issues, features de ponta a ponta, dashboards, o módulo
> de issues com máquina de estados e todo o trabalho de observabilidade e polimento.

Primeira versão estável (publicação `#192`).

### Destaques

- ✅ **Execução ponta a ponta** com rascunho e **autosave incremental** de respostas.
- 📊 **Dashboard composto com cache** + endpoints de agregação/estatística.
- 🐞 **Módulo de issues independente** com máquina de estados completa.
- 🧩 **Tipos/categorias de item** e busca; filtros na listagem (incl. por sala).
- 🔔 **Mensageria (RabbitMQ)** e **observabilidade** (Prometheus, logs JSON/MDC).
- 🔐 **Autorização madura**: `403` para perfis sem acesso, paridade SENAI/WEG, `ADMIN` sem acesso operacional.

### Adicionado

- **#119** Preenchimento incremental do checklist (autosave) via
  `PATCH /api/checklist-executions/{id}/answers`.
- **#193** Consulta de execução por ID e listagem de todas as execuções com
  validação de permissão.
- **#239 / #224** Filtros na listagem de execuções, incluindo **filtro por sala**.
- **#230** Tipos/categorias de item no checklist (`ChecklistCategory`) e migration
  `V5` de `category` em template e execução.
- **#215** Endpoint composto de **dashboard com cache**, restrito à gestão
  (SENAI/WEG); performance por turno e tendência de conformidade.
- **#151** Módulo de **issues** com máquina de estados completa (iniciar, resolver,
  validar, reabrir, retomar, cancelar).
- **#235** Paridade **WEG/SENAI** para validar e reabrir issues.
- **#222** Permissão de cancelamento de execução entre representantes e rastreio de
  ações (`canceled_by`), com migration `V4`.
- **#182** Integração de **mensageria RabbitMQ** para notificações (gated por
  `RABBITMQ_ENABLED`).
- **#199** Logging estruturado (JSON) com **MDC**.
- **#213** Observabilidade: instrumentação Prometheus/Actuator e labels no compose.
- **#191** `Dockerfile` multi-stage com usuário não-root.
- **#112 / #119** Seed de dev (`ChecklistDevSeedInitializer`): templates ativos por
  sala do Hub e janelas de envio para as turmas de teste.

### Alterado

- **#228** *Issues* extraído como **módulo de negócio independente** via portas
  explícitas; renomeia o pacote `modules` → `module`.
- **#226** `ADMIN` deixa de ter acesso operacional ao Checklist; `SENAI`/`WEG` com
  poder gerencial equivalente.
- **#192** Retorno **`403`** (em vez de `200`) para perfis sem acesso.
- **#119** Janela de envio validada também na **edição** de checklist já enviado.
- **#225** Remove `roomId` do `CreateChecklistExecutionCommand` (derivado do template).
- Seed de dev: mantém a **sala 214** sem template ativo e alinha os nomes de turma à
  convenção do core (`MI78`, `MI77`, `MT78` com janela; `MT77` sem janela) — antes
  usava nomes inexistentes (`MIDS1`/`ADSIS1`/…), o que não semeava janela alguma.
- Cache do dashboard: TTL reduzido de 60s para 5s.

### Corrigido

- **409** ao ativar uma nova versão de template de checklist.
- Preenchimento do `room` na resposta de execução de checklist.
- **#243 / #236** Colisões de versão de migration no Flyway e índices únicos
  ausentes (`V7`) que só existiam no `schema-postgresql.sql`.
- **#213** Configuração de acesso ao `portal-logging` no CI; correções de
  encoding/BOM em arquivos Java e do wrapper Maven.

### Removido

- **#236** Coluna `item_title_snapshot` de `checklist_issue` (migration `V6`).
- **#225** Campo `roomId` do comando de criação de execução.

### Documentação

- ADRs **0014–0020** (máquina de estados da issue, consulta bulk, agregações,
  dashboard com cache, observabilidade, build/portal-logging, issues como módulo
  independente).
- **README** reescrito e atualizado para o estado atual da v1.0.0; changelog
  consolidado num único arquivo canônico na raiz.

---

## [0.2.0] — 2026-06-15

> 🧱 **A virada de arquitetura.** Sprint pesada de consolidação: sai a camada
> mockada, entra a **integração real com o Hub**, a arquitetura modular é
> padronizada e nascem as fundações que sustentam a v1 — janelas de envio,
> persistência JSONB, versionamento de template e migração para Flyway.

Segunda versão consolidada, focada em fundação técnica e integração.

### Destaques

- 🔌 **Integração real com o Hub** (ports & adapters), fim dos providers mockados.
- 🏛️ **Arquitetura modular padronizada** e contrato de erro unificado (`ApiError`).
- 🕒 **Janelas de envio** por turma + tipo e **persistência JSONB** do schema/respostas.
- 🗄️ **Fundação do Flyway** e banco em nuvem; primeiras agregações de estatística.

### Adicionado

- **#119** Modelo de **janela de envio** por turma + tipo
  (`UNIQUE(class_id, checklist_type)`), guardando o `shift`.
- **#147 / #148** Busca de itens de template por **texto** (`?query=`) e por
  **categoria** (`?category=`), com `ChecklistItemSearchPort` e adaptador de query.
- **#186 / #185** Primeiros endpoints de **agregação/estatística**.
- **#202 / #171** **Flyway** e banco em nuvem: fundação de migrations (`V1`–`V3`) e
  `baseline-on-migrate` opcional em `dev`.
- **#152** Consulta *bulk* ao Hub usando a listagem da Checklist.
- OpenAPI: `SecurityRequirement bearerAuth` aplicado nas operações protegidas.

### Alterado

- **#158 / #161** **Integração real com o Hub**: remove mocks e padroniza a
  arquitetura modular (ports & adapters); integração reorganizada em
  `shared/integration/hub/` com recurso `me/` (`/me/courses`).
- **#143** Contrato de erro padronizado para o record
  `ApiError(timestamp, status, error, message, path)`; `GlobalHandlerException` e
  `SecurityErrorResponseWriter` adaptados.

### Removido

- **#143** `ErrorResponseDTO` (substituído por `ApiError`).

### Documentação

- ADRs **0009–0013** e **0016** (mensageria/eventos, módulo de notificações,
  persistência JSONB, versionamento/imutabilidade de template, conformidade/geração
  de issues), visão de arquitetura, fluxo operacional e registro de riscos.

---

## [0.1.0] — 2026-06-01

> 🏗️ **A fundação.** Do zero à primeira API funcional numa sprint intensa: toda a
> estrutura em camadas, os fluxos base de template e execução e a autenticação com
> token do Hub nasceram aqui.

Primeira versão consolidada ao fim da sprint.

### Destaques

- 🧩 **Arquitetura base** em camadas (domínio, aplicação, apresentação, infraestrutura).
- 📋 **Fluxos de template e execução** (rascunho, envio, cancelamento).
- 🔑 **Autenticação stateless** com JWT do Hub e autorização por perfil/papel.

### Adicionado

- Estrutura inicial da Checklist API com camadas de domínio, aplicação, apresentação e infraestrutura.
- Fluxo de templates de checklist, incluindo criação, consulta, listagem e ativação.
- Fluxo de execução de checklist, incluindo criação de rascunho, envio e cancelamento.
- Integração inicial com token JWT emitido pelo Hub.
- Resolução do usuário autenticado via `SecurityContext`.
- Providers para consulta de usuário, turma e sala, com implementações HTTP e mock.
- Regras iniciais de autorização por tipo de usuário e papel na turma.
- Bloqueio de acesso operacional para perfis gerenciais `SENAI` e `WEG`.
- Regras de negócio para envio, obrigatoriedade de respostas e controle de duplicidade.
- Tratamento global de erros e padronização de respostas HTTP.
- Documentação de testes com Postman e fluxo de autenticação mockada.
- JavaDocs nas principais classes do projeto.

### Alterado

- Organização dos DTOs, mappers, use cases e repositories conforme a arquitetura do módulo.
- Ajustes na configuração de ambiente para leitura de variáveis locais.
- Alinhamento da segurança com o fluxo de autenticação centralizada pelo Hub.

### Corrigido

- Inicialização da aplicação em ambiente local.
- Configuração de `RestClient.Builder`.
- Regras de permissão para evitar acesso indevido de perfis gerenciais a rotas operacionais.

### Observações

- Esta versão ainda utilizava providers mockados quando o Hub não estava disponível.
- Não havia tag anterior no repositório; esta versão marca o primeiro fechamento formal.

---

## Como manter este arquivo

- Toda mudança relevante de comportamento/contrato entra aqui **no mesmo PR**, sob `[Não publicado]`.
- Ao cortar uma release/tag, mover as entradas de `[Não publicado]` para uma seção `[x.y.z] — data`.
- Referenciar a issue/PR (`#NNN`) e, quando aplicável, o ADR relacionado.
