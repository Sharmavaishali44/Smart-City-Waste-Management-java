import config.DBConnection;
import config.DatabaseInitializer;
import javax.swing.SwingUtilities;
import ui.LoginFrame;
import utils.UIUtils;

/**
 * Waste Collection & Municipal Complaint System
 * Smart City Municipal Corporation Desktop Application.
 *
 * Architecture: Swing/AWT (UI) -> Service Layer -> DAO Layer -> MySQL JDBC.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("  Smart City Waste Collection & Municipal Complaint System");
        System.out.println("==========================================================");
        System.out.println("Java Version : " + System.getProperty("java.version"));
        System.out.println("OS           : " + System.getProperty("os.name"));
        System.out.println("JDBC Driver  : MySQL Connector/J (com.mysql.cj.jdbc.Driver)");
        System.out.println("Database URL : " + DBConnection.getUrl());
        System.out.println("----------------------------------------------------------");

        // Set modern UI Look and Feel & high DPI rendering
        UIUtils.setupModernLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);

                // Check database status in background
                new Thread(() -> {
                    String err = DBConnection.testConnection();
                    if (err == null) {
                        System.out.println("[Main] MySQL Connection: ONLINE ✅");
                        if (DatabaseInitializer.isDatabaseReady()) {
                            System.out.println("[Main] Municipal Database: READY & SEEDED ✅");
                        } else {
                            System.out.println("[Main] Database reachable but tables need initialization.");
                            System.out.println("[Main] Use the 'Database Setup' tab in the Login window to initialize with 1 click.");
                        }
                    } else {
                        System.out.println("[Main] MySQL Connection warning: " + err);
                        System.out.println("[Main] Configure credentials via config/db.properties or the 'Database Setup' wizard.");
                    }
                }).start();

            } catch (Exception e) {
                System.err.println("[Main] Fatal launch error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
