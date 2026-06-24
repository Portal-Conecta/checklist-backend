ALTER TABLE checklist_execution
    ADD COLUMN IF NOT EXISTS class_name         VARCHAR(255),
    ADD COLUMN IF NOT EXISTS representative1_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS representative2_name VARCHAR(255);
