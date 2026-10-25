package damh.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Trình thực thi Migration tự động cho SQLite database
 */
public class DatabaseMigration {

    public interface MigrationLogger {
        void log(String message);
    }

    private static final String DEFAULT_MIGRATIONS_DIR = "migrations";

    // Fallback script V1 nếu không tìm thấy thư mục migrations trên đĩa
    private static final String FALLBACK_V1_SQL =
            "CREATE TABLE IF NOT EXISTS schema_version (\n" +
            "    version INTEGER PRIMARY KEY,\n" +
            "    description TEXT NOT NULL,\n" +
            "    applied_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))\n" +
            ");\n" +
            "CREATE TABLE IF NOT EXISTS students (\n" +
            "    student_id TEXT PRIMARY KEY,\n" +
            "    full_name TEXT NOT NULL,\n" +
            "    class_name TEXT,\n" +
            "    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),\n" +
            "    updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))\n" +
            ");\n" +
            "CREATE TABLE IF NOT EXISTS submissions (\n" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "    student_id TEXT NOT NULL,\n" +
            "    file_name TEXT NOT NULL,\n" +
            "    file_type TEXT,\n" +
            "    file_size INTEGER NOT NULL,\n" +
            "    submission_time TEXT NOT NULL,\n" +
            "    status TEXT NOT NULL DEFAULT 'Thành công',\n" +
            "    saved_path TEXT NOT NULL,\n" +
            "    note TEXT,\n" +
            "    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE\n" +
            ");\n" +
            "CREATE INDEX IF NOT EXISTS idx_submissions_student_id ON submissions(student_id);\n" +
            "CREATE INDEX IF NOT EXISTS idx_submissions_time ON submissions(submission_time);\n" +
            "CREATE INDEX IF NOT EXISTS idx_submissions_file_type ON submissions(file_type);\n" +
            "CREATE INDEX IF NOT EXISTS idx_students_class ON students(class_name);";

    // Fallback script V2 nếu không tìm thấy thư mục migrations trên đĩa
    private static final String FALLBACK_V2_SQL =
            "INSERT OR IGNORE INTO students (student_id, full_name, class_name) VALUES\n" +
            "('B21DCCN001', 'Nguyễn Văn An', 'D21CQCN01-N'),\n" +
            "('B21DCCN002', 'Trần Thị Bình', 'D21CQCN01-N'),\n" +
            "('B21DCCN003', 'Lê Hoàng Cường', 'D21CQCN02-N'),\n" +
            "('B21DCCN004', 'Phạm Minh Đức', 'D21CQCN02-N'),\n" +
            "('B21DCCN005', 'Võ Thu Hương', 'D21CQCN03-N');";

    // Fallback script V3 nếu không tìm thấy thư mục migrations trên đĩa
    private static final String FALLBACK_V3_SQL =
            "ALTER TABLE submissions ADD COLUMN file_hash TEXT;\n" +
            "CREATE INDEX IF NOT EXISTS idx_submissions_file_hash ON submissions(file_hash);";

    // Fallback script V4 nếu không tìm thấy thư mục migrations trên đĩa
    private static final String FALLBACK_V4_SQL =
            "CREATE TABLE IF NOT EXISTS system_config (\n" +
            "    config_key TEXT PRIMARY KEY,\n" +
            "    config_value TEXT NOT NULL,\n" +
            "    updated_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))\n" +
            ");\n" +
            "INSERT OR IGNORE INTO system_config (config_key, config_value) VALUES\n" +
            "('deadline_enabled', 'false'),\n" +
            "('deadline_time', ''),\n" +
            "('deadline_block_late', 'false'),\n" +
            "('gate_manual_closed', 'false');";

    // Fallback script V5 nếu không tìm thấy thư mục migrations trên đĩa
    private static final String FALLBACK_V5_SQL =
            "UPDATE students SET student_id = UPPER(TRIM(student_id)), full_name = UPPER(TRIM(full_name)), class_name = UPPER(TRIM(class_name));\n" +
            "UPDATE submissions SET student_id = UPPER(TRIM(student_id)), file_type = UPPER(TRIM(file_type));";

    // Fallback script V6 nếu không tìm thấy thư mục migrations trên đĩa (Điểm số & Đánh giá)
    private static final String FALLBACK_V6_SQL =
            "ALTER TABLE submissions ADD COLUMN score REAL;\n" +
            "ALTER TABLE submissions ADD COLUMN feedback TEXT;\n" +
            "ALTER TABLE submissions ADD COLUMN graded_at TEXT;\n" +
            "CREATE INDEX IF NOT EXISTS idx_submissions_score ON submissions(score);";

    public static void runMigrations(Connection conn, MigrationLogger logger) throws SQLException {
        if (logger != null) logger.log("[MIGRATION] Kiểm tra cấu trúc bảng schema_version...");

        // 1. Tạo bảng theo dõi migration nếu chưa có
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS schema_version (" +
                    "version INTEGER PRIMARY KEY, " +
                    "description TEXT NOT NULL, " +
                    "applied_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')))");
        }

        // 2. Lấy danh sách các version đã chạy
        Set<Integer> appliedVersions = new HashSet<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version FROM schema_version")) {
            while (rs.next()) {
                appliedVersions.add(rs.getInt("version"));
            }
        }

        // 3. Quét các file migration từ thư mục migrations/
        Map<Integer, MigrationFile> fileMigrations = loadMigrationFiles();

        // Đảm bảo tối thiểu có V1, V2, V3, V4, V5 và V6 nếu thư mục migrations bị thiếu
        if (!fileMigrations.containsKey(1)) {
            fileMigrations.put(1, new MigrationFile(1, "create_tables", FALLBACK_V1_SQL));
        }
        if (!fileMigrations.containsKey(2)) {
            fileMigrations.put(2, new MigrationFile(2, "seed_sample_students", FALLBACK_V2_SQL));
        }
        if (!fileMigrations.containsKey(3)) {
            fileMigrations.put(3, new MigrationFile(3, "add_file_hash", FALLBACK_V3_SQL));
        }
        if (!fileMigrations.containsKey(4)) {
            fileMigrations.put(4, new MigrationFile(4, "add_system_config", FALLBACK_V4_SQL));
        }
        if (!fileMigrations.containsKey(5)) {
            fileMigrations.put(5, new MigrationFile(5, "normalize_uppercase", FALLBACK_V5_SQL));
        }
        if (!fileMigrations.containsKey(6)) {
            fileMigrations.put(6, new MigrationFile(6, "add_grading_and_evaluation", FALLBACK_V6_SQL));
        }

        // 4. Áp dụng tuần tự theo số version tăng dần
        List<Integer> sortedVersions = new ArrayList<>(fileMigrations.keySet());
        Collections.sort(sortedVersions);

        int appliedCount = 0;
        for (int ver : sortedVersions) {
            if (!appliedVersions.contains(ver)) {
                MigrationFile mf = fileMigrations.get(ver);
                if (logger != null) {
                    logger.log(String.format("[MIGRATION] Đang chạy V%d__%s...", ver, mf.description));
                }

                executeMigration(conn, mf);
                recordMigration(conn, mf.version, mf.description);
                appliedCount++;

                if (logger != null) {
                    logger.log(String.format("[MIGRATION] -> Hoàn tất V%d__%s", ver, mf.description));
                }
            }
        }

        if (appliedCount == 0 && logger != null) {
            logger.log("[MIGRATION] Cơ sở dữ liệu đã ở phiên bản mới nhất.");
        }
    }

    private static void executeMigration(Connection conn, MigrationFile mf) throws SQLException {
        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try (Statement stmt = conn.createStatement()) {
            String[] commands = mf.sqlContent.split(";");
            for (String cmd : commands) {
                String trimmed = cmd.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
            conn.commit();
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    private static void recordMigration(Connection conn, int version, String description) throws SQLException {
        String sql = "INSERT INTO schema_version (version, description) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, version);
            ps.setString(2, description);
            ps.executeUpdate();
        }
    }

    private static Map<Integer, MigrationFile> loadMigrationFiles() {
        Map<Integer, MigrationFile> map = new HashMap<>();
        File dir = new File(DEFAULT_MIGRATIONS_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return map;
        }

        File[] files = dir.listFiles();
        if (files == null) return map;

        for (File f : files) {
            String name = f.getName();
            if (name.startsWith("V") && name.endsWith(".sql") && name.contains("__")) {
                try {
                    int underIndex = name.indexOf("__");
                    int version = Integer.parseInt(name.substring(1, underIndex));
                    String desc = name.substring(underIndex + 2, name.length() - 4);
                    String content = readFileContent(f);
                    map.put(version, new MigrationFile(version, desc, content));
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private static String readFileContent(File file) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Bỏ qua dòng comment SQL bắt đầu bằng --
                if (!line.trim().startsWith("--")) {
                    sb.append(line).append("\n");
                }
            }
        } catch (IOException ignored) {}
        return sb.toString();
    }

    public static class MigrationFile {
        public int version;
        public String description;
        public String sqlContent;

        public MigrationFile(int version, String description, String sqlContent) {
            this.version = version;
            this.description = description;
            this.sqlContent = sqlContent;
        }
    }
}
