package damh.db;

import damh.SubmissionModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Bài nộp của Sinh viên
 */
public class SubmissionDAO {

    /**
     * Thêm bản ghi bài nộp mới vào cơ sở dữ liệu
     */
    public static int insertSubmission(SubmissionModel sub) throws SQLException {
        String sql = "INSERT INTO submissions (student_id, file_name, file_type, file_size, submission_time, status, saved_path, file_hash) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sub.getStudentId() != null ? sub.getStudentId().toUpperCase() : "");
            ps.setString(2, sub.getFileName());
            ps.setString(3, sub.getFileType() != null ? sub.getFileType().toUpperCase() : "");
            ps.setLong(4, sub.getFileSize());
            ps.setString(5, sub.getSubmissionTime());
            ps.setString(6, sub.getStatus());
            ps.setString(7, sub.getSavedPath());
            ps.setString(8, sub.getFileHash());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        sub.setId(id);
                        return id;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Lấy danh sách tất cả các bài nộp kèm thông tin sinh viên từ cơ sở dữ liệu
     */
    public static List<SubmissionModel> getAllSubmissions() throws SQLException {
        List<SubmissionModel> list = new ArrayList<>();
        String sql = "SELECT sub.id, sub.student_id, s.full_name, s.class_name, sub.file_name, " +
                     "sub.file_type, sub.file_size, sub.submission_time, sub.status, sub.saved_path, sub.file_hash, " +
                     "sub.score, sub.feedback, sub.graded_at " +
                     "FROM submissions sub " +
                     "LEFT JOIN students s ON sub.student_id = s.student_id " +
                     "ORDER BY sub.id ASC";

        Connection conn = DatabaseManager.getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String studentId = rs.getString("student_id");
                String studentName = rs.getString("full_name");
                String studentClass = rs.getString("class_name");
                String fileName = rs.getString("file_name");
                String fileType = rs.getString("file_type");
                long fileSize = rs.getLong("file_size");
                String subTime = rs.getString("submission_time");
                String status = rs.getString("status");
                String savedPath = rs.getString("saved_path");
                String fileHash = rs.getString("file_hash");
                Double score = rs.getObject("score") != null ? rs.getDouble("score") : null;
                String feedback = rs.getString("feedback");
                String gradedAt = rs.getString("graded_at");

                if (studentName == null) studentName = "(Chưa đăng ký)";
                if (studentClass == null) studentClass = "-";

                SubmissionModel model = new SubmissionModel(
                        id, studentId, studentName, studentClass, fileName, fileType, fileSize, subTime, status, savedPath, fileHash,
                        score, feedback, gradedAt
                );
                list.add(model);
            }
        }
        return list;
    }

    /**
     * Lấy các bài nộp theo Mã số sinh viên
     */
    public static List<SubmissionModel> getSubmissionsByStudent(String studentId) throws SQLException {
        List<SubmissionModel> list = new ArrayList<>();
        String sql = "SELECT sub.id, sub.student_id, s.full_name, s.class_name, sub.file_name, " +
                     "sub.file_type, sub.file_size, sub.submission_time, sub.status, sub.saved_path, sub.file_hash, " +
                     "sub.score, sub.feedback, sub.graded_at " +
                     "FROM submissions sub " +
                     "LEFT JOIN students s ON sub.student_id = s.student_id " +
                     "WHERE sub.student_id = ? " +
                     "ORDER BY sub.id DESC";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Double score = rs.getObject("score") != null ? rs.getDouble("score") : null;
                    list.add(new SubmissionModel(
                            rs.getInt("id"),
                            rs.getString("student_id"),
                            rs.getString("full_name"),
                            rs.getString("class_name"),
                            rs.getString("file_name"),
                            rs.getString("file_type"),
                            rs.getLong("file_size"),
                            rs.getString("submission_time"),
                            rs.getString("status"),
                            rs.getString("saved_path"),
                            rs.getString("file_hash"),
                            score,
                            rs.getString("feedback"),
                            rs.getString("graded_at")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Cập nhật điểm số và nhận xét / đánh giá bài tập
     */
    public static boolean updateGradeAndFeedback(int id, Double score, String feedback) throws SQLException {
        String sql = "UPDATE submissions SET score = ?, feedback = ?, graded_at = datetime('now', 'localtime') WHERE id = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (score != null) {
                ps.setDouble(1, score);
            } else {
                ps.setNull(1, Types.REAL);
            }
            ps.setString(2, feedback != null ? feedback.trim() : "");
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa một bài nộp khỏi hệ thống theo ID
     */
    public static boolean deleteSubmission(int id) throws SQLException {
        String sql = "DELETE FROM submissions WHERE id = ?";
        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lớp đóng gói thống kê điểm số bài nộp
     */
    public static class GradeStats {
        public int totalSubmissions = 0;
        public int gradedCount = 0;
        public int ungradedCount = 0;
        public double averageScore = 0.0;
        public double maxScore = 0.0;
        public double minScore = 0.0;
        public int countExcellent = 0;  // >= 8.5
        public int countGood = 0;       // 7.0 - 8.4
        public int countAverage = 0;    // 5.0 - 6.9
        public int countWeak = 0;       // < 5.0
    }

    /**
     * Tính toán tổng quan phổ điểm bài nộp
     */
    public static GradeStats getGradeStats() throws SQLException {
        GradeStats stats = new GradeStats();
        String sql = "SELECT score FROM submissions";
        Connection conn = DatabaseManager.getConnection();
        double sum = 0.0;
        boolean hasScore = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stats.totalSubmissions++;
                Object scoreObj = rs.getObject("score");
                if (scoreObj != null) {
                    double s = rs.getDouble("score");
                    stats.gradedCount++;
                    sum += s;
                    if (!hasScore) {
                        stats.maxScore = s;
                        stats.minScore = s;
                        hasScore = true;
                    } else {
                        if (s > stats.maxScore) stats.maxScore = s;
                        if (s < stats.minScore) stats.minScore = s;
                    }
                    if (s >= 8.5) {
                        stats.countExcellent++;
                    } else if (s >= 7.0) {
                        stats.countGood++;
                    } else if (s >= 5.0) {
                        stats.countAverage++;
                    } else {
                        stats.countWeak++;
                    }
                } else {
                    stats.ungradedCount++;
                }
            }
        }
        if (stats.gradedCount > 0) {
            stats.averageScore = sum / stats.gradedCount;
        }
        return stats;
    }

    /**
     * Tìm bài nộp của sinh viên khác có cùng mã băm SHA-256 (Phát hiện trùng lặp / nghi vấn đạo bài)
     */
    public static SubmissionModel findDuplicateByHash(String fileHash, String currentStudentId) throws SQLException {
        if (fileHash == null || fileHash.trim().isEmpty()) return null;
        String sql = "SELECT sub.id, sub.student_id, s.full_name, s.class_name, sub.file_name, " +
                     "sub.file_type, sub.file_size, sub.submission_time, sub.status, sub.saved_path, sub.file_hash, " +
                     "sub.score, sub.feedback, sub.graded_at " +
                     "FROM submissions sub " +
                     "LEFT JOIN students s ON sub.student_id = s.student_id " +
                     "WHERE sub.file_hash = ? AND sub.student_id != ? " +
                     "LIMIT 1";

        Connection conn = DatabaseManager.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fileHash.trim());
            ps.setString(2, currentStudentId != null ? currentStudentId.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Double score = rs.getObject("score") != null ? rs.getDouble("score") : null;
                    return new SubmissionModel(
                            rs.getInt("id"),
                            rs.getString("student_id"),
                            rs.getString("full_name"),
                            rs.getString("class_name"),
                            rs.getString("file_name"),
                            rs.getString("file_type"),
                            rs.getLong("file_size"),
                            rs.getString("submission_time"),
                            rs.getString("status"),
                            rs.getString("saved_path"),
                            rs.getString("file_hash"),
                            score,
                            rs.getString("feedback"),
                            rs.getString("graded_at")
                    );
                }
            }
        }
        return null;
    }
}
