package config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Automates MySQL schema creation and seed data execution.
 */
public class DatabaseInitializer {

    public static boolean isDatabaseReady() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static void initializeDatabase(InitProgressListener listener) throws Exception {
        if (listener != null) listener.onProgress("Connecting to MySQL server...", 10);
        
        // Step 1: Create database if missing using server connection
        try (Connection serverConn = DBConnection.getServerConnection();
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DBConnection.getDatabaseName() + 
                               " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        if (listener != null) listener.onProgress("Applying database schema (schema.sql)...", 35);
        executeSqlFile("database/schema.sql");

        if (listener != null) listener.onProgress("Seeding initial municipal records (seed.sql)...", 70);
        executeSqlFile("database/seed.sql");

        if (listener != null) listener.onProgress("Database initialized successfully!", 100);
    }

    public static void executeSqlFile(String filePath) throws IOException, SQLException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("SQL script file not found at: " + file.getAbsolutePath());
        }

        List<String> statements = parseSqlStatements(file);
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    try {
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        System.err.println("[DatabaseInitializer] Error executing: " + 
                            (sql.length() > 60 ? sql.substring(0, 60) + "..." : sql));
                        throw e;
                    }
                }
            }
        }
    }

    private static List<String> parseSqlStatements(File file) throws IOException {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStmt = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String clean = line;
                int commentIdx = clean.indexOf("--");
                if (commentIdx >= 0) {
                    clean = clean.substring(0, commentIdx);
                }
                clean = clean.trim();
                if (clean.isEmpty() || clean.startsWith("//") || clean.startsWith("/*")) {
                    continue;
                }

                currentStmt.append(clean).append(" ");
                if (clean.endsWith(";")) {
                    String sql = currentStmt.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1).trim();
                    }
                    if (!sql.isEmpty()) {
                        statements.add(sql);
                    }
                    currentStmt.setLength(0);
                }
            }
        }
        return statements;
    }

    public interface InitProgressListener {
        void onProgress(String message, int percentage);
    }
}
