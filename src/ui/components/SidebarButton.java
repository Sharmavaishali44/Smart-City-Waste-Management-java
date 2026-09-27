package ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import utils.UIUtils;

/**
 * Sidebar navigation item with active indicator and smooth hover feedback.
 */
public class SidebarButton extends JPanel {

    private final String title;
    private final String icon;
    private boolean active = false;
    private boolean hovered = false;
    private final JLabel label;

    public SidebarButton(String icon, String title) {
        this.icon = icon;
        this.title = title;

        setLayout(new BorderLayout(12, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(220, 44));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        label = new JLabel(icon + "   " + title);
        label.setFont(UIUtils.FONT_BODY);
        label.setForeground(new Color(148, 163, 184)); // Muted slate text

        add(label, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            label.setFont(UIUtils.FONT_BODY_BOLD);
            label.setForeground(Color.WHITE);
        } else {
            label.setFont(UIUtils.FONT_BODY);
            label.setForeground(new Color(148, 163, 184));
        }
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    public String getTitle() {
        return title;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIUtils.enableAntialiasing(g2);

        if (active) {
            g2.setColor(UIUtils.NAVY_SURFACE);
            g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 8, 8);

            // Left Emerald active bar
            g2.setColor(UIUtils.COLOR_PRIMARY);
            g2.fillRoundRect(8, 8, 4, getHeight() - 16, 4, 4);
        } else if (hovered) {
            g2.setColor(new Color(255, 255, 255, 12));
            g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 8, 8);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
