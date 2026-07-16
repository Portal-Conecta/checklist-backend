ALTER TABLE IF EXISTS checklist_template ALTER COLUMN updated_at DROP NOT NULL;
ALTER TABLE IF EXISTS checklist_template DROP CONSTRAINT IF EXISTS checklist_template_status_check;
UPDATE checklist_template
SET status = CASE WHEN active IS TRUE THEN 'ACTIVE' ELSE 'DRAFT' END
WHERE status NOT IN ('DRAFT', 'ACTIVE', 'INACTIVE');
ALTER TABLE IF EXISTS checklist_template
ADD CONSTRAINT checklist_template_status_check
CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'));

CREATE UNIQUE INDEX IF NOT EXISTS uidx_execution_no_duplicate
ON checklist_execution (class_id, room_id, period, checklist_type, (started_at::date))
WHERE status <> 'CANCELED';

ALTER TABLE IF EXISTS checklist_template
    ADD COLUMN IF NOT EXISTS template_group_id UUID;

UPDATE checklist_template
SET template_group_id = id
WHERE template_group_id IS NULL;

ALTER TABLE IF EXISTS checklist_template
    ALTER COLUMN template_group_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uidx_one_active_per_group
    ON checklist_template (template_group_id)
    WHERE status = 'ACTIVE';
    
ALTER TABLE IF EXISTS checklist_submission_window
ADD COLUMN IF NOT EXISTS class_id UUID;

ALTER TABLE IF EXISTS checklist_submission_window
DROP CONSTRAINT IF EXISTS uq_window_shift_type;

CREATE UNIQUE INDEX IF NOT EXISTS uidx_window_class_type
ON checklist_submission_window (class_id, checklist_type)
WHERE class_id IS NOT NULL;

ALTER TABLE IF EXISTS checklist_execution
ADD COLUMN IF NOT EXISTS shift VARCHAR(20);

-- ─── Função auxiliar para índices funcionais sobre colunas TIMESTAMPTZ ──────
-- Colunas TIMESTAMP (sem tz), como started_at, aceitam (col::date) direto pois
-- o cast é IMMUTABLE. Colunas TIMESTAMPTZ, como due_at e created_at, têm cast
-- para date classificado como STABLE (depende do TimeZone da sessão), o que o
-- Postgres rejeita em índice ("functions in index expression must be marked
-- IMMUTABLE"). Esta função fixa o timezone de negócio do domínio Checklist
-- (America/Sao_Paulo, igual a checklist.timezone em application.yml) e pode
-- ser legitimamente marcada IMMUTABLE, pois o timezone não é mais um parâmetro
-- variável de sessão.
CREATE OR REPLACE FUNCTION checklist_immutable_date(ts TIMESTAMPTZ)
RETURNS date
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT (ts AT TIME ZONE 'America/Sao_Paulo')::date
$$;

-- ─── Índices para queries de stats / agregação ──────────────────────────────
-- checklist_execution
-- Suporte a GROUP BY status (já existente implicitamente na constraint, mas sem índice dedicado)
CREATE INDEX IF NOT EXISTS idx_execution_status
    ON checklist_execution (status);

-- Suporte a GROUP BY checklist_type
CREATE INDEX IF NOT EXISTS idx_execution_checklist_type
    ON checklist_execution (checklist_type);

-- Classificação por grupo de itens da sala (eletrônicos, móveis, etc.)
ALTER TABLE IF EXISTS checklist_template
    ADD COLUMN IF NOT EXISTS category VARCHAR(40);

ALTER TABLE IF EXISTS checklist_execution
    ADD COLUMN IF NOT EXISTS category VARCHAR(40);

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

-- Suporte a GROUP BY shift
CREATE INDEX IF NOT EXISTS idx_execution_shift
    ON checklist_execution (shift);

-- Suporte a GROUP BY period
CREATE INDEX IF NOT EXISTS idx_execution_period
    ON checklist_execution (period);

-- Suporte a filtros temporais por submitted_at (completion-rate, avg-fill-time)
CREATE INDEX IF NOT EXISTS idx_execution_submitted_at
    ON checklist_execution (submitted_at)
    WHERE submitted_at IS NOT NULL;

-- Índice funcional por data de início para agregação diária
-- (o uidx_execution_no_duplicate já cobre (started_at::date) mas é parcial/unique;
--  este índice é não-único e cobre toda a tabela para seq scan em range queries)
CREATE INDEX IF NOT EXISTS idx_execution_started_at_date
    ON checklist_execution ((started_at::date));

-- checklist_issue
-- Suporte a GROUP BY status
CREATE INDEX IF NOT EXISTS idx_issue_status
    ON checklist_issue (status);

-- Suporte a GROUP BY priority
CREATE INDEX IF NOT EXISTS idx_issue_priority
    ON checklist_issue (priority);

-- Suporte a filtros e GROUP BY due_at (overdue, série temporal)
CREATE INDEX IF NOT EXISTS idx_issue_due_at
    ON checklist_issue (due_at);

-- Índice funcional por data de prazo para agregação diária
-- due_at é TIMESTAMPTZ: usa checklist_immutable_date() em vez de (due_at::date)
CREATE INDEX IF NOT EXISTS idx_issue_due_at_date
    ON checklist_issue (checklist_immutable_date(due_at));

-- Suporte a filtro WHERE resolved_at IS NOT NULL (resolution-rate, avg-resolution-time)
CREATE INDEX IF NOT EXISTS idx_issue_resolved_at
    ON checklist_issue (resolved_at)
    WHERE resolved_at IS NOT NULL;

-- Suporte a GROUP BY item_key (top failing items)
CREATE INDEX IF NOT EXISTS idx_issue_item_key
    ON checklist_issue (item_key);

-- Suporte ao JOIN com checklist_execution para GROUP BY checklist_type
CREATE INDEX IF NOT EXISTS idx_issue_execution_id
    ON checklist_issue (checklist_execution_id);

-- checklist_template
-- Suporte a GROUP BY status
CREATE INDEX IF NOT EXISTS idx_template_status
    ON checklist_template (status);

-- Suporte a GROUP BY active
CREATE INDEX IF NOT EXISTS idx_template_active
    ON checklist_template (active);

-- Suporte a GROUP BY created_at::date
-- created_at é TIMESTAMPTZ: usa checklist_immutable_date() em vez de (created_at::date)
CREATE INDEX IF NOT EXISTS idx_template_created_at_date
    ON checklist_template (checklist_immutable_date(created_at));

-- Suporte a GROUP BY template_group_id (versões por grupo)
CREATE INDEX IF NOT EXISTS idx_template_group_id
    ON checklist_template (template_group_id);
