-- ==============================================================================
-- Script DDL & DML tham khảo cho cơ sở dữ liệu MySQL (nếu cần triển khai MySQL)
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS damh_lan_submission CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE damh_lan_submission;

-- 1. Bảng lưu trữ lịch sử Migration
CREATE TABLE IF NOT EXISTS schema_version (
    version INT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng thông tin Sinh viên
CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(50) PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    class_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. Bảng thông tin Bài tập đã nộp
CREATE TABLE IF NOT EXISTS submissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(20),
    file_size BIGINT NOT NULL,
    submission_time DATETIME NOT NULL,
    status VARCHAR(50) DEFAULT 'Thành công',
    saved_path VARCHAR(500) NOT NULL,
    note TEXT,
    file_hash VARCHAR(64),
    CONSTRAINT fk_submission_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);

-- 4. Bảng cấu hình hệ thống và Hạn nộp bài
CREATE TABLE IF NOT EXISTS system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 5. Chỉ mục tối ưu
CREATE INDEX idx_submissions_student_id ON submissions(student_id);
CREATE INDEX idx_submissions_time ON submissions(submission_time);
CREATE INDEX idx_submissions_file_type ON submissions(file_type);
CREATE INDEX idx_submissions_file_hash ON submissions(file_hash);
CREATE INDEX idx_students_class ON students(class_name);

-- 5. Dữ liệu mẫu
INSERT IGNORE INTO students (student_id, full_name, class_name) VALUES
('B21DCCN001', 'Nguyễn Văn An', 'D21CQCN01-N'),
('B21DCCN002', 'Trần Thị Bình', 'D21CQCN01-N'),
('B21DCCN003', 'Lê Hoàng Cường', 'D21CQCN02-N'),
('B21DCCN004', 'Phạm Minh Đức', 'D21CQCN02-N'),
('B21DCCN005', 'Võ Thu Hương', 'D21CQCN03-N');
