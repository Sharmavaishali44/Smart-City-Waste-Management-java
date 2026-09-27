package ui.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import model.Enums.ComplaintStatus;
import utils.UIUtils;

/**
 * Pill-shaped badge component for ComplaintStatus.
 */
public class StatusBadge extends JLabel {

    private ComplaintStatus status;

    public StatusBadge(ComplaintStatus status) {
        super(status != null ? status.getDisplayName() : "New", SwingConstants.CENTER);
        this.status = status;
        setFont(UIUtils.FONT_CAPTION_BOLD);
        setOpaque(false);
        setPreferredSize(new Dimension(85, 24));
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
        setText(status != null ? status.getDisplayName() : "New");
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIUtils.enableAntialiasing(g2);

        ComplaintStatus st = status != null ? status : ComplaintStatus.NEW;
        g2.setColor(st.getBackground());
        g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);

        g2.setColor(st.getForeground());
        setForeground(st.getForeground());
        g2.dispose();

        super.paintComponent(g);
    }
}
