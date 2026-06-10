# Fluxo Completo De Teste Com Mocks No Postman

Este documento mostra o fluxo mais simples para testar a Checklist API usando o profile `mock`.

No profile `mock`, a API valida um JWT real, mas consulta usuarios, salas e turmas em dados mockados no arquivo:

```text
src/main/resources/application-mock.properties
```

Ou seja: o token precisa estar assinado corretamente, mas a existencia de usuario, sala e turma vem dos mocks locais.

## 1. Subir A API Com Profile Mock

Na raiz do projeto:

```powershell
cd C:\Users\daniel_sismer\Downloads\conecta.checklist\checklist-backendv23
```

Suba o banco:

```powershell
docker compose up -d
```

Suba a API com profile `mock`:

```powershell
$env:SPRING_PROFILES_ACTIVE="mock"
$env:SERVER_PORT="8083"
$env:JWT_SECRET="<BASE64_HS256_SECRET>"
mvn spring-boot:run
```

Gere um secret local temporario com:

```powershell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

Se estiver usando IntelliJ, configure as variaveis de ambiente da Run Configuration:

```text
SPRING_PROFILES_ACTIVE=mock;SERVER_PORT=8083;JWT_SECRET=<BASE64_HS256_SECRET>
```

Depois pare a aplicacao antiga e rode novamente.

Se o projeto subir sem `SPRING_PROFILES_ACTIVE=mock`, ele pode tentar consultar providers HTTP reais do Hub ou usar configuracoes que ainda nao existem no ambiente local.

## 2. Validar Se A API Subiu

No Postman:

```http
GET http://localhost:8083/actuator/health
```

Authorization:

```text
No Auth
```

Resultado esperado:

```json
{
  "status": "UP"
}
```

Observacao: se essa rota estiver herdando Bearer Token da collection, troque a request para `No Auth`.

## 3. Dados Mock Usados No Teste

Use esses dados no fluxo:

```text
BASE_URL=http://localhost:8083
USER_ID=44444444-4444-4444-4444-444444444444
ROOM_ID=11111111-1111-1111-1111-111111111111
CLASS_ID=8f8e8d8c-8b8a-8f8e-8d8c-8b8a8f8e8d8c
JWT_SECRET=<BASE64_HS256_SECRET>
```

Esses IDs existem no `application-mock.properties`.

### Usuarios mockados

```text
33333333-3333-3333-3333-333333333331
33333333-3333-3333-3333-333333333332
44444444-4444-4444-4444-444444444444
a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d
```

### Salas mockadas

```text
11111111-1111-1111-1111-111111111111
11111111-1111-1111-1111-111111111112
11111111-1111-1111-1111-111111111113
```

### Turmas mockadas

```text
22222222-2222-2222-2222-222222222221
22222222-2222-2222-2222-222222222222
8f8e8d8c-8b8a-8f8e-8d8c-8b8a8f8e8d8c
1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d
```

## 4. Criar Environment No Postman

Crie um environment chamado:

```text
Checklist Local Mock
```

Adicione as variaveis:

```text
BASE_URL=http://localhost:8083
hubToken=<cole o JWT gerado aqui>
TEMPLATE_ID=
EXECUTION_ID=
```

## 5. Gerar O JWT

Use o site:

```text
https://jwt.io
```

### Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload

```json
{
  "jti": "db70849d-4915-4345-962f-be2dd215efd6",
  "sub": "44444444-4444-4444-4444-444444444444",
  "userType": "SENAI",
  "classes": [
    {
      "classId": "8f8e8d8c-8b8a-8f8e-8d8c-8b8a8f8e8d8c",
      "role": "TEACHER"
    },
    {
      "classId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
      "role": "STUDENT"
    }
  ],
  "iat": 1779835463,
  "exp": 1780440263
}
```

### Secret

Use o mesmo secret local temporario configurado em `JWT_SECRET`:

```text
<BASE64_HS256_SECRET>
```

No `jwt.io`:

- algoritmo: `HS256`;
- marque `secret base64 encoded`, se essa opcao aparecer;
- copie o token completo gerado.

O token completo tem este formato:

```text
header.payload.signature
```

Cole esse token completo na variavel `hubToken` do Postman.

Nao cole apenas o JSON do payload.

### Claims que o backend espera hoje

O codigo atual espera estes campos no token:

```text
jti      UUID do token
sub      UUID do usuario
userType STUDENT, REPRESENTATIVE, TEACHER, SENAI, WEG ou ADMIN
classes  lista de turmas do usuario
iat      data de emissao
exp      data de expiracao
```

Dentro de `classes`, cada item precisa ter:

```text
classId   UUID da turma
role     STUDENT, TEACHER ou REPRESENTATIVE
```

Se `exp` ficar no passado, gere outro token com uma data futura.

## 6. Configurar Authorization No Postman

Para todas as rotas privadas, use:

```text
Authorization > Type: Bearer Token
Token: {{hubToken}}
```

Nao escreva `Bearer` dentro do campo Token. O Postman adiciona isso sozinho.

Alternativa manual:

```text
Headers
Authorization: Bearer {{hubToken}}
```

Use uma forma ou outra. Evite configurar as duas ao mesmo tempo.

Rotas privadas nao devem ser testadas pelo navegador comum, porque o navegador nao envia o header `Authorization` sozinho. Para essas rotas, use Postman, Insomnia ou `curl`.

## 7. Testar Token

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

Se retornar `401`, o token esta ausente, expirado, mal assinado ou com usuario inexistente no mock.

## 8. Criar Template

Request:

```http
POST {{BASE_URL}}/api/checklist-templates
```

Authorization:

```text
Bearer Token: {{hubToken}}
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

Copie o campo `id` da resposta e cole na variavel:

```text
TEMPLATE_ID
```

## 9. Ativar Template

Request:

```http
PATCH {{BASE_URL}}/api/checklist-templates/{{TEMPLATE_ID}}/activate
```

Authorization:

```text
Bearer Token: {{hubToken}}
```

Resultado esperado:

```text
200 OK
```

## 10. Criar Draft

Request:

```http
POST {{BASE_URL}}/api/checklist-executions/drafts
```

Authorization:

```text
Bearer Token: {{hubToken}}
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

Copie o campo `id` da resposta e cole na variavel:

```text
EXECUTION_ID
```

## 11. Enviar Checklist

Request:

```http
POST {{BASE_URL}}/api/checklist-executions/{{EXECUTION_ID}}/submit
```

Authorization:

```text
Bearer Token: {{hubToken}}
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

## 12. Erros Comuns

### 401 Unauthorized

Possiveis causas:

- token nao foi enviado;
- token foi colado sem `Bearer`;
- token foi colado com `Bearer` duplicado;
- token expirou;
- secret usado no `jwt.io` esta errado;
- `sub` do token nao existe em `checklist.mock.hub.user-ids`;
- API nao foi subida com profile `mock`.

Se a resposta for:

```json
{
  "message": "Token de autenticacao e obrigatorio."
}
```

O backend nao recebeu o header `Authorization`.

Confira no Postman:

```text
Authorization > Type: Bearer Token
Token: {{hubToken}}
```

Ou confira se existe este header em `Headers`:

```text
Authorization: Bearer eyJ...
```

Se a resposta for:

```json
{
  "message": "Token do Hub invalido ou expirado."
}
```

O header chegou, mas o token falhou na validacao. Confira secret, algoritmo `HS256`, `exp`, `sub`, `userType` e `classes`.

### 403 Forbidden

O token e valido, mas o usuario nao tem permissao para a acao.

Exemplo:

- tentar criar draft com uma turma em que o usuario tem `role` `STUDENT`;
- tentar criar template com `userType` sem permissao.

### 404 Not Found

O recurso nao existe.

Exemplos:

- `roomId` nao existe em `checklist.mock.hub.room-ids`;
- `classId` nao existe em `checklist.mock.hub.class-ids`;
- `TEMPLATE_ID` ou `EXECUTION_ID` incorreto.

### 400 Bad Request

Payload invalido ou regra de negocio.

Exemplos:

- item obrigatorio sem resposta;
- item `NON_COMPLIANT` sem `observation`;
- checklist duplicado para mesma turma, sala, periodo, dia e tipo.

## 13. Fluxo Resumido

```text
1. Subir API com SPRING_PROFILES_ACTIVE=mock
2. Gerar JWT no jwt.io
3. Colar JWT em hubToken no Postman
4. GET /actuator/health com No Auth
5. GET /api/checklist-templates com Bearer Token
6. POST /api/checklist-templates
7. Copiar TEMPLATE_ID
8. PATCH /api/checklist-templates/{TEMPLATE_ID}/activate
9. POST /api/checklist-executions/drafts
10. Copiar EXECUTION_ID
11. POST /api/checklist-executions/{EXECUTION_ID}/submit
```
