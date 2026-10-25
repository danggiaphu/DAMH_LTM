-- ==============================================================================
-- Migration V2: Nạp dữ liệu mẫu sinh viên phục vụ kiểm thử hệ thống
-- ==============================================================================

INSERT OR IGNORE INTO students (student_id, full_name, class_name) VALUES
('B21DCCN001', 'Nguyễn Văn An', 'D21CQCN01-N'),
('B21DCCN002', 'Trần Thị Bình', 'D21CQCN01-N'),
('B21DCCN003', 'Lê Hoàng Cường', 'D21CQCN02-N'),
('B21DCCN004', 'Phạm Minh Đức', 'D21CQCN02-N'),
('B21DCCN005', 'Võ Thu Hương', 'D21CQCN03-N');
