# Guia De Testes No Postman Com JWT Mock

Este guia centraliza os dados JSON usados para testar a Checklist API enquanto o Hub ainda nao esta disponivel. A API valida um JWT real, mas consulta usuarios, salas e turmas em providers mockados configurados no projeto.

> Importante: os JSONs podem mudar conforme as regras do backend evoluirem. Sempre confira se os IDs usados aqui continuam existindo em `src/main/resources/application-mock.properties`.

## 1. Subir A API Em Modo Mock

Na raiz do projeto:

```powershell
docker compose up -d
```

Depois rode a API com o profile `mock`:

```powershell
$env:SPRING_PROFILES_ACTIVE="mock"
$env:SERVER_PORT="8083"
$env:JWT_SECRET="MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
mvn spring-boot:run
```

Se estiver usando outra porta, ajuste a variavel `BASE_URL` no Postman.

Health check:

```http
GET http://localhost:8083/actuator/health
```

Esse endpoint nao precisa de token.

## 2. Dados Mock Disponiveis

Os dados abaixo existem no profile `mock`.

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

## 3. Configurar Ambiente No Postman

Crie um environment chamado `Checklist Local Mock`.

```text
BASE_URL=http://localhost:8083
JWT_SECRET=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=
USER_ID=44444444-4444-4444-4444-444444444444
ROOM_ID=11111111-1111-1111-1111-111111111111
TEACHER_CLASS_ID=8f8e8d8c-8b8a-8f8e-8d8c-8b8a8f8e8d8c
STUDENT_CLASS_ID=1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d
USER_TYPE=SENAI
TEACHER_CLASS_ROLE=TEACHER
STUDENT_CLASS_ROLE=STUDENT
```

## 4. Autenticacao JWT

Rotas privadas precisam receber o header:

```http
Authorization: Bearer {{hubToken}}
```

O backend nao aceita o payload JSON puro. Ele precisa receber um JWT assinado no formato:

```text
header.payload.signature
```

### Gerar JWT Automaticamente No Postman

Na collection do Postman, va em `Authorization`:

```text
Type: Bearer Token
Token: {{hubToken}}
```

Depois va em `Pre-request Script` e cole:

```javascript
const secret = pm.environment.get("JWT_SECRET");
const now = Math.floor(Date.now() / 1000);

function base64Url(wordArray) {
  return CryptoJS.enc.Base64.stringify(wordArray)
    .replace(/=+$/g, "")
    .replace(/\+/g, "-")
    .replace(/\//g, "_");
}

const header = {
  alg: "HS256",
  typ: "JWT"
};

const payload = {
  jti: pm.variables.replaceIn("{{$guid}}"),
  sub: pm.environment.get("USER_ID"),
  userType: pm.environment.get("USER_TYPE"),
  classes: [
    {
      classId: pm.environment.get("TEACHER_CLASS_ID"),
      role: pm.environment.get("TEACHER_CLASS_ROLE")
    },
    {
      classId: pm.environment.get("STUDENT_CLASS_ID"),
      role: pm.environment.get("STUDENT_CLASS_ROLE")
    }
  ],
  iat: now,
  exp: now + 900
};

const encodedHeader = base64Url(CryptoJS.enc.Utf8.parse(JSON.stringify(header)));
const encodedPayload = base64Url(CryptoJS.enc.Utf8.parse(JSON.stringify(payload)));
const unsignedToken = `${encodedHeader}.${encodedPayload}`;
const signature = base64Url(CryptoJS.HmacSHA256(unsignedToken, CryptoJS.enc.Base64.parse(secret)));

pm.environment.set("hubToken", `${unsignedToken}.${signature}`);
pm.environment.set("hubTokenPayload", JSON.stringify(payload, null, 2));
```

Esse script gera um token novo antes de cada requisicao, evitando erro por `exp` expirado.

### Payload Base Do Token

Este e o payload gerado pelo script, com `iat` e `exp` dinamicos:

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

Se for gerar manualmente no jwt.io:

- algoritmo: `HS256`;
- secret Base64: `MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=`;
- marque a opcao de secret em Base64, se a ferramenta oferecer essa opcao;
- se nao marcar Base64, use o secret decodificado: `0123456789abcdef0123456789abcdef`;
- atualize `iat` e `exp` para datas validas.

## 5. Validar Se O Token Esta Funcionando

```http
GET {{BASE_URL}}/api/checklist-templates
Authorization: Bearer {{hubToken}}
```

Resultado esperado:

```text
200 OK
```

Se retornar `401`, o problema esta na autenticacao/token. Se retornar `403`, o token e valido, mas o usuario nao tem permissao para a acao.

## 6. Criar Template De Checklist

O que testa:

- autenticacao JWT;
- permissao de gestao de template por `SENAI`;
- existencia da sala no provider mock;
- validacao das keys do schema.

Rota:

```http
POST {{BASE_URL}}/api/checklist-templates
Authorization: Bearer {{hubToken}}
Content-Type: application/json
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

Observacao: o backend atual cria o template como `DRAFT`. Por isso o payload de criacao nao usa `isActive`; a ativacao acontece na rota de activate.

No Postman, adicione este script na aba `Tests` da requisicao para salvar o ID:

```javascript
const body = pm.response.json();
pm.environment.set("TEMPLATE_ID", body.id);
```

## 7. Ativar Template

O que testa:

- permissao de gestao de template;
- fluxo que ativa o template e desativa outros templates ativos da mesma sala.

Rota:

```http
PATCH {{BASE_URL}}/api/checklist-templates/{{TEMPLATE_ID}}/activate
Authorization: Bearer {{hubToken}}
```

Resultado esperado:

```text
200 OK
```

## 8. Criar Draft De Checklist

O que testa:

- template precisa estar ativo;
- sala precisa existir no mock;
- turma precisa existir no mock;
- usuario precisa ter `role` `TEACHER` ou `REPRESENTATIVE` na turma informada;
- nao pode existir checklist duplicado para mesma turma, sala, periodo, dia e tipo.

Rota:

```http
POST {{BASE_URL}}/api/checklist-executions/drafts
Authorization: Bearer {{hubToken}}
Content-Type: application/json
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

No Postman, adicione este script na aba `Tests` da requisicao para salvar o ID:

```javascript
const body = pm.response.json();
pm.environment.set("EXECUTION_ID", body.id);
```

## 9. Enviar Checklist

O que testa:

- checklist precisa estar em `DRAFT`;
- somente o usuario que criou o draft pode enviar;
- todos os itens obrigatorios precisam estar respondidos;
- item `COMPLIANT` nao exige observacao;
- item `NON_COMPLIANT` exige observacao;
- item `NON_COMPLIANT` gera issue.

Rota:

```http
POST {{BASE_URL}}/api/checklist-executions/{{EXECUTION_ID}}/submit
Authorization: Bearer {{hubToken}}
Content-Type: application/json
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

## 10. Testes Negativos Recomendados

### Token Sem Bearer

Remova o token da requisicao.

Resultado esperado:

```text
401 Unauthorized
```

### Usuario Inexistente

Troque `USER_ID` para um UUID que nao esta em `checklist.mock.hub.user-ids`.

Resultado esperado:

```text
401 Unauthorized
```

### Sala Inexistente

Troque `roomId` para um UUID que nao esta em `checklist.mock.hub.room-ids`.

Resultado esperado:

```text
404 Not Found
```

### Turma Sem Permissao

Troque o `classId` do draft para:

```text
1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d
```

No token base, essa turma tem `role` `STUDENT`. Como `STUDENT` nao pode criar draft, o esperado e:

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

## 11. Resumo Dos Status Mais Comuns

```text
200 OK       -> requisicao executada com sucesso
201 Created  -> recurso criado
400 Bad Request -> regra de negocio ou payload invalido
401 Unauthorized -> problema de token/autenticacao
403 Forbidden -> token valido, mas sem permissao
404 Not Found -> recurso nao encontrado
```
