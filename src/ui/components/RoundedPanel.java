package ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import utils.UIUtils;

/**
 * Modern rounded card panel with anti-aliasing and optional border / subtle drop shadow.
 */
public class RoundedPanel extends JPanel {

    private int cornerRadius;
    private Color backgroundColor;
    private Color borderColor;
    private int borderWidth;
    private boolean drawShadow;

    public RoundedPanel() {
        this(12, UIUtils.BG_CARD, UIUtils.BORDER_LIGHT, 1, false);
    }

    public RoundedPanel(int radius) {
        this(radius, UIUtils.BG_CARD, UIUtils.BORDER_LIGHT, 1, false);
    }

    public RoundedPanel(int radius, Color bg) {
        this(radius, bg, UIUtils.BORDER_LIGHT, 1, false);
    }

    public RoundedPanel(int radius, Color bg, Color border, int borderWidth, boolean shadow) {
        this.cornerRadius = radius;
        this.backgroundColor = bg;
        this.borderColor = border;
        this.borderWidth = borderWidth;
        this.drawShadow = shadow;
        setOpaque(false);
    }

    public RoundedPanel(LayoutManager layout, int radius, Color bg) {
        super(layout);
        this.cornerRadius = radius;
        this.backgroundColor = bg;
        this.borderColor = UIUtils.BORDER_LIGHT;
        this.borderWidth = 1;
        this.drawShadow = false;
        setOpaque(false);
    }

    public void setBackgroundColor(Color bg) {
        this.backgroundColor = bg;
        repaint();
    }

    public void setBorderColor(Color border) {
        this.borderColor = border;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        UIUtils.enableAntialiasing(g2);

        int width = getWidth();
        int height = getHeight();

        // Optional subtle shadow
        if (drawShadow) {
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fillRoundRect(2, 4, width - 4, height - 4, cornerRadius + 2, cornerRadius + 2);
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fillRoundRect(1, 2, width - 2, height - 2, cornerRadius + 1, cornerRadius + 1);
        }

        int offset = drawShadow ? 2 : 0;
        int drawW = width - (offset * 2) - 1;
        int drawH = height - (offset * 2) - 1;

        // Background
        g2.setColor(backgroundColor != null ? backgroundColor : getBackground());
        g2.fillRoundRect(offset, offset, drawW, drawH, cornerRadius, cornerRadius);

        // Border
        if (borderWidth > 0 && borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new java.awt.BasicStroke(borderWidth));
            g2.drawRoundRect(offset, offset, drawW, drawH, cornerRadius, cornerRadius);
        }

        g2.dispose();
    }
}
