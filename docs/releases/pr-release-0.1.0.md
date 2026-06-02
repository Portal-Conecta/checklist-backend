# Pull Request - Release 0.1.0

## Titulo sugerido

```text
#108 chore: prepara release 0.1.0
```

## Descricao do PR

```md
## Objetivo

Preparar a release `0.1.0` da Checklist API ao fim da sprint, seguindo o fluxo de GitFlow definido pelo Portal Conecta.

Esta branch `release/0.1.0` foi criada a partir da `develop` e tem como objetivo consolidar as entregas ja aprovadas e integradas durante a sprint, deixando a versao pronta para merge na `main`.

---

## O que foi feito

- Atualizada a versao do projeto no `pom.xml` de `0.0.1-SNAPSHOT` para `0.1.0`.
- Criado o arquivo `CHANGELOG.md` com o resumo das mudancas relevantes da sprint.
- Criada a documentacao da release em `docs/releases/v0.1.0.md`.
- Consolidada a descricao das principais entregas implementadas ate esta versao.
- Registradas observacoes sobre pontos ainda pendentes para proximas sprints.

---

## Principais entregas contempladas nesta release

- Estrutura inicial da Checklist API.
- Organizacao em camadas de dominio, aplicacao, apresentacao e infraestrutura.
- Fluxo de templates de checklist:
  - criacao;
  - consulta;
  - listagem;
  - ativacao.
- Fluxo de execucao de checklist:
  - criacao de rascunho;
  - envio;
  - cancelamento.
- Autenticacao stateless com JWT emitido pelo Hub.
- Resolucao do usuario autenticado via `SecurityContext`.
- Regras iniciais de autorizacao por tipo de usuario e papel na turma.
- Bloqueio de perfis gerenciais em rotas operacionais.
- Providers mock e HTTP para integracao com dados do Hub.
- Tratamento global de excecoes.
- Padronizacao de respostas HTTP.
- Documentacao de apoio para testes com Postman.
- JavaDocs nas principais classes para facilitar entendimento e manutencao.

---

## Como testar

Rodar os testes automatizados:

```powershell
mvn test
```

Ou, usando o Maven Wrapper quando disponivel:

```powershell
.\mvnw.cmd test
```

Resultado obtido localmente:

```text
Tests run: 96
Failures: 0
Errors: 0
Skipped: 1
BUILD SUCCESS
```

---

## Arquivos alterados

- `pom.xml`
- `CHANGELOG.md`
- `docs/releases/v0.1.0.md`

---

## Impacto

Esta release prepara o projeto para a primeira entrega versionada da Checklist API.

Nao foram alteradas regras de negocio, endpoints ou comportamento da aplicacao neste PR. As mudancas sao de versionamento e documentacao de release.

---

## Observacoes

- Esta versao ainda utiliza providers mockados quando o Hub nao esta disponivel.
- A integracao final com os providers reais do Hub deve ser validada nas proximas sprints.
- A versao `0.1.0` foi escolhida porque o projeto ainda nao possui contrato estavel suficiente para ser considerado `1.0.0`.
- Apos o merge na `main`, a tag `v0.1.0` deve ser criada a partir da `main`.
- Apos a criacao da tag, a `main` deve ser sincronizada novamente com a `develop`.

---

## Fluxo GitFlow seguido

```text
develop -> release/0.1.0 -> main
```

Apos aprovacao e merge:

```text
main -> develop
```

---

## Checklist antes do merge

- [x] Branch criada a partir da `develop`
- [x] PR aberto via branch `release/0.1.0`
- [x] Codigo compila sem erros
- [x] Testes automatizados executados
- [x] Nao foram adicionados arquivos desnecessarios como `.env`, `target/` ou arquivos locais da IDE
- [x] Commit segue Conventional Commits
- [x] Arquivos de release documentados
- [ ] PR aprovado por pelo menos 1 revisor
- [ ] Merge realizado na `main`
- [ ] Tag `v0.1.0` criada apos merge
- [ ] `main` sincronizada de volta com `develop`

---

## Commit principal

```text
#108 chore: prepara release 0.1.0
```

---

## Issue relacionada

Closes #108
```

## Proximos passos apos aprovar o PR

### 1. Atualizar a `main`

```powershell
git checkout main
git pull origin main
```

### 2. Criar a tag da release na `main`

```powershell
git tag -a v0.1.0 -m "Release v0.1.0"
git push origin v0.1.0
```

### 3. Sincronizar `main` de volta para `develop`

```powershell
git checkout develop
git pull origin develop
git merge origin/main
git push origin develop
```

## Link para abrir o PR

```text
https://github.com/Portal-Conecta/checklist-backend/pull/new/release/0.1.0
```

