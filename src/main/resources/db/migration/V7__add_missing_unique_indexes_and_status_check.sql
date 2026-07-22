-- Fecha um gap entre o schema real de producao (Flyway puro, ddl-auto=none) e o
-- schema-postgresql.sql (aplicado apenas no profile dev via spring.sql.init).
-- Estes objetos existiam SOMENTE no schema-postgresql.sql e nunca foram
-- portados para uma migration Flyway, entao producao provavelmente nao os tem:
--
--   - uidx_execution_no_duplicate: indice unico parcial que o proprio
--     docs/riscos.md lista como mitigacao da race condition de duplicidade
--     ("Mitigado - indice parcial uidx_execution_no_duplicate + handler 409").
--     Sem ele, a checagem de duplicidade depende so da aplicacao
--     (existsDuplicateChecklist), que tem uma janela de corrida (TOCTOU).
--   - uidx_one_active_per_group: garante no maximo um template ACTIVE por
--     template_group_id.
--   - checklist_template_status_check: valida os valores de status do template.
--
-- ATENCAO antes de aplicar em producao: se ja existirem linhas duplicadas
-- (mesma class_id/room_id/period/checklist_type/dia, status <> CANCELED) ou
-- mais de um template ACTIVE por grupo, a criacao dos indices unicos abaixo
-- vai falhar. Rode uma verificacao de duplicidade contra o banco de producao
-- antes de mergear/implantar esta migration.

ALTER TABLE checklist_template
    DROP CONSTRAINT IF EXISTS checklist_template_status_check;

UPDATE checklist_template
SET status = CASE WHEN active IS TRUE THEN 'ACTIVE' ELSE 'DRAFT' END
WHERE status NOT IN ('DRAFT', 'ACTIVE', 'INACTIVE');

ALTER TABLE checklist_template
    ADD CONSTRAINT checklist_template_status_check
    CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'));

CREATE UNIQUE INDEX IF NOT EXISTS uidx_execution_no_duplicate
    ON checklist_execution (class_id, room_id, period, checklist_type, (started_at::date))
    WHERE status <> 'CANCELED';

CREATE UNIQUE INDEX IF NOT EXISTS uidx_one_active_per_group
    ON checklist_template (template_group_id)
    WHERE status = 'ACTIVE';
