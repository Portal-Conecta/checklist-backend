# #175 chore: automatiza inicializacao local com Docker Compose

## O que foi feito

- Adicionado Maven Wrapper versionado com Maven `3.9.16`.
- Corrigido o `EnvFileLoader` para carregar `.env` em vez de `..env`.
- Adicionada a dependencia `spring-boot-docker-compose`.
- Configurado o profile `local` para iniciar o Docker Compose automaticamente.
- Adicionado health check ao PostgreSQL antes da inicializacao da API.
- Padronizada a porta `5433` para o banco local, reduzindo conflitos com instalacoes existentes.
- Tornado o Grafana opcional pelo profile `observability`.
- Atualizado `.env.example` com configuracoes locais de banco e sem expor chave JWT.
- Atualizado README com fluxo de primeira execucao e gerenciamento manual da infraestrutura.

## Como testar

1. Tenha o Docker Desktop aberto.
2. Crie o arquivo local:

```powershell
Copy-Item .env.example .env
```

3. Configure `JWT_SECRET` no `.env` com a chave Base64 HS256 usada pelo Hub local.
4. Execute a aplicacao:

```powershell
.\mvnw.cmd spring-boot:run
```

5. Verifique os logs:
   - PostgreSQL deve iniciar pelo Docker Compose;
   - o container deve ficar com status `healthy`;
   - a API deve iniciar em `http://localhost:8083`.

6. Execute os testes:

```powershell
.\mvnw.cmd test
```

## Observações

- O PostgreSQL local permanece ativo apos encerrar a API.
- Para encerrar a infraestrutura:

```powershell
docker compose down
```

- Para remover tambem os dados locais:

```powershell
docker compose down -v
```

- O Grafana nao inicia por padrao. Para inicia-lo:

```powershell
docker compose --profile observability up -d
```

Closes #175
