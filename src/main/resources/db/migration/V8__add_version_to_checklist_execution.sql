-- Adiciona a coluna de versionamento otimista (@Version) em checklist_execution.
-- Protege contra escrita concorrente no mesmo registro (ex: autosave de rascunho
-- disparado por dois dispositivos/sessoes ao mesmo tempo) -- sem esta coluna, a
-- segunda escrita simplesmente sobrescreve a primeira silenciosamente.
ALTER TABLE checklist_execution
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
