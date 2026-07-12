-- Indices de suporte aos endpoints de agregacao (stats/dashboard).
-- Estes indices existiam apenas no schema-postgresql.sql (aplicado somente no
-- profile dev via spring.sql.init). Como prod/infra usa Flyway (ddl-auto=none /
-- validate), sem esta migration as agregacoes rodariam sem indice em producao
-- (full scan). Espelham exatamente o schema-postgresql.sql; IF NOT EXISTS torna
-- a migration idempotente.

-- checklist_execution
CREATE INDEX IF NOT EXISTS idx_execution_status
    ON checklist_execution (status);

CREATE INDEX IF NOT EXISTS idx_execution_checklist_type
    ON checklist_execution (checklist_type);

CREATE INDEX IF NOT EXISTS idx_execution_shift
    ON checklist_execution (shift);

CREATE INDEX IF NOT EXISTS idx_execution_period
    ON checklist_execution (period);

CREATE INDEX IF NOT EXISTS idx_execution_submitted_at
    ON checklist_execution (submitted_at)
    WHERE submitted_at IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_execution_started_at_date
    ON checklist_execution ((started_at::date));

-- checklist_issue
CREATE INDEX IF NOT EXISTS idx_issue_status
    ON checklist_issue (status);

CREATE INDEX IF NOT EXISTS idx_issue_priority
    ON checklist_issue (priority);

CREATE INDEX IF NOT EXISTS idx_issue_due_at
    ON checklist_issue (due_at);

CREATE INDEX IF NOT EXISTS idx_issue_due_at_date
    ON checklist_issue ((due_at::date));

CREATE INDEX IF NOT EXISTS idx_issue_resolved_at
    ON checklist_issue (resolved_at)
    WHERE resolved_at IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_issue_item_key
    ON checklist_issue (item_key);

CREATE INDEX IF NOT EXISTS idx_issue_execution_id
    ON checklist_issue (checklist_execution_id);

-- checklist_template
CREATE INDEX IF NOT EXISTS idx_template_status
    ON checklist_template (status);

CREATE INDEX IF NOT EXISTS idx_template_active
    ON checklist_template (active);

CREATE INDEX IF NOT EXISTS idx_template_created_at_date
    ON checklist_template ((created_at::date));

CREATE INDEX IF NOT EXISTS idx_template_group_id
    ON checklist_template (template_group_id);
