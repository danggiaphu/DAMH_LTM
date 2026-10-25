-- ==============================================================================
-- Migration V4: Tạo bảng system_config lưu cấu hình hạn nộp bài và trạng thái hệ thống
-- ==============================================================================

CREATE TABLE IF NOT EXISTS system_config (
    config_key TEXT PRIMARY KEY,
    config_value TEXT NOT NULL,
    updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

INSERT OR IGNORE INTO system_config (config_key, config_value) VALUES
('deadline_enabled', 'false'),
('deadline_time', ''),
('deadline_block_late', 'false'),
('gate_manual_closed', 'false');
