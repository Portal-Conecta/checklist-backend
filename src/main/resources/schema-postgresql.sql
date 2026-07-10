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
