package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Crew;
import model.Enums.CrewAvailability;

public class CrewDAO {

    public List<Crew> getAllCrews() {
        List<Crew> list = new ArrayList<>();
        String sql = "SELECT c.*, a.name AS area_name, w.ward_number, w.ward_name " +
                     "FROM crews c " +
                     "JOIN areas a ON c.area_id = a.id " +
                     "JOIN wards w ON c.ward_id = w.id " +
                     "ORDER BY c.crew_name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToCrew(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CrewDAO] Error getAllCrews: " + e.getMessage());
        }
        return list;
    }

    public Crew getCrewById(int id) {
        String sql = "SELECT c.*, a.name AS area_name, w.ward_number, w.ward_name " +
                     "FROM crews c " +
                     "JOIN areas a ON c.area_id = a.id " +
                     "JOIN wards w ON c.ward_id = w.id " +
                     "WHERE c.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCrew(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CrewDAO] Error getCrewById: " + e.getMessage());
        }
        return null;
    }

    public Crew getCrewByUserId(int userId) {
        String sql = "SELECT c.*, a.name AS area_name, w.ward_number, w.ward_name " +
                     "FROM crews c " +
                     "JOIN areas a ON c.area_id = a.id " +
                     "JOIN wards w ON c.ward_id = w.id " +
                     "WHERE c.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCrew(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CrewDAO] Error getCrewByUserId: " + e.getMessage());
        }
        return null;
    }

    /**
     * Strict Area-Zone Smart Routing:
     * Never dispatches a crew outside the complaint's Area/Zone!
     * Considers available/on-duty status, exact ward match, current workload, and vehicle capacity.
     */
    public List<Crew> findAvailableCrewsForRouting(int areaId, Integer wardId) {
        List<Crew> list = new ArrayList<>();
        // Enforce STRICT area isolation (c.area_id = ?)
        StringBuilder sql = new StringBuilder(
            "SELECT c.*, a.name AS area_name, w.ward_number, w.ward_name " +
            "FROM crews c " +
            "JOIN areas a ON c.area_id = a.id " +
            "JOIN wards w ON c.ward_id = w.id " +
            "WHERE c.area_id = ? " +
            "AND c.status = 'ACTIVE' " +
            "AND c.availability IN ('AVAILABLE', 'ON_DUTY') "
        );

        if (wardId != null && wardId > 0) {
            sql.append("ORDER BY (CASE WHEN c.ward_id = ? THEN 0 ELSE 1 END), c.current_workload ASC, c.vehicle_capacity DESC");
        } else {
            sql.append("ORDER BY c.current_workload ASC, c.vehicle_capacity DESC");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, areaId);
            if (wardId != null && wardId > 0) {
                ps.setInt(2, wardId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCrew(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CrewDAO] Error in findAvailableCrewsForRouting: " + e.getMessage());
        }
        return list;
    }

    public boolean updateWorkload(int crewId, int delta) {
        String sql = "UPDATE crews SET current_workload = GREATEST(0, current_workload + ?) WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, crewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CrewDAO] Error updateWorkload: " + e.getMessage());
            return false;
        }
    }

    public void recalculateAllWorkloads() {
        String sql = "UPDATE crews c SET current_workload = (" +
                     "  SELECT COUNT(*) FROM complaints comp " +
                     "  WHERE comp.assigned_crew_id = c.id " +
                     "  AND comp.status IN ('ASSIGNED', 'IN_PROGRESS')" +
                     ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[CrewDAO] Error recalculateAllWorkloads: " + e.getMessage());
        }
    }

    public boolean updateAvailability(int crewId, CrewAvailability availability) {
        String sql = "UPDATE crews SET availability = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, availability.name());
            ps.setInt(2, crewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CrewDAO] Error updateAvailability: " + e.getMessage());
            return false;
        }
    }

    public boolean saveCrew(Crew crew) {
        String sql = "INSERT INTO crews (crew_name, leader_name, members_count, members_description, phone, " +
                     "area_id, ward_id, ward_coverage, vehicle_number, vehicle_capacity, current_workload, availability, status, user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, crew.getCrewName());
            ps.setString(2, crew.getLeaderName());
            ps.setInt(3, crew.getMembersCount());
            ps.setString(4, crew.getMembersDescription());
            ps.setString(5, crew.getPhone());
            ps.setInt(6, crew.getAreaId());
            ps.setInt(7, crew.getWardId());
            ps.setString(8, crew.getWardCoverage());
            ps.setString(9, crew.getVehicleNumber());
            ps.setDouble(10, crew.getVehicleCapacity());
            ps.setInt(11, crew.getCurrentWorkload());
            ps.setString(12, crew.getAvailability() != null ? crew.getAvailability().name() : CrewAvailability.AVAILABLE.name());
            ps.setString(13, crew.getStatus() != null ? crew.getStatus() : "ACTIVE");
            if (crew.getUserId() != null) ps.setInt(14, crew.getUserId()); else ps.setNull(14, java.sql.Types.INTEGER);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) crew.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[CrewDAO] Error saveCrew: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int crewId, String status) {
        String sql = "UPDATE crews SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, crewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CrewDAO] Error updateStatus: " + e.getMessage());
            return false;
        }
    }

    private Crew mapResultSetToCrew(ResultSet rs) throws SQLException {
        Crew c = new Crew();
        c.setId(rs.getInt("id"));
        c.setCrewName(rs.getString("crew_name"));
        c.setLeaderName(rs.getString("leader_name"));
        c.setMembersCount(rs.getInt("members_count"));
        c.setMembersDescription(rs.getString("members_description"));
        c.setPhone(rs.getString("phone"));
        c.setAreaId(rs.getInt("area_id"));
        c.setAreaName(rs.getString("area_name"));
        c.setWardId(rs.getInt("ward_id"));
        c.setWardNumber(rs.getString("ward_number"));
        c.setWardName(rs.getString("ward_name"));
        c.setWardCoverage(rs.getString("ward_coverage"));
        c.setVehicleNumber(rs.getString("vehicle_number"));
        c.setVehicleCapacity(rs.getDouble("vehicle_capacity"));
        c.setCurrentWorkload(rs.getInt("current_workload"));
        c.setAvailability(CrewAvailability.fromString(rs.getString("availability")));
        c.setStatus(rs.getString("status"));
        int uid = rs.getInt("user_id");
        c.setUserId(rs.wasNull() ? null : uid);
        c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }
}
