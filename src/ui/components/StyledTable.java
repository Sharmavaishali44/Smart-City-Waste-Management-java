package ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import model.Enums.ComplaintStatus;
import model.Enums.PriorityLevel;
import utils.UIUtils;

/**
 * Modernized JTable with custom header, generous row height, hover effects,
 * alternating row colors, and built-in badge rendering.
 */
public class StyledTable extends JTable {

    private int hoveredRow = -1;

    public StyledTable(TableModel model) {
        super(model);
        initTable();
    }

    public StyledTable() {
        super();
        initTable();
    }

    private void initTable() {
        setRowHeight(42);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(UIUtils.BORDER_LIGHT);
        setFont(UIUtils.FONT_BODY);
        setForeground(UIUtils.TEXT_MAIN);
        setSelectionBackground(new Color(224, 231, 255)); // Soft indigo highlight
        setSelectionForeground(UIUtils.TEXT_MAIN);
        setFillsViewportHeight(true);
        setIntercellSpacing(new Dimension(0, 1));

        // Header Styling
        JTableHeader header = getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
        header.setFont(UIUtils.FONT_BODY_BOLD);
        header.setBackground(UIUtils.NAVY_SURFACE);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setBackground(UIUtils.NAVY_SURFACE);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(UIUtils.FONT_BODY_BOLD);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return lbl;
            }
        });

        // Hover tracking
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                int row = rowAtPoint(p);
                if (row != hoveredRow) {
                    hoveredRow = row;
                    repaint();
                }
            }
        });
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);

        Object val = getValueAt(row, column);

        // Built-in rendering for Status Badge
        if (val instanceof ComplaintStatus) {
            return new StatusBadge((ComplaintStatus) val);
        } else if (val instanceof String && isStatusString((String) val)) {
            return new StatusBadge(ComplaintStatus.fromString((String) val));
        }

        // Built-in rendering for Priority Badge
        if (val instanceof PriorityLevel) {
            return new PriorityBadge((PriorityLevel) val);
        } else if (val instanceof String && isPriorityString((String) val)) {
            return new PriorityBadge(PriorityLevel.fromString((String) val));
        }

        // Row Coloring
        if (!isRowSelected(row)) {
            if (row == hoveredRow) {
                c.setBackground(UIUtils.BG_HOVER);
            } else if (row % 2 == 1) {
                c.setBackground(new Color(248, 250, 252));
            } else {
                c.setBackground(Color.WHITE);
            }
            c.setForeground(UIUtils.TEXT_MAIN);
        }

        if (c instanceof JLabel) {
            JLabel lbl = (JLabel) c;
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        }

        return c;
    }

    private boolean isStatusString(String str) {
        if (str == null) return false;
        String s = str.trim().toUpperCase();
        return s.equals("NEW") || s.equals("ASSIGNED") || s.equals("IN_PROGRESS") ||
               s.equals("RESOLVED") || s.equals("REJECTED") || s.equals("IN PROGRESS");
    }

    private boolean isPriorityString(String str) {
        if (str == null) return false;
        String s = str.trim().toUpperCase();
        return s.equals("CRITICAL") || s.equals("HIGH") || s.equals("MEDIUM") || s.equals("LOW");
    }
}
