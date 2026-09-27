package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Thread-safe Database Connection Factory for MySQL JDBC.
 * Reads properties from config/db.properties with resilient fallbacks.
 */
public class DBConnection {

    private static final String CONFIG_PATH = "config/db.properties";
    private static Properties props = new Properties();
    private static boolean driverLoaded = false;

    static {
        loadConfiguration();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    public static synchronized void loadConfiguration() {
        File file = new File(CONFIG_PATH);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("[DBConnection] Could not read " + CONFIG_PATH + ", using defaults: " + e.getMessage());
            }
        }
    }

    public static synchronized void saveConfiguration(String host, String port, String name, String user, String password) throws IOException {
        props.setProperty("db.host", host);
        props.setProperty("db.port", port);
        props.setProperty("db.name", name);
        props.setProperty("db.user", user);
        props.setProperty("db.password", password);

        File file = new File(CONFIG_PATH);
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "Smart City Waste Management Database Configuration");
        }
    }

    public static String getHost() {
        return props.getProperty("db.host", "localhost");
    }

    public static String getPort() {
        return props.getProperty("db.port", "3306");
    }

    public static String getDatabaseName() {
        return props.getProperty("db.name", "smart_city_waste");
    }

    public static String getUser() {
        return props.getProperty("db.user", "root");
    }

    public static String getPassword() {
        return props.getProperty("db.password", "");
    }

    public static String getUrl() {
        String host = getHost();
        String port = getPort();
        String name = getDatabaseName();
        String params = props.getProperty("db.params", "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true");
        return "jdbc:mysql://" + host + ":" + port + "/" + name + params;
    }

    public static String getServerOnlyUrl() {
        String host = getHost();
        String port = getPort();
        String params = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return "jdbc:mysql://" + host + ":" + port + "/" + params;
    }

    /**
     * Obtains an active connection to the smart_city_waste database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), getUser(), getPassword());
    }

    /**
     * Obtains a connection directly to the MySQL server (not database-specific),
     * useful for creating the database or running initial setup.
     */
    public static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(getServerOnlyUrl(), getUser(), getPassword());
    }

    /**
     * Tests connection to MySQL.
     * @return null if successful, or error message string if failed.
     */
    public static String testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                return null; // Success
            }
            return "Connection opened but returned closed state.";
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /**
     * Helper to safely close DB resources.
     */
    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    public static boolean isDriverLoaded() {
        return driverLoaded;
    }
}
