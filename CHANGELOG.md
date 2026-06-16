# Changelog

Todas as mudancas relevantes deste projeto serao registradas neste arquivo.

## [0.1.0] - 2026-06-01

Primeira versao consolidada ao fim da sprint.

### Adicionado

- Estrutura inicial da Checklist API com camadas de dominio, aplicacao, apresentacao e infraestrutura.
- Fluxo de templates de checklist, incluindo criacao, consulta, listagem e ativacao.
- Fluxo de execucao de checklist, incluindo criacao de rascunho, envio e cancelamento.
- Integracao inicial com token JWT emitido pelo Hub.
- Resolucao do usuario autenticado via `SecurityContext`.
- Providers para consulta de usuario, turma e sala, com implementacoes HTTP e mock.
- Regras iniciais de autorizacao por tipo de usuario e papel na turma.
- Bloqueio de acesso operacional para perfis gerenciais `SENAI` e `WEG`.
- Regras de negocio para envio, obrigatoriedade de respostas e controle de duplicidade.
- Tratamento global de erros e padronizacao de respostas HTTP.
- Documentacao de testes com Postman e fluxo de autenticacao mockada.
- JavaDocs nas principais classes do projeto para facilitar entendimento e manutencao.

### Alterado

- Organizacao dos DTOs, mappers, use cases e repositories conforme a arquitetura do modulo.
- Ajustes na configuracao de ambiente para leitura de variaveis locais.
- Alinhamento da seguranca com o fluxo esperado de autenticacao centralizada pelo Hub.

### Corrigido

- Inicializacao da aplicacao em ambiente local.
- Configuracao de `RestClient.Builder`.
- Regras de permissao para evitar acesso indevido de perfis gerenciais a rotas operacionais.

### Observacoes

- Esta versao ainda utiliza providers mockados quando o Hub nao esta disponivel.
- A integracao final com os providers reais do Hub deve ser validada nas proximas sprints.
- Nao ha tag anterior no repositorio; por isso esta versao marca o primeiro fechamento formal.

