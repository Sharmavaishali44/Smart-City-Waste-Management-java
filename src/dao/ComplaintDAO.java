package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import model.Complaint;
import model.Enums.ComplaintStatus;
import model.Enums.PriorityLevel;

public class ComplaintDAO {

    public boolean createComplaint(Complaint c) {
        if (c.getComplaintCode() == null || c.getComplaintCode().isEmpty()) {
            c.setComplaintCode(generateNextCode());
        }

        String sql = "INSERT INTO complaints (" +
                     "complaint_code, citizen_user_id, citizen_name, phone, email, " +
                     "area_id, ward_id, locality, address, category, description, image_path, " +
                     "status, priority, priority_score, priority_reason, is_health_risk, " +
                     "is_garbage_overflow, is_sensitive_area, assigned_crew_id, expected_resolution_time" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getComplaintCode());
            if (c.getCitizenUserId() != null) ps.setInt(2, c.getCitizenUserId()); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setString(3, c.getCitizenName());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getEmail());
            ps.setInt(6, c.getAreaId());
            ps.setInt(7, c.getWardId());
            ps.setString(8, c.getLocality());
            ps.setString(9, c.getAddress());
            ps.setString(10, c.getCategory());
            ps.setString(11, c.getDescription());
            ps.setString(12, c.getImagePath());
            ps.setString(13, c.getStatus() != null ? c.getStatus().name() : ComplaintStatus.NEW.name());
            ps.setString(14, c.getPriority() != null ? c.getPriority().name() : PriorityLevel.MEDIUM.name());
            ps.setInt(15, c.getPriorityScore());
            ps.setString(16, c.getPriorityReason());
            ps.setBoolean(17, c.isHealthRisk());
            ps.setBoolean(18, c.isGarbageOverflow());
            ps.setBoolean(19, c.isSensitiveArea());
            if (c.getAssignedCrewId() != null) ps.setInt(20, c.getAssignedCrewId()); else ps.setNull(20, java.sql.Types.INTEGER);
            ps.setTimestamp(21, c.getExpectedResolutionTime());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        c.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error createComplaint: " + e.getMessage());
        }
        return false;
    }

    public Complaint findById(int id) {
        String sql = getBaseSelectSql() + " WHERE c.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToComplaint(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error findById: " + e.getMessage());
        }
        return null;
    }

    public Complaint findByCode(String code) {
        String sql = getBaseSelectSql() + " WHERE c.complaint_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToComplaint(rs);
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error findByCode: " + e.getMessage());
        }
        return null;
    }

    public List<Complaint> getByCitizenId(int citizenUserId) {
        List<Complaint> list = new ArrayList<>();
        String sql = getBaseSelectSql() + " WHERE c.citizen_user_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, citizenUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error getByCitizenId: " + e.getMessage());
        }
        return list;
    }

    public List<Complaint> getByCrewId(int crewId, boolean onlyActive) {
        List<Complaint> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(getBaseSelectSql());
        sql.append(" WHERE c.assigned_crew_id = ? ");
        if (onlyActive) {
            sql.append(" AND c.status IN ('ASSIGNED', 'IN_PROGRESS') ");
        }
        sql.append(" ORDER BY c.priority_score DESC, c.created_at ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, crewId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error getByCrewId: " + e.getMessage());
        }
        return list;
    }

    public List<Complaint> searchAndFilter(String query, ComplaintStatus status, PriorityLevel priority,
                                          Integer areaId, Integer wardId, String category,
                                          String sortBy, boolean sortAsc) {
        List<Complaint> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(getBaseSelectSql());
        sql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (c.complaint_code LIKE ? OR c.citizen_name LIKE ? OR c.phone LIKE ? OR c.locality LIKE ? OR c.address LIKE ? OR c.description LIKE ?) ");
            String wild = "%" + query.trim() + "%";
            for (int i = 0; i < 6; i++) params.add(wild);
        }

        if (status != null) {
            sql.append(" AND c.status = ? ");
            params.add(status.name());
        }

        if (priority != null) {
            sql.append(" AND c.priority = ? ");
            params.add(priority.name());
        }

        if (areaId != null && areaId > 0) {
            sql.append(" AND c.area_id = ? ");
            params.add(areaId);
        }

        if (wardId != null && wardId > 0) {
            sql.append(" AND c.ward_id = ? ");
            params.add(wardId);
        }

        if (category != null && !category.equalsIgnoreCase("ALL") && !category.trim().isEmpty()) {
            sql.append(" AND c.category = ? ");
            params.add(category);
        }

        // Sorting
        String orderCol = "c.created_at";
        if ("priority".equalsIgnoreCase(sortBy)) {
            orderCol = "c.priority_score";
        } else if ("status".equalsIgnoreCase(sortBy)) {
            orderCol = "c.status";
        } else if ("area".equalsIgnoreCase(sortBy)) {
            orderCol = "a.name";
        } else if ("code".equalsIgnoreCase(sortBy)) {
            orderCol = "c.complaint_code";
        }

        sql.append(" ORDER BY ").append(orderCol).append(sortAsc ? " ASC" : " DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error in searchAndFilter: " + e.getMessage());
        }
        return list;
    }

    public List<Complaint> getPendingAssignments() {
        List<Complaint> list = new ArrayList<>();
        String sql = getBaseSelectSql() + " WHERE c.status IN ('NEW', 'PENDING_ASSIGNMENT') ORDER BY c.priority_score DESC, c.created_at ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSetToComplaint(rs));
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error getPendingAssignments: " + e.getMessage());
        }
        return list;
    }

    public boolean updateStatus(int complaintId, ComplaintStatus newStatus, String notes) {
        String sql = "UPDATE complaints SET status = ?, resolution_notes = ?, " +
                     "resolved_at = (CASE WHEN ? = 'RESOLVED' THEN NOW() ELSE resolved_at END) " +
                     "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setString(2, notes);
            ps.setString(3, newStatus.name());
            ps.setInt(4, complaintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error updateStatus: " + e.getMessage());
            return false;
        }
    }

    public boolean assignCrew(int complaintId, int crewId, Timestamp expectedResolutionTime) {
        String sql = "UPDATE complaints SET assigned_crew_id = ?, status = 'ASSIGNED', " +
                     "expected_resolution_time = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, crewId);
            ps.setTimestamp(2, expectedResolutionTime);
            ps.setInt(3, complaintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error assignCrew: " + e.getMessage());
            return false;
        }
    }

    public boolean setPendingAssignment(int complaintId) {
        String sql = "UPDATE complaints SET status = 'PENDING_ASSIGNMENT', assigned_crew_id = NULL WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error setPendingAssignment: " + e.getMessage());
            return false;
        }
    }

    public int countByStatus(ComplaintStatus status) {
        String sql = "SELECT COUNT(*) FROM complaints WHERE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error countByStatus: " + e.getMessage());
        }
        return 0;
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM complaints";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error countTotal: " + e.getMessage());
        }
        return 0;
    }

    public int countCritical() {
        String sql = "SELECT COUNT(*) FROM complaints WHERE priority = 'CRITICAL' AND status != 'RESOLVED'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error countCritical: " + e.getMessage());
        }
        return 0;
    }

    public int countHigh() {
        String sql = "SELECT COUNT(*) FROM complaints WHERE priority = 'HIGH' AND status != 'RESOLVED'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error countHigh: " + e.getMessage());
        }
        return 0;
    }

    public int countRecentInWard(int wardId, int hours) {
        String sql = "SELECT COUNT(*) FROM complaints WHERE ward_id = ? " +
                     "AND created_at >= DATE_SUB(NOW(), INTERVAL ? HOUR)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wardId);
            ps.setInt(2, hours);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] Error countRecentInWard: " + e.getMessage());
        }
        return 0;
    }

    private synchronized String generateNextCode() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String prefix = "CMP-" + year + "-";
        String sql = "SELECT MAX(id) FROM complaints";
        int nextNum = 1001;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int maxId = rs.getInt(1);
                nextNum = 1000 + maxId + 1;
            }
        } catch (SQLException ignored) {}
        return prefix + nextNum;
    }

    private String getBaseSelectSql() {
        return "SELECT c.*, a.name AS area_name, w.ward_number, w.ward_name, " +
               "cr.crew_name AS assigned_crew_name, cr.phone AS crew_phone, cr.vehicle_number AS crew_vehicle " +
               "FROM complaints c " +
               "JOIN areas a ON c.area_id = a.id " +
               "JOIN wards w ON c.ward_id = w.id " +
               "LEFT JOIN crews cr ON c.assigned_crew_id = cr.id";
    }

    private Complaint mapResultSetToComplaint(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getInt("id"));
        c.setComplaintCode(rs.getString("complaint_code"));
        int uid = rs.getInt("citizen_user_id");
        c.setCitizenUserId(rs.wasNull() ? null : uid);
        c.setCitizenName(rs.getString("citizen_name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAreaId(rs.getInt("area_id"));
        c.setAreaName(rs.getString("area_name"));
        c.setWardId(rs.getInt("ward_id"));
        c.setWardNumber(rs.getString("ward_number"));
        c.setWardName(rs.getString("ward_name"));
        c.setLocality(rs.getString("locality"));
        c.setAddress(rs.getString("address"));
        c.setCategory(rs.getString("category"));
        c.setDescription(rs.getString("description"));
        c.setImagePath(rs.getString("image_path"));
        c.setStatus(ComplaintStatus.fromString(rs.getString("status")));
        c.setPriority(PriorityLevel.fromString(rs.getString("priority")));
        c.setPriorityScore(rs.getInt("priority_score"));
        c.setPriorityReason(rs.getString("priority_reason"));
        c.setHealthRisk(rs.getBoolean("is_health_risk"));
        c.setGarbageOverflow(rs.getBoolean("is_garbage_overflow"));
        c.setSensitiveArea(rs.getBoolean("is_sensitive_area"));

        int crewId = rs.getInt("assigned_crew_id");
        c.setAssignedCrewId(rs.wasNull() ? null : crewId);
        c.setAssignedCrewName(rs.getString("assigned_crew_name"));
        c.setCrewPhone(rs.getString("crew_phone"));
        c.setCrewVehicleNumber(rs.getString("crew_vehicle"));

        c.setExpectedResolutionTime(rs.getTimestamp("expected_resolution_time"));
        c.setResolutionNotes(rs.getString("resolution_notes"));
        c.setResolutionImagePath(rs.getString("resolution_image_path"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));
        c.setResolvedAt(rs.getTimestamp("resolved_at"));
        return c;
    }
}
