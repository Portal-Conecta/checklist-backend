# ADR-0019 — Dependência `portal-logging` via GitHub Packages (Autenticação Maven)

- **Status:** Aceito
- **Data:** 2026-07-11
- **Autor:** danielsismer
- **Relacionado:** [ADR-0018](0018-observabilidade-prometheus.md)

---

## Contexto

O portal padroniza logging estruturado numa biblioteca compartilhada
(`com.portal.conecta:portal-logging`), publicada no **GitHub Packages**. O GitHub Packages exige
autenticação **mesmo para pacotes públicos** — logo, qualquer build que dependa dessa lib falha
pedindo credenciais se o ambiente não estiver configurado.

---

## Decisão

Configurar a autenticação do Maven no GitHub Packages via `.mvn/settings.xml` + `.mvn/maven.config`,
lendo as credenciais de **variáveis de ambiente** (`MAVEN_USERNAME`, `MAVEN_PASSWORD`), nunca
versionadas:

```xml
<server>
  <id>portal-logging</id>
  <username>${env.MAVEN_USERNAME}</username>
  <password>${env.MAVEN_PASSWORD}</password>
</server>
```

- `MAVEN_USERNAME` = usuário do GitHub; `MAVEN_PASSWORD` = Personal Access Token (classic) com escopo
  `read:packages`.
- No docker-compose, injetado via build-arg + secret; local (IDE/CI), via variáveis de ambiente.
- Mesmo padrão usado por `core-backend` e `api-gateway`.

---

## Alternativas consideradas

| Alternativa | Por que foi descartada |
|---|---|
| Publicar a lib num repositório público sem auth (Maven Central) | Processo de publicação mais pesado; o portal já padronizou GitHub Packages. |
| Vendorizar/copiar o código de logging em cada serviço | Duplicação e drift entre serviços. |
| Commitar as credenciais no `settings.xml` | Vazamento de segredo no repositório. |

---

## Consequências

### Positivas

- Logging estruturado compartilhado e versionado; configuração consistente entre os repos.
- Segredos ficam fora do versionamento (apenas `${env.*}`).

### Pontos de atenção / negativas

- Todo ambiente de build (dev, CI, docker) precisa das duas variáveis — sem elas o build **falha de
  propósito**, com mensagem explícita.
- O PAT precisa de rotação/gestão como qualquer segredo.

---

## Referências

- `.mvn/settings.xml`, `.mvn/maven.config`
- `pom.xml` (repositório `portal-logging`, dependência `com.portal.conecta:portal-logging`)
- PR #188 (`feature/#188-config-maven-github-packages`)
