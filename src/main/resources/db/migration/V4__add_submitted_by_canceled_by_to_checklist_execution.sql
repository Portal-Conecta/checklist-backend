ALTER TABLE checklist_execution ADD COLUMN submitted_by UUID;
ALTER TABLE checklist_execution ADD COLUMN canceled_by UUID;
