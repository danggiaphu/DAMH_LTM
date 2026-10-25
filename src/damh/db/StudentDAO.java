package damh.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Sinh viên
 */
public class StudentDAO {

    /**
     * Thêm mới hoặc cập nhật thông tin sinh viên khi nộp bài
     */
    public static void upsertStudent(String studentId, String fullName, String className) throws SQLException {
        if (studentId == null || studentId.trim().isEmpty()) return;

        String safeId = studentId.trim().toUpperCase();
        String safeName = fullName != null ? fullName.trim().toUpperCase() : "";
        String safeClass = className != null ? className.trim().toUpperCase() : "";

        Connection conn = DatabaseManager.getConnection();

        // 1. Thử cập nhật trước nếu sinh viên đã tồn tại
        String updateSql = "UPDATE students SET full_name = ?, class_name = ?, updated_at = datetime('now', 'localtime') WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, safeName);
            ps.setString(2, safeClass);
            ps.setString(3, safeId);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                return;
            }
        }

        // 2. Nếu chưa có, tiến hành chèn mới
        String insertSql = "INSERT INTO students (student_id, full_name, class_name) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, safeId);
            ps.setString(2, safeName);
            ps.setString(3, safeClass);
            ps.executeUpdate();
        }
    }

    /**
     * Lấy toàn bộ danh sách sinh viên kèm thống kê số bài nộp, số bài đã chấm và điểm trung bình
     */
    public static List<StudentModel> getAllStudentsWithStats() throws SQLException {
        List<StudentModel> list = new ArrayList<>();
        String sql = "SELECT s.student_id, s.full_name, s.class_name, s.created_at, s.updated_at, " +
                     "COUNT(sub.id) AS sub_count, MAX(sub.submission_time) AS last_sub, " +
                     "AVG(sub.score) AS avg_score, COUNT(sub.score) AS graded_count " +
                     "FROM students s " +
                     "LEFT JOIN submissions sub ON s.student_id = sub.student_id " +
                     "GROUP BY s.student_id, s.full_name, s.class_name, s.created_at, s.updated_at " +
                     "ORDER BY s.student_id ASC";

        Connection conn = DatabaseManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String id = rs.getString("student_id");
                String name = rs.getString("full_name");
                String clazz = rs.getString("class_name");
                String createdAt = rs.getString("created_at");
                String updatedAt = rs.getString("updated_at");
                int count = rs.getInt("sub_count");
                String lastSub = rs.getString("last_sub");
                Double avgScore = rs.getObject("avg_score") != null ? rs.getDouble("avg_score") : null;
                int gradedCount = rs.getInt("graded_count");

                list.add(new StudentModel(id, name, clazz, createdAt, updatedAt, count, lastSub, avgScore, gradedCount));
            }
        }
        return list;
    }

    /**
     * Tìm kiếm sinh viên theo từ khóa (MSSV, Họ tên hoặc Lớp)
     */
    public static List<StudentModel> searchStudents(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudentsWithStats();
        }
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        String sql = "SELECT s.student_id, s.full_name, s.class_name, s.created_at, s.updated_at, " +
                     "COUNT(sub.id) AS sub_count, MAX(sub.submission_time) AS last_sub, " +
                     "AVG(sub.score) AS avg_score, COUNT(sub.score) AS graded_count " +
                     "FROM students s " +
                     "LEFT JOIN submissions sub ON s.student_id = sub.student_id " +
                     "WHERE LOWER(s.student_id) LIKE ? OR LOWER(s.full_name) LIKE ? OR LOWER(s.class_name) LIKE ? " +
                     "GROUP BY s.student_id, s.full_name, s.class_name, s.created_at, s.updated_at " +
                     "ORDER BY s.student_id ASC";

        List<StudentModel> list = new ArrayList<>();
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Double avgScore = rs.getObject("avg_score") != null ? rs.getDouble("avg_score") : null;
                    int gradedCount = rs.getInt("graded_count");
                    list.add(new StudentModel(
                            rs.getString("student_id"),
                            rs.getString("full_name"),
                            rs.getString("class_name"),
                            rs.getString("created_at"),
                            rs.getString("updated_at"),
                            rs.getInt("sub_count"),
                            rs.getString("last_sub"),
                            avgScore,
                            gradedCount
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Tìm thông tin 1 sinh viên theo MSSV
     */
    public static StudentModel findStudentById(String studentId) throws SQLException {
        String sql = "SELECT student_id, full_name, class_name, created_at, updated_at FROM students WHERE student_id = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StudentModel(
                            rs.getString("student_id"),
                            rs.getString("full_name"),
                            rs.getString("class_name"),
                            rs.getString("created_at"),
                            rs.getString("updated_at"),
                            0,
                            null
                    );
                }
            }
        }
        return null;
    }
}
