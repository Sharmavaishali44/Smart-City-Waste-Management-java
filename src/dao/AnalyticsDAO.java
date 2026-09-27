package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsDAO {

    public static class AreaIntelligenceRecord {
        public String areaName;
        public int totalComplaints;
        public int criticalCount;
        public int pendingCount;
        public int resolvedCount;
        public double avgResolutionHours;

        public AreaIntelligenceRecord(String areaName, int total, int critical, int pending, int resolved, double avgHours) {
            this.areaName = areaName;
            this.totalComplaints = total;
            this.criticalCount = critical;
            this.pendingCount = pending;
            this.resolvedCount = resolved;
            this.avgResolutionHours = avgHours;
        }
    }

    public Map<String, Integer> getComplaintsByCategory() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT category, COUNT(*) as cnt FROM complaints GROUP BY category ORDER BY cnt DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("category"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Error getComplaintsByCategory: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getComplaintsByArea() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT a.name as area_name, COUNT(c.id) as cnt FROM areas a " +
                     "LEFT JOIN complaints c ON a.id = c.area_id " +
                     "GROUP BY a.id, a.name ORDER BY cnt DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("area_name"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Error getComplaintsByArea: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getPriorityDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT priority, COUNT(*) as cnt FROM complaints GROUP BY priority ORDER BY " +
                     "FIELD(priority, 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW')";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("priority"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Error getPriorityDistribution: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getStatusDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) as cnt FROM complaints GROUP BY status ORDER BY " +
                     "FIELD(status, 'NEW', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'REJECTED')";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Error getStatusDistribution: " + e.getMessage());
        }
        return map;
    }

    public List<AreaIntelligenceRecord> getAreaIntelligence() {
        List<AreaIntelligenceRecord> list = new ArrayList<>();
        String sql = "SELECT a.name AS area_name, " +
                     "COUNT(c.id) AS total, " +
                     "SUM(CASE WHEN c.priority = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_cnt, " +
                     "SUM(CASE WHEN c.status IN ('NEW', 'ASSIGNED', 'IN_PROGRESS') THEN 1 ELSE 0 END) AS pending_cnt, " +
                     "SUM(CASE WHEN c.status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved_cnt, " +
                     "COALESCE(AVG(CASE WHEN c.status = 'RESOLVED' AND c.resolved_at IS NOT NULL " +
                     "  THEN TIMESTAMPDIFF(MINUTE, c.created_at, c.resolved_at) / 60.0 ELSE NULL END), 6.5) AS avg_hours " +
                     "FROM areas a " +
                     "LEFT JOIN complaints c ON a.id = c.area_id " +
                     "GROUP BY a.id, a.name ORDER BY total DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AreaIntelligenceRecord(
                    rs.getString("area_name"),
                    rs.getInt("total"),
                    rs.getInt("critical_cnt"),
                    rs.getInt("pending_cnt"),
                    rs.getInt("resolved_cnt"),
                    Math.round(rs.getDouble("avg_hours") * 10.0) / 10.0
                ));
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Error getAreaIntelligence: " + e.getMessage());
        }
        return list;
    }

    public Map<String, Integer> getDailyTrendLast7Days() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(created_at, '%b %d') as dt_label, COUNT(*) as cnt " +
                     "FROM complaints " +
                     "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                     "GROUP BY DATE(created_at), dt_label " +
                     "ORDER BY DATE(created_at) ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("dt_label"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            System.err.println("[AnalyticsDAO] Error getDailyTrendLast7Days: " + e.getMessage());
        }
        return map;
    }
}
