package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.StatusHistory;

public class StatusHistoryDAO {

    public boolean logTransition(int complaintId, String oldStatus, String newStatus, Integer userId, String comments) {
        String sql = "INSERT INTO complaint_status_history (complaint_id, old_status, new_status, changed_by_user_id, comments) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            ps.setString(2, oldStatus);
            ps.setString(3, newStatus);
            if (userId != null) ps.setInt(4, userId); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, comments);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StatusHistoryDAO] Error logTransition: " + e.getMessage());
            return false;
        }
    }

    public List<StatusHistory> getByComplaintId(int complaintId) {
        List<StatusHistory> list = new ArrayList<>();
        String sql = "SELECT h.*, u.full_name as user_name FROM complaint_status_history h " +
                     "LEFT JOIN users u ON h.changed_by_user_id = u.id " +
                     "WHERE h.complaint_id = ? ORDER BY h.changed_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StatusHistory sh = new StatusHistory();
                    sh.setId(rs.getInt("id"));
                    sh.setComplaintId(rs.getInt("complaint_id"));
                    sh.setOldStatus(rs.getString("old_status"));
                    sh.setNewStatus(rs.getString("new_status"));
                    int uid = rs.getInt("changed_by_user_id");
                    sh.setChangedByUserId(rs.wasNull() ? null : uid);
                    sh.setChangedByName(rs.getString("user_name"));
                    sh.setComments(rs.getString("comments"));
                    sh.setChangedAt(rs.getTimestamp("changed_at"));
                    list.add(sh);
                }
            }
        } catch (SQLException e) {
            System.err.println("[StatusHistoryDAO] Error getByComplaintId: " + e.getMessage());
        }
        return list;
    }
}
