package ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import utils.UIUtils;

/**
 * Modern flat rounded button with hover state animation and color variants.
 */
public class ModernButton extends JButton {

    public enum ButtonStyle {
        PRIMARY(UIUtils.COLOR_PRIMARY, UIUtils.COLOR_PRIMARY_HOVER, Color.WHITE, null),
        SECONDARY(new Color(241, 245, 249), new Color(226, 232, 240), UIUtils.TEXT_MAIN, UIUtils.BORDER_LIGHT),
        DANGER(new Color(239, 68, 68), new Color(220, 38, 38), Color.WHITE, null),
        SUCCESS(new Color(16, 185, 129), new Color(5, 150, 105), Color.WHITE, null),
        DARK(UIUtils.NAVY_SURFACE, UIUtils.NAVY_LIGHT, Color.WHITE, null),
        OUTLINE(new Color(0,0,0,0), new Color(241, 245, 249), UIUtils.COLOR_PRIMARY, UIUtils.COLOR_PRIMARY);

        final Color normalBg;
        final Color hoverBg;
        final Color textCol;
        final Color borderCol;

        ButtonStyle(Color normalBg, Color hoverBg, Color textCol, Color borderCol) {
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.textCol = textCol;
            this.borderCol = borderCol;
        }
    }

    private ButtonStyle style;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 8;

    public ModernButton(String text) {
        this(text, ButtonStyle.PRIMARY);
    }

    public ModernButton(String text, ButtonStyle style) {
        super(text);
        this.style = style;
        initButton();
    }

    public ModernButton(String text, ButtonStyle style, int radius) {
        super(text);
        this.style = style;
        this.cornerRadius = radius;
        initButton();
    }

    private void initButton() {
        setFont(UIUtils.FONT_BODY_BOLD);
        setForeground(style.textCol);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(getPreferredSize().width + 24, 38));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setButtonStyle(ButtonStyle style) {
        this.style = style;
        setForeground(style.textCol);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIUtils.enableAntialiasing(g2);

        Color bg;
        if (!isEnabled()) {
            bg = new Color(226, 232, 240);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            setForeground(new Color(148, 163, 184));
            super.paintComponent(g);
            g2.dispose();
            return;
        }

        if (isPressed) {
            bg = style.hoverBg.darker();
        } else if (isHovered) {
            bg = style.hoverBg;
        } else {
            bg = style.normalBg;
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        if (style.borderCol != null) {
            g2.setColor(style.borderCol);
            g2.setStroke(new java.awt.BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
