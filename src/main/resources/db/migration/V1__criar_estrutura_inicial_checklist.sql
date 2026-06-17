
CREATE TABLE checklist_template (
    id UUID PRIMARY KEY,
    template_group_id UUID NOT NULL,
    room_id UUID NOT NULL,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(250) NOT NULL,
    version INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL,
    schema_json JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ
);


CREATE TABLE checklist_execution (
    id UUID PRIMARY KEY,
    checklist_template_id UUID NOT NULL,
    room_id UUID NOT NULL,
    class_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    answers_json JSONB NOT NULL,
    compliance_score DECIMAL(5, 2),
    checklist_type VARCHAR(30) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP,
    period VARCHAR(20) NOT NULL,
    shift VARCHAR(20) NOT NULL,
    CONSTRAINT fk_execution_template FOREIGN KEY (checklist_template_id) REFERENCES checklist_template(id)
);


CREATE TABLE checklist_issue (
    id UUID PRIMARY KEY,
    checklist_execution_id UUID NOT NULL,
    assigned_user_id UUID NOT NULL,
    item_key VARCHAR(150) NOT NULL,
    item_title_snapshot VARCHAR(150) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    due_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ,
    CONSTRAINT fk_issue_execution FOREIGN KEY (checklist_execution_id) REFERENCES checklist_execution(id) ON DELETE CASCADE
);


CREATE TABLE checklist_submission_window (
    id UUID PRIMARY KEY,
    class_id UUID NOT NULL,
    shift VARCHAR(20) NOT NULL,
    checklist_type VARCHAR(30) NOT NULL,
    open_at TIME NOT NULL,
    duration_minutes INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    CONSTRAINT uq_window_class_type UNIQUE (class_id, checklist_type)
);


CREATE INDEX idx_template_room ON checklist_template(room_id);
CREATE INDEX idx_execution_room_class ON checklist_execution(room_id, class_id);
CREATE INDEX idx_issue_execution ON checklist_issue(checklist_execution_id);