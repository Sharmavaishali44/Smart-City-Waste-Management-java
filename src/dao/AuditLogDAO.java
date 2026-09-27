package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.AuditLog;

public class AuditLogDAO {

    public boolean insertLog(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, username, action, target_type, target_id, details) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (log.getUserId() != null) ps.setInt(1, log.getUserId()); else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, log.getUsername() != null ? log.getUsername() : "SYSTEM");
            ps.setString(3, log.getAction());
            ps.setString(4, log.getTargetType());
            if (log.getTargetId() != null) ps.setInt(5, log.getTargetId()); else ps.setNull(5, java.sql.Types.INTEGER);
            ps.setString(6, log.getDetails());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AuditLogDAO] Error insertLog: " + e.getMessage());
            return false;
        }
    }

    public List<AuditLog> getAllLogs(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 100);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(rs.getInt("id"));
                    int uid = rs.getInt("user_id");
                    log.setUserId(rs.wasNull() ? null : uid);
                    log.setUsername(rs.getString("username"));
                    log.setAction(rs.getString("action"));
                    log.setTargetType(rs.getString("target_type"));
                    int tid = rs.getInt("target_id");
                    log.setTargetId(rs.wasNull() ? null : tid);
                    log.setDetails(rs.getString("details"));
                    log.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AuditLogDAO] Error getAllLogs: " + e.getMessage());
        }
        return list;
    }
}
