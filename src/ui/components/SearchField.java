package ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import utils.UIUtils;

/**
 * Modern rounded search field with placeholder, icon, and clear button.
 */
public class SearchField extends JPanel {

    private final JTextField textField;
    private final JLabel clearLabel;
    private boolean isFocused = false;

    public SearchField(String placeholder) {
        setLayout(new BorderLayout(8, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(280, 38));
        setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 10));

        // Magnifying glass icon indicator
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(UIUtils.FONT_BODY);
        searchIcon.setForeground(UIUtils.TEXT_MUTED);

        textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    UIUtils.enableAntialiasing(g2);
                    g2.setFont(UIUtils.FONT_BODY);
                    g2.setColor(UIUtils.TEXT_PLACEHOLDER);
                    g2.drawString(placeholder, 2, getHeight() / 2 + 5);
                    g2.dispose();
                }
            }
        };

        textField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        textField.setFont(UIUtils.FONT_BODY);
        textField.setForeground(UIUtils.TEXT_MAIN);
        textField.setOpaque(false);
        textField.setCaretColor(UIUtils.COLOR_PRIMARY);

        clearLabel = new JLabel("✕");
        clearLabel.setFont(UIUtils.FONT_CAPTION_BOLD);
        clearLabel.setForeground(UIUtils.TEXT_MUTED);
        clearLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearLabel.setVisible(false);

        clearLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textField.setText("");
                clearLabel.setVisible(false);
                textField.requestFocusInWindow();
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                clearLabel.setVisible(!textField.getText().isEmpty());
            }
        });

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });

        add(searchIcon, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);
        add(clearLabel, BorderLayout.EAST);
    }

    public String getText() {
        return textField.getText().trim();
    }

    public void setText(String text) {
        textField.setText(text);
        clearLabel.setVisible(text != null && !text.isEmpty());
    }

    public JTextField getTextField() {
        return textField;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIUtils.enableAntialiasing(g2);

        // White background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        // Border (accent if focused)
        g2.setColor(isFocused ? UIUtils.COLOR_PRIMARY : UIUtils.BORDER_LIGHT);
        g2.setStroke(new java.awt.BasicStroke(isFocused ? 1.5f : 1.0f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        g2.dispose();
        super.paintComponent(g);
    }
}
