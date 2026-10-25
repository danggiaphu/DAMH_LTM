package damh.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object cho cấu hình hệ thống (Hạn nộp, trạng thái cổng nhận bài...)
 */
public class ConfigDAO {

    public static String getConfig(String key, String defaultValue) {
        String sql = "SELECT config_value FROM system_config WHERE config_key = ?";
        try {
            Connection conn = DatabaseManager.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("config_value");
                    }
                }
            }
        } catch (SQLException ignored) {}
        return defaultValue;
    }

    public static void setConfig(String key, String value) {
        String sql = "INSERT INTO system_config (config_key, config_value, updated_at) VALUES (?, ?, datetime('now', 'localtime')) " +
                     "ON CONFLICT(config_key) DO UPDATE SET config_value = excluded.config_value, updated_at = excluded.updated_at";
        try {
            Connection conn = DatabaseManager.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, key);
                ps.setString(2, value != null ? value : "");
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            // Fallback nếu phiên bản SQLite chưa hỗ trợ ON CONFLICT
            try {
                Connection conn = DatabaseManager.getConnection();
                String updateSql = "UPDATE system_config SET config_value = ?, updated_at = datetime('now', 'localtime') WHERE config_key = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, value != null ? value : "");
                    ps.setString(2, key);
                    int affected = ps.executeUpdate();
                    if (affected == 0) {
                        String insertSql = "INSERT INTO system_config (config_key, config_value) VALUES (?, ?)";
                        try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                            psIns.setString(1, key);
                            psIns.setString(2, value != null ? value : "");
                            psIns.executeUpdate();
                        }
                    }
                }
            } catch (SQLException ignored) {}
        }
    }

    public static Map<String, String> getAllConfigs() {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT config_key, config_value FROM system_config";
        try {
            Connection conn = DatabaseManager.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("config_key"), rs.getString("config_value"));
                }
            }
        } catch (SQLException ignored) {}
        return map;
    }
}
