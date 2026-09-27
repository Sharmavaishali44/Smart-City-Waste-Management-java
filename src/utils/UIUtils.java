package utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import model.Enums.ComplaintStatus;
import model.Enums.PriorityLevel;

/**
 * Design tokens, colors, fonts, and styling utilities for the Smart City UI.
 */
public class UIUtils {

    // --- Modern Color Palette ---
    public static final Color NAVY_DARK = new Color(15, 23, 42);       // #0F172A
    public static final Color NAVY_SURFACE = new Color(30, 41, 59);    // #1E293B
    public static final Color NAVY_LIGHT = new Color(51, 65, 85);      // #334155

    public static final Color COLOR_PRIMARY = new Color(16, 185, 129);      // Emerald Green #10B981
    public static final Color COLOR_PRIMARY_HOVER = new Color(5, 150, 105); // #059669
    public static final Color COLOR_PRIMARY_LIGHT = new Color(236, 253, 245);// #ECFDF5

    public static final Color COLOR_ACCENT_BLUE = new Color(59, 130, 246);  // #3B82F6
    public static final Color COLOR_ACCENT_PURPLE = new Color(139, 92, 246);// #8B5CF6
    public static final Color COLOR_ACCENT_CYAN = new Color(6, 182, 212);   // #06B6D4

    public static final Color BG_LIGHT = new Color(248, 250, 252);     // #F8FAFC
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_HOVER = new Color(241, 245, 249);     // #F1F5F9

    public static final Color TEXT_MAIN = new Color(15, 23, 42);       // #0F172A
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // #64748B
    public static final Color TEXT_LIGHT = new Color(248, 250, 252);
    public static final Color TEXT_PLACEHOLDER = new Color(148, 163, 184);

    public static final Color BORDER_LIGHT = new Color(226, 232, 240); // #E2E8F0
    public static final Color BORDER_FOCUS = new Color(16, 185, 129);

    // Priority Colors
    public static final Color PRIORITY_CRITICAL = new Color(239, 68, 68);
    public static final Color PRIORITY_HIGH = new Color(249, 115, 22);
    public static final Color PRIORITY_MEDIUM = new Color(245, 158, 11);
    public static final Color PRIORITY_LOW = new Color(16, 185, 129);

    // Status Colors
    public static final Color STATUS_NEW = new Color(59, 130, 246);
    public static final Color STATUS_ASSIGNED = new Color(168, 85, 247);
    public static final Color STATUS_IN_PROGRESS = new Color(245, 158, 11);
    public static final Color STATUS_RESOLVED = new Color(16, 185, 129);
    public static final Color STATUS_REJECTED = new Color(239, 68, 68);

    // --- Typography (Clean Segoe UI / System Sans) ---
    private static final String FONT_FAMILY = "Segoe UI";

    public static final Font FONT_HEADER_LARGE = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font FONT_HEADER_MEDIUM = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_HEADER_SMALL = new Font(FONT_FAMILY, Font.BOLD, 15);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_CAPTION = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_CAPTION_BOLD = new Font(FONT_FAMILY, Font.BOLD, 11);
    public static final Font FONT_STAT_NUM = new Font(FONT_FAMILY, Font.BOLD, 28);

    /**
     * Enables anti-aliasing on Graphics2D for ultra-smooth rendering.
     */
    public static void enableAntialiasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public static Border createFieldBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }

    public static void styleTextField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_MAIN);
        field.setBackground(Color.WHITE);
        field.setBorder(createFieldBorder());
        field.setCaretColor(COLOR_PRIMARY);
    }

    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_LIGHT);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
    }

    public static Color getPriorityColor(PriorityLevel priority) {
        if (priority == null) return PRIORITY_MEDIUM;
        switch (priority) {
            case CRITICAL: return PRIORITY_CRITICAL;
            case HIGH: return PRIORITY_HIGH;
            case MEDIUM: return PRIORITY_MEDIUM;
            case LOW: return PRIORITY_LOW;
            default: return PRIORITY_MEDIUM;
        }
    }

    public static Color getStatusColor(ComplaintStatus status) {
        if (status == null) return STATUS_NEW;
        switch (status) {
            case NEW: return STATUS_NEW;
            case ASSIGNED: return STATUS_ASSIGNED;
            case IN_PROGRESS: return STATUS_IN_PROGRESS;
            case RESOLVED: return STATUS_RESOLVED;
            case REJECTED: return STATUS_REJECTED;
            default: return STATUS_NEW;
        }
    }

    /**
     * Sets standard system look and feel with high DPI settings.
     */
    public static void setupModernLookAndFeel() {
        try {
            System.setProperty("sun.java2d.uiScale.enabled", "true");
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Fallback to default
        }
    }
}
