package ui.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import utils.UIUtils;

/**
 * Lightweight, beautiful Swing Graphics2D chart panels.
 */
public class CustomChartPanel {

    private static final Color[] PALETTE = {
        new Color(16, 185, 129),  // Emerald
        new Color(59, 130, 246),  // Blue
        new Color(245, 158, 11),  // Amber
        new Color(239, 68, 68),   // Red
        new Color(139, 92, 246),  // Purple
        new Color(6, 182, 212),   // Cyan
        new Color(236, 72, 153),  // Pink
        new Color(100, 116, 139)  // Slate
    };

    /**
     * Modern Donut Chart with interactive legend.
     */
    public static class DonutChartPanel extends RoundedPanel {
        private Map<String, Integer> data;
        private String chartTitle;

        public DonutChartPanel(String title, Map<String, Integer> data) {
            super(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
            this.chartTitle = title;
            this.data = data;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            setPreferredSize(new Dimension(380, 260));
        }

        public void setData(Map<String, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            UIUtils.enableAntialiasing(g2);

            // Title
            g2.setFont(UIUtils.FONT_HEADER_SMALL);
            g2.setColor(UIUtils.TEXT_MAIN);
            g2.drawString(chartTitle, 20, 30);

            if (data == null || data.isEmpty()) {
                g2.setFont(UIUtils.FONT_BODY);
                g2.setColor(UIUtils.TEXT_MUTED);
                g2.drawString("No data available", getWidth() / 2 - 50, getHeight() / 2);
                g2.dispose();
                return;
            }

            int total = 0;
            for (int v : data.values()) total += v;
            if (total == 0) total = 1;

            int size = Math.min(getWidth() - 200, getHeight() - 70);
            int x = 25;
            int y = 50;

            double startAngle = 90.0;
            int colorIdx = 0;

            // Draw donut slices
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                double arcAngle = (entry.getValue() / (double) total) * 360.0;
                Color col = PALETTE[colorIdx % PALETTE.length];

                g2.setColor(col);
                g2.fill(new Arc2D.Double(x, y, size, size, startAngle, -arcAngle, Arc2D.PIE));

                startAngle -= arcAngle;
                colorIdx++;
            }

            // Cut out donut center
            int innerHole = (int) (size * 0.60);
            int holeX = x + (size - innerHole) / 2;
            int holeY = y + (size - innerHole) / 2;
            g2.setColor(Color.WHITE);
            g2.fillOval(holeX, holeY, innerHole, innerHole);

            // Center metric
            g2.setFont(UIUtils.FONT_BODY_BOLD);
            g2.setColor(UIUtils.TEXT_MAIN);
            String totalStr = String.valueOf(total);
            int strW = g2.getFontMetrics().stringWidth(totalStr);
            g2.drawString(totalStr, holeX + (innerHole - strW) / 2, holeY + (innerHole / 2) + 2);

            g2.setFont(UIUtils.FONT_CAPTION);
            g2.setColor(UIUtils.TEXT_MUTED);
            String subStr = "TOTAL";
            int subW = g2.getFontMetrics().stringWidth(subStr);
            g2.drawString(subStr, holeX + (innerHole - subW) / 2, holeY + (innerHole / 2) + 16);

            // Legend on the right
            int legX = x + size + 30;
            int legY = 65;
            colorIdx = 0;
            g2.setFont(UIUtils.FONT_CAPTION);

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                if (colorIdx >= 6) break; // Limit legend items for clean UI
                Color col = PALETTE[colorIdx % PALETTE.length];

                g2.setColor(col);
                g2.fillRoundRect(legX, legY - 9, 10, 10, 3, 3);

                g2.setColor(UIUtils.TEXT_MAIN);
                String label = entry.getKey();
                if (label.length() > 16) label = label.substring(0, 14) + "..";
                int pct = (int) Math.round((entry.getValue() / (double) total) * 100);
                g2.drawString(label + " (" + pct + "%)", legX + 16, legY);

                legY += 24;
                colorIdx++;
            }

            g2.dispose();
        }
    }

    /**
     * Modern Horizontal Bar Chart for Area density.
     */
    public static class HorizontalBarChartPanel extends RoundedPanel {
        private Map<String, Integer> data;
        private String chartTitle;

        public HorizontalBarChartPanel(String title, Map<String, Integer> data) {
            super(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
            this.chartTitle = title;
            this.data = data;
            setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            setPreferredSize(new Dimension(380, 260));
        }

        public void setData(Map<String, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            UIUtils.enableAntialiasing(g2);

            g2.setFont(UIUtils.FONT_HEADER_SMALL);
            g2.setColor(UIUtils.TEXT_MAIN);
            g2.drawString(chartTitle, 20, 30);

            if (data == null || data.isEmpty()) {
                g2.setFont(UIUtils.FONT_BODY);
                g2.setColor(UIUtils.TEXT_MUTED);
                g2.drawString("No data available", getWidth() / 2 - 50, getHeight() / 2);
                g2.dispose();
                return;
            }

            int maxVal = 1;
            for (int v : data.values()) if (v > maxVal) maxVal = v;

            int startY = 60;
            int barHeight = 16;
            int barMaxW = getWidth() - 180;
            int labelW = 110;

            int idx = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                if (idx >= 6) break;

                // Label
                g2.setFont(UIUtils.FONT_CAPTION_BOLD);
                g2.setColor(UIUtils.TEXT_MAIN);
                String area = entry.getKey();
                if (area.length() > 14) area = area.substring(0, 12) + "..";
                g2.drawString(area, 20, startY + 12);

                // Bar Background
                int barX = 20 + labelW;
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(barX, startY, barMaxW, barHeight, 8, 8);

                // Active Bar with Gradient
                int valW = (int) ((entry.getValue() / (double) maxVal) * barMaxW);
                if (valW < 8 && entry.getValue() > 0) valW = 8;

                Color startCol = PALETTE[idx % PALETTE.length];
                Color endCol = new Color(Math.min(255, startCol.getRed() + 40),
                                         Math.min(255, startCol.getGreen() + 40),
                                         Math.min(255, startCol.getBlue() + 40));
                g2.setPaint(new GradientPaint(barX, startY, startCol, barX + valW, startY, endCol));
                g2.fillRoundRect(barX, startY, valW, barHeight, 8, 8);

                // Value Text
                g2.setFont(UIUtils.FONT_CAPTION_BOLD);
                g2.setColor(UIUtils.TEXT_MUTED);
                g2.drawString(String.valueOf(entry.getValue()), barX + barMaxW + 10, startY + 13);

                startY += 30;
                idx++;
            }

            g2.dispose();
        }
    }

    /**
     * Modern Trend Line Chart with smooth area fill.
     */
    public static class TrendLineChartPanel extends RoundedPanel {
        private Map<String, Integer> trendData;
        private String chartTitle;

        public TrendLineChartPanel(String title, Map<String, Integer> data) {
            super(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
            this.chartTitle = title;
            this.trendData = data;
            setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
            setPreferredSize(new Dimension(380, 260));
        }

        public void setData(Map<String, Integer> data) {
            this.trendData = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            UIUtils.enableAntialiasing(g2);

            g2.setFont(UIUtils.FONT_HEADER_SMALL);
            g2.setColor(UIUtils.TEXT_MAIN);
            g2.drawString(chartTitle, 20, 30);

            if (trendData == null || trendData.isEmpty()) {
                g2.setFont(UIUtils.FONT_BODY);
                g2.setColor(UIUtils.TEXT_MUTED);
                g2.drawString("No trend data available", getWidth() / 2 - 60, getHeight() / 2);
                g2.dispose();
                return;
            }

            List<String> keys = new ArrayList<>(trendData.keySet());
            List<Integer> values = new ArrayList<>(trendData.values());

            int maxVal = 1;
            for (int v : values) if (v > maxVal) maxVal = v;
            maxVal = (int) Math.ceil(maxVal * 1.2); // headroom

            int chartX = 40;
            int chartY = 55;
            int chartW = getWidth() - 70;
            int chartH = getHeight() - 100;

            // Draw horizontal grid lines
            g2.setColor(new Color(241, 245, 249));
            for (int i = 0; i <= 4; i++) {
                int gy = chartY + (int) (chartH * (i / 4.0));
                g2.drawLine(chartX, gy, chartX + chartW, gy);
            }

            int n = keys.size();
            int[] px = new int[n];
            int[] py = new int[n];

            for (int i = 0; i < n; i++) {
                px[i] = chartX + (int) ((i / (double) Math.max(1, n - 1)) * chartW);
                py[i] = chartY + chartH - (int) ((values.get(i) / (double) maxVal) * chartH);
            }

            // Draw Area Fill Under Line
            Path2D.Double areaPath = new Path2D.Double();
            areaPath.moveTo(px[0], chartY + chartH);
            for (int i = 0; i < n; i++) {
                areaPath.lineTo(px[i], py[i]);
            }
            areaPath.lineTo(px[n - 1], chartY + chartH);
            areaPath.closePath();

            GradientPaint areaGrad = new GradientPaint(
                chartX, chartY, new Color(16, 185, 129, 70),
                chartX, chartY + chartH, new Color(16, 185, 129, 5)
            );
            g2.setPaint(areaGrad);
            g2.fill(areaPath);

            // Draw connecting line
            g2.setColor(UIUtils.COLOR_PRIMARY);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < n - 1; i++) {
                g2.drawLine(px[i], py[i], px[i + 1], py[i + 1]);
            }

            // Draw Point Circles & Labels
            g2.setFont(UIUtils.FONT_CAPTION);
            for (int i = 0; i < n; i++) {
                // Outer circle
                g2.setColor(Color.WHITE);
                g2.fillOval(px[i] - 5, py[i] - 5, 10, 10);
                // Inner dot
                g2.setColor(UIUtils.COLOR_PRIMARY);
                g2.fillOval(px[i] - 3, py[i] - 3, 6, 6);

                // Date label below
                g2.setColor(UIUtils.TEXT_MUTED);
                String lbl = keys.get(i);
                int strW = g2.getFontMetrics().stringWidth(lbl);
                g2.drawString(lbl, px[i] - (strW / 2), chartY + chartH + 18);

                // Value above dot
                g2.setColor(UIUtils.TEXT_MAIN);
                String valStr = String.valueOf(values.get(i));
                int valW = g2.getFontMetrics().stringWidth(valStr);
                g2.drawString(valStr, px[i] - (valW / 2), py[i] - 8);
            }

            g2.dispose();
        }
    }
}
