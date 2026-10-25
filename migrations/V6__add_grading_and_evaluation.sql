-- ==============================================================================
-- Migration V6: Thêm các trường Điểm số, Đánh giá / Nhận xét và Thời gian chấm bài
-- ==============================================================================

ALTER TABLE submissions ADD COLUMN score REAL;
ALTER TABLE submissions ADD COLUMN feedback TEXT;
ALTER TABLE submissions ADD COLUMN graded_at TEXT;

CREATE INDEX IF NOT EXISTS idx_submissions_score ON submissions(score);
