-- ==============================================================================
-- Migration V5: Chuẩn hóa toàn bộ dữ liệu sang chữ IN HOA (UPPERCASE)
-- Đảm bảo đồng bộ thông tin MSSV, Họ tên và Lớp giữa Client, Server và Database
-- ==============================================================================

-- 1. Chuẩn hóa dữ liệu bảng sinh viên
UPDATE students 
SET student_id = UPPER(TRIM(student_id)),
    full_name = UPPER(TRIM(full_name)),
    class_name = UPPER(TRIM(class_name));

-- 2. Chuẩn hóa dữ liệu bảng bài nộp
UPDATE submissions 
SET student_id = UPPER(TRIM(student_id)),
    file_type = UPPER(TRIM(file_type));
