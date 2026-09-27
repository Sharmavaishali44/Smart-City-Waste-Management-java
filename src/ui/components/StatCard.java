package ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import utils.UIUtils;

/**
 * Modern KPI Statistic Card for dashboards.
 */
public class StatCard extends RoundedPanel {

    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private final Color accentColor;

    public StatCard(String title, String value, String subtitle, Color accentColor) {
        super(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
        this.accentColor = accentColor;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(16, 20, 16, 20));
        setPreferredSize(new Dimension(190, 115));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UIUtils.FONT_CAPTION_BOLD);
        titleLabel.setForeground(UIUtils.TEXT_MUTED);

        valueLabel = new JLabel(value);
        valueLabel.setFont(UIUtils.FONT_STAT_NUM);
        valueLabel.setForeground(UIUtils.TEXT_MAIN);

        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(UIUtils.FONT_CAPTION);
        subtitleLabel.setForeground(accentColor);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(valueLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitleLabel);

        add(content, BorderLayout.CENTER);
    }

    public void setValue(String val) {
        valueLabel.setText(val);
        repaint();
    }

    public void setSubtitle(String sub) {
        subtitleLabel.setText(sub);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Left accent bar
        Graphics2D g2 = (Graphics2D) g.create();
        UIUtils.enableAntialiasing(g2);
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 8, 4, getHeight() - 16, 4, 4);
        g2.dispose();
    }
}
