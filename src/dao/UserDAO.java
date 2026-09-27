package dao;

import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Enums.UserRole;
import model.User;
import utils.SecurityUtils;

public class UserDAO {

    /**
     * Authenticates a user using either username OR email, checking is_active flag.
     */
    public User authenticate(String identifier, String plainPassword) {
        if (identifier == null || plainPassword == null) return null;
        String cleanId = identifier.trim();
        String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cleanId);
            ps.setString(2, cleanId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String salt = rs.getString("salt");
                    String storedHash = rs.getString("password_hash");
                    if (SecurityUtils.verifyPassword(plainPassword, salt, storedHash)) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error in authenticate: " + e.getMessage());
        }
        return null;
    }

    /**
     * Public user registration strictly sets role to CITIZEN.
     * Prevents public creation of Admin or Crew accounts.
     */
    public boolean register(User user, String plainPassword) {
        user.setRole(UserRole.CITIZEN); // Strict security enforcement
        return saveUserInternal(user, plainPassword, true);
    }

    /**
     * Admin-only creation of staff or crew accounts.
     */
    public boolean createUserByAdmin(User user, String plainPassword) {
        return saveUserInternal(user, plainPassword, true);
    }

    private boolean saveUserInternal(User user, String plainPassword, boolean isActive) {
        String sql = "INSERT INTO users (username, password_hash, salt, full_name, email, phone, role, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword(plainPassword, salt);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getRole().name());
            ps.setBoolean(8, isActive);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error in saveUserInternal: " + e.getMessage());
        }
        return false;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error findByUsername: " + e.getMessage());
        }
        return null;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error findById: " + e.getMessage());
        }
        return null;
    }

    public boolean existsUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean existsEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public List<User> getAllCitizens() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'CITIZEN' ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error getAllCitizens: " + e.getMessage());
        }
        return list;
    }

    public List<User> getAllStaff() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'ADMIN' ORDER BY full_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error getAllStaff: " + e.getMessage());
        }
        return list;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setRole(UserRole.valueOf(rs.getString("role")));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
