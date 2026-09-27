package ui.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import model.Enums.PriorityLevel;
import utils.UIUtils;

/**
 * Pill-shaped badge component for PriorityLevel.
 */
public class PriorityBadge extends JLabel {

    private PriorityLevel priority;

    public PriorityBadge(PriorityLevel priority) {
        super(priority != null ? priority.name() : "MEDIUM", SwingConstants.CENTER);
        this.priority = priority;
        setFont(UIUtils.FONT_CAPTION_BOLD);
        setOpaque(false);
        setPreferredSize(new Dimension(80, 24));
    }

    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
        setText(priority != null ? priority.name() : "MEDIUM");
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIUtils.enableAntialiasing(g2);

        PriorityLevel pr = priority != null ? priority : PriorityLevel.MEDIUM;
        g2.setColor(pr.getBackground());
        g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);

        g2.setColor(pr.getForeground());
        setForeground(pr.getForeground());
        g2.dispose();

        super.paintComponent(g);
    }
}
