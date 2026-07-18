-- Adiciona a coluna de versionamento otimista (@Version) usada pela maquina de
-- estados de ChecklistIssue. Sem esta coluna, qualquer transicao (start, resolve,
-- validate, reopen, restart, cancel) falha em runtime no Postgres, pois o Hibernate
-- gera UPDATE ... SET version = ? WHERE id = ? AND version = ?.
ALTER TABLE checklist_issue
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
