# Documentação — Checklist Backend

> Documentação **oficial e versionada** do microserviço Checklist (Portal Conecta).
> Tudo aqui viaja com o código e é revisado no PR. Esta é a **fonte de verdade**.

- **Repositório:** https://github.com/Portal-Conecta/checklist-backend
- **Branch principal:** `develop`
- **Stack:** Java 21 · Spring Boot 4.x · PostgreSQL · OpenFeign · RabbitMQ

---

## Por onde começar

1. [Visão de arquitetura](arquitetura/visao-geral.md) — módulos, camadas e convenções de pacote.
2. [Fluxo operacional](dominio/fluxo-operacional.md) — templates, execuções, submit, issues, janela e busca.
3. [Decisões de arquitetura (ADRs)](adr/README.md) — por que o sistema é como é.
4. [Registro de riscos](riscos.md) — riscos abertos e pendências de contrato com o Hub.
5. [CHANGELOG](CHANGELOG.md) — o que mudou e quando.

---

## Estrutura

| Pasta | Conteúdo |
|---|---|
| `adr/` | Architecture Decision Records — decisões formais, imutáveis, numeradas |
| `arquitetura/` | Visão de arquitetura viva (mapa de pacotes e camadas) |
| `dominio/` | Fluxo operacional e regras de negócio do módulo |
| `riscos.md` | Riscos atuais e ações recomendadas |
| `CHANGELOG.md` | Histórico de mudanças relevantes |
| `releases/` | Notas de release (ex.: `v0.1.0`) |
| `pr-175-bootstrap-local.md` | Nota de PR — inicialização local com Docker Compose |

---

## ADR vs. documento vivo

- Um **ADR** registra uma **decisão num ponto no tempo**. Não se reescreve depois de aceito —
  se a decisão muda, cria-se um **novo ADR** que substitui o anterior (`Substituído por ADR-XXXX`).
- A **visão de arquitetura** e o **fluxo de domínio** são **documentos vivos**: refletem o
  estado atual do código e são atualizados a cada mudança relevante.

---

## Relação com o vault (`back-recorders`)

O vault Obsidian `back-recorders` continua existindo como **espaço de notas de trabalho**:
rascunhos de discussão, handoff para LLM e histórico datado (`archive/`). Ele **não é fonte de
verdade** para implementação. Toda decisão que se consolida deve ser promovida para um ADR aqui.

---

## Como contribuir com a documentação

- Mudou uma decisão de arquitetura ou regra crítica? Abra um **ADR novo** (ver [processo](adr/README.md)).
- Mudou a estrutura/fluxo sem virar decisão nova? Atualize o **documento vivo** correspondente.
- Toda alteração de documentação entra **no mesmo PR** da mudança de código que a motivou.
