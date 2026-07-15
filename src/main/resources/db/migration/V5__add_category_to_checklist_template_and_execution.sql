-- Classificação do checklist por grupo de itens da sala
-- (ELETRONICOS, MOVEIS, ILUMINACAO, CLIMATIZACAO, INFRAESTRUTURA, HIGIENE, GERAL).
-- Distinto de checklist_type (ARRIVAL / POST_BREAK).

ALTER TABLE checklist_template
    ADD COLUMN category VARCHAR(40);

ALTER TABLE checklist_execution
    ADD COLUMN category VARCHAR(40);

-- Legado: evita NOT NULL sem valor para registros já existentes.
UPDATE checklist_template SET category = 'GERAL' WHERE category IS NULL;
UPDATE checklist_execution SET category = 'GERAL' WHERE category IS NULL;

ALTER TABLE checklist_template
    ALTER COLUMN category SET NOT NULL;

ALTER TABLE checklist_execution
    ALTER COLUMN category SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_template_category
    ON checklist_template (category);

CREATE INDEX IF NOT EXISTS idx_execution_category
    ON checklist_execution (category);

CREATE INDEX IF NOT EXISTS idx_execution_category_status
    ON checklist_execution (category, status);
