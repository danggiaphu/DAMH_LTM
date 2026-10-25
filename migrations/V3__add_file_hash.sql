-- ==============================================================================
-- Migration V3: Thêm cột file_hash (SHA-256) vào bảng submissions
-- ==============================================================================

ALTER TABLE submissions ADD COLUMN file_hash TEXT;
CREATE INDEX IF NOT EXISTS idx_submissions_file_hash ON submissions(file_hash);
