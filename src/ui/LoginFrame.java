package ui;

import config.DBConnection;
import config.DatabaseInitializer;
import dao.CrewDAO;
import dao.UserDAO;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import model.Crew;
import model.Enums.UserRole;
import model.User;
import service.AuditService;
import ui.components.ModernButton;
import ui.components.ModernButton.ButtonStyle;
import ui.components.NotificationToast;
import ui.components.NotificationToast.ToastType;
import ui.components.RoundedPanel;
import utils.SessionManager;
import utils.UIUtils;
import utils.ValidationUtils;

/**
 * World-class Smart City Municipal Portal Login & Account Creation Screen.
 */
public class LoginFrame extends JFrame {

    private final UserDAO userDAO;
    private final CrewDAO crewDAO;

    private JPanel formContainer;
    private CardLayout cardLayout;

    // Segmented tab buttons
    private JButton tabSignInBtn;
    private JButton tabSignUpBtn;

    // Sign In Fields
    private JTextField loginUserField;
    private JPasswordField loginPassField;
    private JLabel loginErrorLabel;
    private boolean loginPassVisible = false;
    private JButton loginEyeBtn;

    // Sign Up Fields (Strictly CITIZEN registration)
    private JTextField regNameField;
    private JTextField regUserField;
    private JTextField regPhoneField;
    private JTextField regEmailField;
    private JPasswordField regPassField;
    private JPasswordField regConfirmPassField;
    private JLabel regErrorLabel;
    private boolean regPassVisible = false;
    private JButton regEyeBtn;

    // DB Status pill
    private JLabel dbStatusPill;

    public LoginFrame() {
        this.userDAO = new UserDAO();
        this.crewDAO = new CrewDAO();

        setTitle("Smart City Waste Management & Municipal Complaint Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1060, 680);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);

        initUI();
        checkDatabaseStatusAsync();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG_LIGHT);

        // --- Left Hero Panel (Gradient & Smart City Branding) ---
        root.add(createHeroPanel(), BorderLayout.WEST);

        // --- Right Interactive Panel ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));

        // Top Segmented Switcher: [ Sign In ] | [ Create Account ]
        rightPanel.add(createSegmentedTabBar(), BorderLayout.NORTH);

        // Form Container with CardLayout
        cardLayout = new CardLayout();
        formContainer = new JPanel(cardLayout);
        formContainer.setOpaque(false);

        formContainer.add(createSignInPanel(), "SIGN_IN");
        formContainer.add(createSignUpPanel(), "SIGN_UP");
        formContainer.add(createDatabaseConfigPanel(), "DB_SETUP");

        rightPanel.add(formContainer, BorderLayout.CENTER);
        root.add(rightPanel, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createHeroPanel() {
        JPanel hero = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UIUtils.enableAntialiasing(g2);
                // Premium deep navy to midnight indigo gradient
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(11, 17, 32),
                    getWidth(), getHeight(), new Color(24, 24, 52)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle glowing emerald accent orb on top right
                g2.setColor(new Color(16, 185, 129, 20));
                g2.fillOval(getWidth() - 140, -40, 220, 220);

                g2.dispose();
            }
        };

        hero.setPreferredSize(new Dimension(440, 640));
        hero.setBorder(BorderFactory.createEmptyBorder(36, 32, 32, 32));

        // Top Brand & Title
        JPanel topContent = new JPanel();
        topContent.setOpaque(false);
        topContent.setLayout(new BoxLayout(topContent, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("♻️ SMART CITY MUNICIPAL CORPORATION");
        badge.setFont(UIUtils.FONT_CAPTION_BOLD);
        badge.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel h1 = new JLabel("<html><b style='font-size:22px; color:#FFFFFF;'>Waste Collection &amp;<br>Municipal Complaints</b></html>");
        h1.setFont(UIUtils.FONT_HEADER_LARGE);

        JLabel sub = new JLabel("<html><body style='width:340px; color:#94A3B8; font-size:12px;'>" +
            "Intelligent municipal operations featuring AI-assisted priority scoring, automatic area-wise crew routing, and real-time citizen grievance resolution.</body></html>");
        sub.setFont(UIUtils.FONT_BODY);

        topContent.add(badge);
        topContent.add(Box.createVerticalStrut(12));
        topContent.add(h1);
        topContent.add(Box.createVerticalStrut(10));
        topContent.add(sub);
        topContent.add(Box.createVerticalStrut(24));

        // Feature Highlights Pills
        topContent.add(createFeatureItem("🤖", "AI Priority Scoring", "Auto-evaluates health risks & garbage overflow"));
        topContent.add(Box.createVerticalStrut(10));
        topContent.add(createFeatureItem("🗺️", "Smart Area-Wise Routing", "Dispatches available crews based on live workload"));
        topContent.add(Box.createVerticalStrut(10));
        topContent.add(createFeatureItem("📊", "Control Center Analytics", "Problem hotspots & resolution turnaround tracking"));

        hero.add(topContent, BorderLayout.NORTH);

        // Center Quick Demo Accounts
        JPanel centerContent = new JPanel();
        centerContent.setOpaque(false);
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.add(Box.createVerticalStrut(16));

        RoundedPanel quickFillCard = new RoundedPanel(10, new Color(255, 255, 255, 12), new Color(255, 255, 255, 20), 1, false);
        quickFillCard.setLayout(new BoxLayout(quickFillCard, BoxLayout.Y_AXIS));
        quickFillCard.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel qTitle = new JLabel("⚡ 1-CLICK DEMO LOGIN (OR CREATE NEW)");
        qTitle.setFont(UIUtils.FONT_CAPTION_BOLD);
        qTitle.setForeground(UIUtils.COLOR_ACCENT_CYAN);
        quickFillCard.add(qTitle);
        quickFillCard.add(Box.createVerticalStrut(8));

        quickFillCard.add(createQuickFillBtn("👑 Municipal Admin", "admin@smartcityjaipur.gov.in", "Admin@2026"));
        quickFillCard.add(Box.createVerticalStrut(4));
        quickFillCard.add(createQuickFillBtn("👤 Citizen User", "rahul", "password123"));
        quickFillCard.add(Box.createVerticalStrut(4));
        quickFillCard.add(createQuickFillBtn("🚛 Collection Crew", "crew_mansarovar", "crew123"));

        centerContent.add(quickFillCard);
        hero.add(centerContent, BorderLayout.CENTER);

        // Bottom MySQL Status & Settings
        JPanel bottomHero = new JPanel(new BorderLayout(8, 0));
        bottomHero.setOpaque(false);

        dbStatusPill = new JLabel("● Checking MySQL...");
        dbStatusPill.setFont(UIUtils.FONT_CAPTION_BOLD);
        dbStatusPill.setForeground(UIUtils.TEXT_MUTED);

        ModernButton dbBtn = new ModernButton("⚙️ DB Setup", ButtonStyle.DARK, 6);
        dbBtn.setPreferredSize(new Dimension(100, 32));
        dbBtn.addActionListener(e -> {
            highlightTab(null);
            cardLayout.show(formContainer, "DB_SETUP");
        });

        bottomHero.add(dbStatusPill, BorderLayout.WEST);
        bottomHero.add(dbBtn, BorderLayout.EAST);

        hero.add(bottomHero, BorderLayout.SOUTH);
        return hero;
    }

    private JPanel createFeatureItem(String icon, String title, String desc) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);

        JLabel ic = new JLabel(icon);
        ic.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(UIUtils.FONT_BODY_BOLD);
        t.setForeground(Color.WHITE);

        JLabel d = new JLabel(desc);
        d.setFont(UIUtils.FONT_CAPTION);
        d.setForeground(new Color(148, 163, 184));

        text.add(t);
        text.add(d);

        p.add(ic);
        p.add(text);
        return p;
    }

    private JButton createQuickFillBtn(String label, String user, String pass) {
        JButton btn = new JButton("<html>" + label + ": <b style='color:#38BDF8;'>" + user + "</b></html>");
        btn.setFont(UIUtils.FONT_CAPTION);
        btn.setForeground(new Color(226, 232, 240));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.addActionListener(e -> {
            highlightTab(tabSignInBtn);
            cardLayout.show(formContainer, "SIGN_IN");
            loginUserField.setText(user);
            loginPassField.setText(pass);
            loginPassField.requestFocusInWindow();
        });
        return btn;
    }

    private JPanel createSegmentedTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        tabSignInBtn = createTabButton("🔐 Sign In to Account", true);
        tabSignUpBtn = createTabButton("✍️ Citizen Registration", false);

        tabSignInBtn.addActionListener(e -> {
            highlightTab(tabSignInBtn);
            cardLayout.show(formContainer, "SIGN_IN");
        });

        tabSignUpBtn.addActionListener(e -> {
            highlightTab(tabSignUpBtn);
            cardLayout.show(formContainer, "SIGN_UP");
        });

        bar.add(tabSignInBtn);
        bar.add(tabSignUpBtn);
        return bar;
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UIUtils.FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(190, 38));
        updateTabStyle(btn, active);
        return btn;
    }

    private void updateTabStyle(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(UIUtils.COLOR_PRIMARY);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(241, 245, 249));
            btn.setForeground(UIUtils.TEXT_MUTED);
        }
    }

    private void highlightTab(JButton activeBtn) {
        updateTabStyle(tabSignInBtn, tabSignInBtn == activeBtn);
        updateTabStyle(tabSignUpBtn, tabSignUpBtn == activeBtn);
    }

    // -------------------------------------------------------------
    // 1. Sign In Panel
    // -------------------------------------------------------------
    private JPanel createSignInPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 4, 0);

        JLabel header = new JLabel("Welcome Back");
        header.setFont(UIUtils.FONT_HEADER_LARGE);
        header.setForeground(UIUtils.TEXT_MAIN);
        panel.add(header, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        JLabel sub = new JLabel("Enter your credentials to access municipal services");
        sub.setFont(UIUtils.FONT_BODY);
        sub.setForeground(UIUtils.TEXT_MUTED);
        panel.add(sub, gbc);

        // Error message banner
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        loginErrorLabel = new JLabel();
        loginErrorLabel.setFont(UIUtils.FONT_CAPTION_BOLD);
        loginErrorLabel.setForeground(UIUtils.PRIORITY_CRITICAL);
        loginErrorLabel.setVisible(false);
        panel.add(loginErrorLabel, gbc);

        // Username or Email
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(createFieldLabel("Username or Official Email"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 14, 0);
        loginUserField = new JTextField("admin@smartcityjaipur.gov.in");
        UIUtils.styleTextField(loginUserField);
        loginUserField.setPreferredSize(new Dimension(320, 40));
        panel.add(loginUserField, gbc);

        // Password with Eye Toggle
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        panel.add(createFieldLabel("Password"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 22, 0);
        JPanel passRow = new JPanel(new BorderLayout(6, 0));
        passRow.setOpaque(false);

        loginPassField = new JPasswordField("Admin@2026");
        UIUtils.styleTextField(loginPassField);
        loginPassField.setPreferredSize(new Dimension(280, 40));
        loginPassField.addActionListener(e -> performLogin());

        loginEyeBtn = new JButton("👁️");
        loginEyeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginEyeBtn.setFocusPainted(false);
        loginEyeBtn.setBackground(new Color(241, 245, 249));
        loginEyeBtn.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_LIGHT, 1));
        loginEyeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginEyeBtn.setPreferredSize(new Dimension(42, 40));
        loginEyeBtn.addActionListener(e -> {
            loginPassVisible = !loginPassVisible;
            loginPassField.setEchoChar(loginPassVisible ? (char) 0 : '•');
        });

        passRow.add(loginPassField, BorderLayout.CENTER);
        passRow.add(loginEyeBtn, BorderLayout.EAST);
        panel.add(passRow, gbc);

        // Sign In Button
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 14, 0);
        ModernButton loginBtn = new ModernButton("Sign In  ➔", ButtonStyle.PRIMARY, 10);
        loginBtn.setPreferredSize(new Dimension(320, 44));
        loginBtn.addActionListener(e -> performLogin());
        panel.add(loginBtn, gbc);

        // Switch to Sign Up text
        gbc.gridy++;
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        switchPanel.setOpaque(false);
        JLabel dontHave = new JLabel("Don't have an account? ");
        dontHave.setFont(UIUtils.FONT_BODY);
        dontHave.setForeground(UIUtils.TEXT_MUTED);

        JLabel createLink = new JLabel("Create Citizen Account");
        createLink.setFont(UIUtils.FONT_BODY_BOLD);
        createLink.setForeground(UIUtils.COLOR_PRIMARY);
        createLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                highlightTab(tabSignUpBtn);
                cardLayout.show(formContainer, "SIGN_UP");
            }
        });

        switchPanel.add(dontHave);
        switchPanel.add(createLink);
        panel.add(switchPanel, gbc);

        return panel;
    }

    // -------------------------------------------------------------
    // 2. Sign Up (Create Account) Panel
    // -------------------------------------------------------------
    private JPanel createSignUpPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 2, 0);

        JLabel header = new JLabel("Create Citizen Account");
        header.setFont(UIUtils.FONT_HEADER_LARGE);
        header.setForeground(UIUtils.TEXT_MAIN);
        panel.add(header, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JLabel sub = new JLabel("Jaipur Municipal Resident Portal: Register to report waste and track cleanups");
        sub.setFont(UIUtils.FONT_BODY);
        sub.setForeground(UIUtils.TEXT_MUTED);
        panel.add(sub, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        regErrorLabel = new JLabel();
        regErrorLabel.setFont(UIUtils.FONT_CAPTION_BOLD);
        regErrorLabel.setForeground(UIUtils.PRIORITY_CRITICAL);
        regErrorLabel.setVisible(false);
        panel.add(regErrorLabel, gbc);

        // Full Name & Username in 2 columns
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 2, 0);
        JPanel row1Labels = new JPanel(new GridLayout(1, 2, 10, 0));
        row1Labels.setOpaque(false);
        row1Labels.add(createFieldLabel("Full Name *"));
        row1Labels.add(createFieldLabel("Username (User ID) *"));
        panel.add(row1Labels, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JPanel row1Fields = new JPanel(new GridLayout(1, 2, 10, 0));
        row1Fields.setOpaque(false);
        regNameField = new JTextField();
        UIUtils.styleTextField(regNameField);
        regUserField = new JTextField();
        UIUtils.styleTextField(regUserField);
        row1Fields.add(regNameField);
        row1Fields.add(regUserField);
        panel.add(row1Fields, gbc);

        // Mobile & Email in 2 columns
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 2, 0);
        JPanel row2Labels = new JPanel(new GridLayout(1, 2, 10, 0));
        row2Labels.setOpaque(false);
        row2Labels.add(createFieldLabel("Mobile Number (10 digits) *"));
        row2Labels.add(createFieldLabel("Email (Optional)"));
        panel.add(row2Labels, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JPanel row2Fields = new JPanel(new GridLayout(1, 2, 10, 0));
        row2Fields.setOpaque(false);
        regPhoneField = new JTextField();
        UIUtils.styleTextField(regPhoneField);
        regEmailField = new JTextField();
        UIUtils.styleTextField(regEmailField);
        row2Fields.add(regPhoneField);
        row2Fields.add(regEmailField);
        panel.add(row2Fields, gbc);

        // Password & Confirm Password in 2 columns
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 2, 0);
        JPanel row3Labels = new JPanel(new GridLayout(1, 2, 10, 0));
        row3Labels.setOpaque(false);
        row3Labels.add(createFieldLabel("Password (min 6 chars) *"));
        row3Labels.add(createFieldLabel("Confirm Password *"));
        panel.add(row3Labels, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 18, 0);
        JPanel row3Fields = new JPanel(new GridLayout(1, 2, 10, 0));
        row3Fields.setOpaque(false);
        regPassField = new JPasswordField();
        UIUtils.styleTextField(regPassField);
        regConfirmPassField = new JPasswordField();
        UIUtils.styleTextField(regConfirmPassField);
        row3Fields.add(regPassField);
        row3Fields.add(regConfirmPassField);
        panel.add(row3Fields, gbc);

        // Submit Button
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        ModernButton submitRegBtn = new ModernButton("🚀 Create Account & Proceed to Login", ButtonStyle.PRIMARY, 10);
        submitRegBtn.setPreferredSize(new Dimension(320, 44));
        submitRegBtn.addActionListener(e -> performRegister());
        panel.add(submitRegBtn, gbc);

        // Back to Sign In Link
        gbc.gridy++;
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backPanel.setOpaque(false);
        JLabel haveAcc = new JLabel("Already registered? ");
        haveAcc.setFont(UIUtils.FONT_BODY);
        haveAcc.setForeground(UIUtils.TEXT_MUTED);

        JLabel loginLink = new JLabel("Sign In here");
        loginLink.setFont(UIUtils.FONT_BODY_BOLD);
        loginLink.setForeground(UIUtils.COLOR_PRIMARY);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                highlightTab(tabSignInBtn);
                cardLayout.show(formContainer, "SIGN_IN");
            }
        });

        backPanel.add(haveAcc);
        backPanel.add(loginLink);
        panel.add(backPanel, gbc);

        return panel;
    }

    // -------------------------------------------------------------
    // 3. Database Config & Setup Panel
    // -------------------------------------------------------------
    private JPanel createDatabaseConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 4, 0);

        JLabel header = new JLabel("MySQL Database Configuration");
        header.setFont(UIUtils.FONT_HEADER_MEDIUM);
        header.setForeground(UIUtils.TEXT_MAIN);
        panel.add(header, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 14, 0);
        JLabel sub = new JLabel("Change MySQL credentials or execute auto-seed scripts");
        sub.setFont(UIUtils.FONT_BODY);
        sub.setForeground(UIUtils.TEXT_MUTED);
        panel.add(sub, gbc);

        // Host & Port
        gbc.gridy++;
        panel.add(createFieldLabel("Host & Port"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        JPanel rowHP = new JPanel(new BorderLayout(8, 0));
        rowHP.setOpaque(false);
        JTextField hostF = new JTextField(DBConnection.getHost());
        UIUtils.styleTextField(hostF);
        JTextField portF = new JTextField(DBConnection.getPort());
        portF.setPreferredSize(new Dimension(80, 38));
        UIUtils.styleTextField(portF);
        rowHP.add(hostF, BorderLayout.CENTER);
        rowHP.add(portF, BorderLayout.EAST);
        panel.add(rowHP, gbc);

        // DB Name
        gbc.gridy++;
        panel.add(createFieldLabel("Database Name"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);
        JTextField nameF = new JTextField(DBConnection.getDatabaseName());
        UIUtils.styleTextField(nameF);
        panel.add(nameF, gbc);

        // User & Password
        gbc.gridy++;
        panel.add(createFieldLabel("Username & Password"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 14, 0);
        JPanel rowUP = new JPanel(new GridLayout(1, 2, 8, 0));
        rowUP.setOpaque(false);
        JTextField userF = new JTextField(DBConnection.getUser());
        UIUtils.styleTextField(userF);
        JPasswordField passF = new JPasswordField(DBConnection.getPassword());
        UIUtils.styleTextField(passF);
        rowUP.add(userF);
        rowUP.add(passF);
        panel.add(rowUP, gbc);

        // Action Buttons
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        ModernButton testBtn = new ModernButton("🔍 Test Connection", ButtonStyle.DARK, 8);
        ModernButton seedBtn = new ModernButton("⚡ Run Schema & Seed", ButtonStyle.PRIMARY, 8);

        testBtn.addActionListener(e -> {
            try {
                DBConnection.saveConfiguration(hostF.getText().trim(), portF.getText().trim(),
                    nameF.getText().trim(), userF.getText().trim(), new String(passF.getPassword()));
                String err = DBConnection.testConnection();
                if (err == null) {
                    dbStatusPill.setText("● MySQL: Connected");
                    dbStatusPill.setForeground(UIUtils.COLOR_PRIMARY);
                    JOptionPane.showMessageDialog(LoginFrame.this, "Connection Successful!", "MySQL Online", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    dbStatusPill.setText("● MySQL: Error");
                    dbStatusPill.setForeground(UIUtils.PRIORITY_CRITICAL);
                    JOptionPane.showMessageDialog(LoginFrame.this, "Connection failed: " + err, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(LoginFrame.this, ex.getMessage());
            }
        });

        seedBtn.addActionListener(e -> {
            try {
                DBConnection.saveConfiguration(hostF.getText().trim(), portF.getText().trim(),
                    nameF.getText().trim(), userF.getText().trim(), new String(passF.getPassword()));
                DatabaseInitializer.initializeDatabase(null);
                JOptionPane.showMessageDialog(LoginFrame.this, "Database schema created and 30+ sample records seeded successfully!");
                highlightTab(tabSignInBtn);
                cardLayout.show(formContainer, "SIGN_IN");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(LoginFrame.this, "Error initializing: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(testBtn);
        btnRow.add(seedBtn);
        panel.add(btnRow, gbc);

        // Back to Sign In
        gbc.gridy++;
        ModernButton backBtn = new ModernButton("← Back to Sign In", ButtonStyle.SECONDARY, 8);
        backBtn.addActionListener(e -> {
            highlightTab(tabSignInBtn);
            cardLayout.show(formContainer, "SIGN_IN");
        });
        panel.add(backBtn, gbc);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIUtils.FONT_CAPTION_BOLD);
        lbl.setForeground(UIUtils.TEXT_MAIN);
        return lbl;
    }

    private void checkDatabaseStatusAsync() {
        new Thread(() -> {
            String err = DBConnection.testConnection();
            SwingUtilities.invokeLater(() -> {
                if (err == null) {
                    dbStatusPill.setText("● MySQL: Connected (port 3306)");
                    dbStatusPill.setForeground(UIUtils.COLOR_PRIMARY);
                } else {
                    dbStatusPill.setText("● MySQL: Disconnected");
                    dbStatusPill.setForeground(UIUtils.PRIORITY_CRITICAL);
                }
            });
        }).start();
    }

    private void performLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword());

        loginErrorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            showLoginError("Please enter both username and password.");
            return;
        }

        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() {
                return userDAO.authenticate(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        SessionManager.setCurrentUser(user);
                        AuditService.logLogin(user);

                        if (user.getRole() == UserRole.CREW) {
                            Crew crew = crewDAO.getCrewByUserId(user.getId());
                            SessionManager.setCurrentCrew(crew);
                        }

                        openDashboard(user);
                    } else {
                        showLoginError("Invalid credentials. Please verify username and password.");
                    }
                } catch (Exception ex) {
                    showLoginError("Login error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void performRegister() {
        String name = regNameField.getText().trim();
        String username = regUserField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String email = regEmailField.getText().trim();
        UserRole role = UserRole.CITIZEN;
        String pass = new String(regPassField.getPassword());
        String confirmPass = new String(regConfirmPassField.getPassword());

        regErrorLabel.setVisible(false);

        if (name.isEmpty() || username.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            showRegError("Please fill in all mandatory fields marked with *.");
            return;
        }

        if (!ValidationUtils.isValidMobile(phone)) {
            showRegError("Please enter a valid 10-digit Indian mobile number.");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showRegError("Please enter a valid email address.");
            return;
        }

        if (pass.length() < 6) {
            showRegError("Password must be at least 6 characters long.");
            return;
        }

        if (!pass.equals(confirmPass)) {
            showRegError("Password and Confirm Password do not match.");
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private String errorMsg = null;

            @Override
            protected Boolean doInBackground() {
                if (userDAO.existsUsername(username)) {
                    errorMsg = "Username '" + username + "' is already taken. Please choose another.";
                    return false;
                }

                User newUser = new User();
                newUser.setFullName(name);
                newUser.setUsername(username);
                newUser.setPhone(ValidationUtils.sanitizePhone(phone));
                newUser.setEmail(email.isEmpty() ? null : email);
                newUser.setRole(UserRole.CITIZEN);

                return userDAO.register(newUser, pass);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(LoginFrame.this,
                            "🎉 Citizen account created successfully for " + name + "!\n" +
                            "You can now log in using username: " + username,
                            "Account Created", JOptionPane.INFORMATION_MESSAGE);

                        // Auto-fill sign in form
                        loginUserField.setText(username);
                        loginPassField.setText(pass);
                        highlightTab(tabSignInBtn);
                        cardLayout.show(formContainer, "SIGN_IN");
                        loginPassField.requestFocusInWindow();
                    } else {
                        showRegError(errorMsg != null ? errorMsg : "Registration failed. Check MySQL connection.");
                    }
                } catch (Exception ex) {
                    showRegError("Error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void openDashboard(User user) {
        dispose();
        switch (user.getRole()) {
            case ADMIN:
                new AdminDashboard().setVisible(true);
                break;
            case CREW:
                new CrewDashboard().setVisible(true);
                break;
            case CITIZEN:
            default:
                new CitizenDashboard().setVisible(true);
                break;
        }
    }

    private void showLoginError(String message) {
        loginErrorLabel.setText("⚠️ " + message);
        loginErrorLabel.setVisible(true);
    }

    private void showRegError(String message) {
        regErrorLabel.setText("⚠️ " + message);
        regErrorLabel.setVisible(true);
    }
}
