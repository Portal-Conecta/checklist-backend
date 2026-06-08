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
