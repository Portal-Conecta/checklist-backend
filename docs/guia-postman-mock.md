# Guia De Testes No Postman Com JWT Mock

Este guia mostra o caminho mais simples para testar a Checklist API no Postman usando os dados mockados do projeto.

> Importante: esses JSONs podem mudar conforme o backend evoluir. Quando alguma regra ou DTO mudar, atualize este guia junto.

## 1. Subir A API

Na raiz do projeto, suba o banco:

```powershell
docker compose up -d
```

Rode a API com o profile `mock`:

```powershell
$env:SPRING_PROFILES_ACTIVE="mock"
$env:SERVER_PORT="8083"
$env:JWT_SECRET="<BASE64_HS256_SECRET>"
mvn spring-boot:run
```

Valide se a API subiu:

```http
GET http://localhost:8083/actuator/health
```

Esse endpoint nao precisa de token.

## 2. Dados Mock Disponiveis

Esses IDs existem em `src/main/resources/application-mock.properties`.

### Usuarios

```text
33333333-3333-3333-3333-333333333331
33333333-3333-3333-3333-333333333332
44444444-4444-4444-4444-444444444444
a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d
```

### Salas

```text
11111111-1111-1111-1111-111111111111
11111111-1111-1111-1111-111111111112
11111111-1111-1111-1111-111111111113
```

### Turmas

```text
22222222-2222-2222-2222-222222222221
22222222-2222-2222-2222-222222222222
8f8e8d8c-8b8a-8f8e-8d8c-8b8a8f8e8d8c
1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d
```

## 3. Configurar O Postman

Crie um environment chamado `Checklist Local Mock`.

Adicione estas variaveis:

```text
BASE_URL=http://localhost:8083
hubToken=<colar o JWT gerado>
TEMPLATE_ID=<preencher depois>
EXECUTION_ID=<preencher depois>
```

Nas rotas privadas, use:

```text
Authorization > Type: Bearer Token
Token: {{hubToken}}
```

## 4. Gerar O JWT

Use um gerador de JWT, como `jwt.io`, apenas para gerar o token de teste local.

### Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload

Use este payload:

```json
{
  "jti": "db70849d-4915-4345-962f-be2dd215efd6",
  "sub": "44444444-4444-4444-4444-444444444444",
  "userType": "SENAI",
  "classes": [
    {
      "classId": "8f8e8d8c-8b8a-8f8e-8d8c-8b8a8f8e8d8c",
      "classRole": "TEACHER"
    },
    {
      "classId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
      "classRole": "STUDENT"
    }
  ],
  "iat": 1779835463,
  "exp": 1780440263
}
```

### Secret

Use um secret local temporario, nunca um secret real versionado. Para gerar um valor Base64 com 32 bytes no PowerShell:

```powershell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

Configure o valor gerado em `JWT_SECRET` e use o mesmo valor no Postman:

```text
<BASE64_HS256_SECRET>
```

No `jwt.io`, selecione algoritmo `HS256`.

Se a ferramenta tiver a opcao `secret base64 encoded`, marque essa opcao.

Se a ferramenta nao tiver essa opcao, use a versao decodificada do seu proprio secret:

```text
<decoded-32-byte-secret>
```

Depois de gerar o token, copie o JWT completo e cole na variavel `hubToken` do Postman.

O JWT completo tem este formato:

```text
header.payload.signature
```

Nao cole apenas o JSON do payload no Postman.

## 5. Validar Autenticacao

Request:

```http
GET {{BASE_URL}}/api/checklist-templates
```

Authorization:

```text
Bearer Token: {{hubToken}}
```

Resultado esperado:

```text
200 OK
```

Se retornar `401`, o token esta ausente, expirado, mal assinado ou com algum campo invalido.

Se retornar `403`, o token e valido, mas o usuario nao tem permissao para aquela acao.

## 6. Criar Template De Checklist

O usuario do token tem `userType` `SENAI`, entao pode criar template.

Request:

```http
POST {{BASE_URL}}/api/checklist-templates
```

Body:

```json
{
  "title": "Checklist de Chegada - Sala Padrao",
  "description": "Template para verificacao dos equipamentos e estado geral da sala antes do inicio das aulas.",
  "roomId": "11111111-1111-1111-1111-111111111111",
  "schemaJson": {
    "sections": [
      {
        "key": "verificacao_geral",
        "title": "Verificacao Geral",
        "order": 1,
        "items": [
          {
            "key": "ar_condicionado",
            "title": "Ar Condicionado",
            "description": "Verificar se o aparelho esta ligando, respondendo ao controle e resfriando corretamente.",
            "required": true,
            "order": 1
          },
          {
            "key": "iluminacao",
            "title": "Iluminacao",
            "description": "Testar todas as lampadas e interruptores da sala.",
            "required": true,
            "order": 2
          },
          {
            "key": "projetor",
            "title": "Projetor",
            "description": "Verificar funcionamento, qualidade da imagem e conexoes.",
            "required": true,
            "order": 3
          },
          {
            "key": "limpeza",
            "title": "Limpeza e Organizacao",
            "description": "Verificar se a sala esta limpa, lixeiras vazias e cadeiras organizadas.",
            "required": true,
            "order": 4
          }
        ]
      }
    ]
  }
}
```

Resultado esperado:

```text
201 Created
```

Copie o campo `id` da resposta e cole na variavel `TEMPLATE_ID`.

Observacao: o template nasce como `DRAFT`. A ativacao acontece na proxima rota.

## 7. Ativar Template

Request:

```http
PATCH {{BASE_URL}}/api/checklist-templates/{{TEMPLATE_ID}}/activate
```

Resultado esperado:

```text
200 OK
```

## 8. Criar Draft De Checklist

Request:

```http
POST {{BASE_URL}}/api/checklist-executions/drafts
```

Body:

```json
{
  "templateId": "{{TEMPLATE_ID}}",
  "roomId": "11111111-1111-1111-1111-111111111111",
  "classId": "8f8e8d8c-8b8a-8f8e-8d8c-8b8a8f8e8d8c",
  "period": "MORNING",
  "checklistType": "ARRIVAL"
}
```

Resultado esperado:

```text
201 Created
```

Copie o campo `id` da resposta e cole na variavel `EXECUTION_ID`.

## 9. Enviar Checklist

Request:

```http
POST {{BASE_URL}}/api/checklist-executions/{{EXECUTION_ID}}/submit
```

Body:

```json
{
  "answers": [
    {
      "itemKey": "ar_condicionado",
      "value": "COMPLIANT",
      "observation": null,
      "answeredAt": "2026-05-27T12:00:00Z"
    },
    {
      "itemKey": "iluminacao",
      "value": "NON_COMPLIANT",
      "observation": "Lampada queimada.",
      "answeredAt": "2026-05-27T12:01:00Z"
    },
    {
      "itemKey": "projetor",
      "value": "COMPLIANT",
      "observation": null,
      "answeredAt": "2026-05-27T12:02:00Z"
    },
    {
      "itemKey": "limpeza",
      "value": "COMPLIANT",
      "observation": null,
      "answeredAt": "2026-05-27T12:03:00Z"
    }
  ]
}
```

Resultado esperado:

```text
200 OK
```

## 10. Testes Negativos Simples

### Sem Token

Remova o token da requisicao.

Resultado esperado:

```text
401 Unauthorized
```

### Usuario Inexistente

No payload do JWT, troque `sub` para um UUID que nao esta nos usuarios mockados.

Resultado esperado:

```text
401 Unauthorized
```

### Sala Inexistente

No body, troque `roomId` para um UUID que nao esta nas salas mockadas.

Resultado esperado:

```text
404 Not Found
```

### Turma Sem Permissao Operacional

No draft, troque `classId` para:

```text
1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d
```

No token base, essa turma tem `classRole` `STUDENT`. Esse papel nao pode criar draft.

Resultado esperado:

```text
403 Forbidden
```

### Nao Conforme Sem Observacao

No submit, deixe uma resposta `NON_COMPLIANT` com `observation` vazio ou nulo.

Resultado esperado:

```text
400 Bad Request
```

### Checklist Duplicado

Tente criar outro draft com a mesma turma, sala, periodo, dia e tipo.

Resultado esperado:

```text
400 Bad Request
```

Para testar novamente, altere `period`, `checklistType`, turma, sala ou limpe o banco local.

## 11. Resumo Dos Status

```text
200 OK            -> sucesso
201 Created       -> recurso criado
400 Bad Request   -> payload invalido ou regra de negocio
401 Unauthorized  -> problema de token
403 Forbidden     -> usuario autenticado, mas sem permissao
404 Not Found     -> recurso, sala ou turma nao encontrado
```
