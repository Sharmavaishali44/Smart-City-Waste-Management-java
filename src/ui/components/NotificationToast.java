package ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import utils.UIUtils;

/**
 * Modern floating notification toast for real-time municipal alerts.
 */
public class NotificationToast {

    public enum ToastType {
        INFO(UIUtils.COLOR_ACCENT_BLUE, "ℹ️"),
        SUCCESS(UIUtils.COLOR_PRIMARY, "✅"),
        WARNING(UIUtils.PRIORITY_HIGH, "⚠️"),
        CRITICAL(UIUtils.PRIORITY_CRITICAL, "🚨");

        final Color color;
        final String icon;

        ToastType(Color color, String icon) {
            this.color = color;
            this.icon = icon;
        }
    }

    public static void show(Component parent, String title, String message, ToastType type) {
        SwingUtilities.invokeLater(() -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(parent);
            if (parentWindow == null && parent instanceof Window) {
                parentWindow = (Window) parent;
            }

            JWindow toast = new JWindow(parentWindow);
            toast.setBackground(new Color(0, 0, 0, 0));

            RoundedPanel panel = new RoundedPanel(10, UIUtils.NAVY_DARK, type.color, 2, true);
            panel.setLayout(new BorderLayout(12, 0));
            panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 18));

            JLabel iconLbl = new JLabel(type.icon);
            iconLbl.setFont(UIUtils.FONT_HEADER_MEDIUM);

            JPanel textPanel = new JPanel();
            textPanel.setOpaque(false);
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(UIUtils.FONT_BODY_BOLD);
            titleLbl.setForeground(Color.WHITE);

            JLabel msgLbl = new JLabel("<html><body style='width: 220px;'>" + message + "</body></html>");
            msgLbl.setFont(UIUtils.FONT_CAPTION);
            msgLbl.setForeground(new Color(203, 213, 225));

            textPanel.add(titleLbl);
            textPanel.add(Box.createVerticalStrut(3));
            textPanel.add(msgLbl);

            panel.add(iconLbl, BorderLayout.WEST);
            panel.add(textPanel, BorderLayout.CENTER);

            toast.setContentPane(panel);
            toast.pack();

            if (parentWindow != null && parentWindow.isShowing()) {
                Point loc = parentWindow.getLocationOnScreen();
                int x = loc.x + parentWindow.getWidth() - toast.getWidth() - 25;
                int y = loc.y + 40;
                toast.setLocation(x, y);
            } else {
                Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                toast.setLocation(screen.width - toast.getWidth() - 30, 40);
            }

            toast.setAlwaysOnTop(true);
            toast.setVisible(true);

            // Auto dismiss after 4.5 seconds
            Timer timer = new Timer(4500, e -> {
                toast.setVisible(false);
                toast.dispose();
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
}
