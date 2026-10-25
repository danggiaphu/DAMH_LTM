package damh.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối cơ sở dữ liệu SQLite và vòng đời khởi tạo
 */
public class DatabaseManager {

    public static final String DB_FILE_NAME = "damh.db";
    public static final String DB_URL = "jdbc:sqlite:" + DB_FILE_NAME;

    private static Connection connection = null;
    private static boolean isInitialized = false;

    private DatabaseManager() {}

    /**
     * Khởi tạo CSDL và chạy tự động Migration
     */
    public static synchronized void initialize(DatabaseMigration.MigrationLogger logger) throws SQLException {
        if (isInitialized && connection != null && !connection.isClosed()) {
            return;
        }

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy SQLite JDBC Driver trong Classpath!", e);
        }

        Connection conn = getConnection();
        // Bật hỗ trợ Foreign Key trong SQLite
        try (java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }

        DatabaseMigration.runMigrations(conn, logger);
        isInitialized = true;
    }

    /**
     * Lấy kết nối duy nhất (Singleton Connection) tới SQLite
     */
    public static synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy SQLite JDBC Driver!", e);
        }
        return connection;
    }

    /**
     * Kiểm tra file database đã tồn tại hay chưa
     */
    public static boolean databaseExists() {
        return new File(DB_FILE_NAME).exists();
    }

    /**
     * Đóng kết nối khi tắt server
     */
    public static synchronized void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {}
            connection = null;
        }
    }
}
