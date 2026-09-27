package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Area;
import model.Ward;

public class AreaDAO {

    public List<Area> getAllAreas() {
        List<Area> areas = new ArrayList<>();
        String sql = "SELECT * FROM areas ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Area a = new Area();
                a.setId(rs.getInt("id"));
                a.setName(rs.getString("name"));
                a.setCity(rs.getString("city"));
                a.setPopulation(rs.getInt("population"));
                a.setSensitive(rs.getBoolean("is_sensitive"));
                a.setCreatedAt(rs.getTimestamp("created_at"));
                areas.add(a);
            }
        } catch (SQLException e) {
            System.err.println("[AreaDAO] Error in getAllAreas: " + e.getMessage());
        }
        return areas;
    }

    public Area getAreaById(int id) {
        String sql = "SELECT * FROM areas WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Area a = new Area();
                    a.setId(rs.getInt("id"));
                    a.setName(rs.getString("name"));
                    a.setCity(rs.getString("city"));
                    a.setPopulation(rs.getInt("population"));
                    a.setSensitive(rs.getBoolean("is_sensitive"));
                    a.setCreatedAt(rs.getTimestamp("created_at"));
                    return a;
                }
            }
        } catch (SQLException e) {
            System.err.println("[AreaDAO] Error in getAreaById: " + e.getMessage());
        }
        return null;
    }

    public List<Ward> getWardsByAreaId(int areaId) {
        List<Ward> wards = new ArrayList<>();
        String sql = "SELECT w.*, a.name as area_name FROM wards w " +
                     "JOIN areas a ON w.area_id = a.id WHERE w.area_id = ? ORDER BY w.ward_number ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, areaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ward w = new Ward();
                    w.setId(rs.getInt("id"));
                    w.setWardNumber(rs.getString("ward_number"));
                    w.setWardName(rs.getString("ward_name"));
                    w.setAreaId(rs.getInt("area_id"));
                    w.setAreaName(rs.getString("area_name"));
                    w.setCreatedAt(rs.getTimestamp("created_at"));
                    wards.add(w);
                }
            }
        } catch (SQLException e) {
            System.err.println("[AreaDAO] Error in getWardsByAreaId: " + e.getMessage());
        }
        return wards;
    }

    public List<Ward> getAllWards() {
        List<Ward> wards = new ArrayList<>();
        String sql = "SELECT w.*, a.name as area_name FROM wards w " +
                     "JOIN areas a ON w.area_id = a.id ORDER BY a.name, w.ward_number ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Ward w = new Ward();
                w.setId(rs.getInt("id"));
                w.setWardNumber(rs.getString("ward_number"));
                w.setWardName(rs.getString("ward_name"));
                w.setAreaId(rs.getInt("area_id"));
                w.setAreaName(rs.getString("area_name"));
                w.setCreatedAt(rs.getTimestamp("created_at"));
                wards.add(w);
            }
        } catch (SQLException e) {
            System.err.println("[AreaDAO] Error in getAllWards: " + e.getMessage());
        }
        return wards;
    }

    public Ward getWardById(int wardId) {
        String sql = "SELECT w.*, a.name as area_name FROM wards w " +
                     "JOIN areas a ON w.area_id = a.id WHERE w.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wardId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ward w = new Ward();
                    w.setId(rs.getInt("id"));
                    w.setWardNumber(rs.getString("ward_number"));
                    w.setWardName(rs.getString("ward_name"));
                    w.setAreaId(rs.getInt("area_id"));
                    w.setAreaName(rs.getString("area_name"));
                    w.setCreatedAt(rs.getTimestamp("created_at"));
                    return w;
                }
            }
        } catch (SQLException e) {
            System.err.println("[AreaDAO] Error in getWardById: " + e.getMessage());
        }
        return null;
    }
}
