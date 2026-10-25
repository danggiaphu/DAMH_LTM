-- ==============================================================================
-- Migration V1: Tạo các bảng quản lý sinh viên, bài nộp và theo dõi phiên bản
-- ==============================================================================

-- 1. Bảng lưu trữ lịch sử Migration
CREATE TABLE IF NOT EXISTS schema_version (
    version INTEGER PRIMARY KEY,
    description TEXT NOT NULL,
    applied_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 2. Bảng thông tin Sinh viên
CREATE TABLE IF NOT EXISTS students (
    student_id TEXT PRIMARY KEY,
    full_name TEXT NOT NULL,
    class_name TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 3. Bảng thông tin Bài tập đã nộp
CREATE TABLE IF NOT EXISTS submissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id TEXT NOT NULL,
    file_name TEXT NOT NULL,
    file_type TEXT,
    file_size INTEGER NOT NULL,
    submission_time TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'Thành công',
    saved_path TEXT NOT NULL,
    note TEXT,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- 4. Tạo các chỉ mục (Index) để tối ưu tốc độ tra cứu & tìm kiếm
CREATE INDEX IF NOT EXISTS idx_submissions_student_id ON submissions(student_id);
CREATE INDEX IF NOT EXISTS idx_submissions_time ON submissions(submission_time);
CREATE INDEX IF NOT EXISTS idx_submissions_file_type ON submissions(file_type);
CREATE INDEX IF NOT EXISTS idx_students_class ON students(class_name);
