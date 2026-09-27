package ui;

import dao.AreaDAO;
import dao.ComplaintDAO;
import dao.FeedbackDAO;
import dao.StatusHistoryDAO;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Area;
import model.Complaint;
import model.Enums.ComplaintCategory;
import model.Enums.ComplaintStatus;
import model.Enums.PriorityLevel;
import model.Feedback;
import model.StatusHistory;
import model.User;
import model.Ward;
import service.ComplaintService;
import service.PriorityEngine;
import ui.components.ModernButton;
import ui.components.ModernButton.ButtonStyle;
import ui.components.NotificationToast;
import ui.components.NotificationToast.ToastType;
import ui.components.PriorityBadge;
import ui.components.RoundedPanel;
import ui.components.SidebarButton;
import ui.components.StatusBadge;
import ui.components.StyledTable;
import utils.SessionManager;
import utils.UIUtils;
import utils.ValidationUtils;

/**
 * Citizen Self-Service Portal for Municipal Waste Management.
 */
public class CitizenDashboard extends JFrame {

    private final ComplaintService complaintService;
    private final ComplaintDAO complaintDAO;
    private final AreaDAO areaDAO;
    private final StatusHistoryDAO statusHistoryDAO;
    private final FeedbackDAO feedbackDAO;
    private final User currentUser;

    private JPanel mainContent;
    private CardLayout contentLayout;
    private List<SidebarButton> sidebarButtons = new ArrayList<>();

    // Form inputs
    private JComboBox<ComplaintCategory> categoryCombo;
    private JComboBox<Area> areaCombo;
    private JComboBox<Ward> wardCombo;
    private JTextField localityField;
    private JTextField addressField;
    private JTextArea descArea;
    private JCheckBox healthRiskCheck;
    private JCheckBox overflowCheck;
    private JCheckBox sensitiveCheck;
    private JTextField imagePathField;
    private JLabel previewScoreLabel;
    private JLabel previewLevelLabel;
    private JLabel previewReasonLabel;

    // My complaints table
    private StyledTable complaintsTable;
    private DefaultTableModel tableModel;

    // Track panel inputs
    private JTextField trackCodeField;
    private JPanel trackResultContainer;

    public CitizenDashboard() {
        this.complaintService = new ComplaintService();
        this.complaintDAO = new ComplaintDAO();
        this.areaDAO = new AreaDAO();
        this.statusHistoryDAO = new StatusHistoryDAO();
        this.feedbackDAO = new FeedbackDAO();
        this.currentUser = SessionManager.getCurrentUser();

        setTitle("Smart City Waste Portal - Citizen Dashboard (" + (currentUser != null ? currentUser.getFullName() : "Citizen") + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG_LIGHT);

        // Sidebar
        root.add(createSidebar(), BorderLayout.WEST);

        // Content Area
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);

        rightContainer.add(createTopHeader(), BorderLayout.NORTH);

        contentLayout = new CardLayout();
        mainContent = new JPanel(contentLayout);
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        mainContent.add(createDashboardOverviewPanel(), "OVERVIEW");
        mainContent.add(createSubmitComplaintPanel(), "SUBMIT");
        mainContent.add(createMyComplaintsPanel(), "MY_COMPLAINTS");
        mainContent.add(createTrackComplaintPanel(), "TRACK");
        mainContent.add(createProfilePanel(), "PROFILE");

        rightContainer.add(mainContent, BorderLayout.CENTER);
        root.add(rightContainer, BorderLayout.CENTER);

        setContentPane(root);
        selectNav(0);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, 700));
        sidebar.setBackground(UIUtils.NAVY_DARK);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        // Brand
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        brand.setOpaque(false);
        JLabel logo = new JLabel("♻️ SMART CITY");
        logo.setFont(UIUtils.FONT_HEADER_MEDIUM);
        logo.setForeground(UIUtils.COLOR_PRIMARY);
        brand.add(logo);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(28));

        // Navigation buttons
        addNavButton(sidebar, "📊", "Overview", "OVERVIEW", 0);
        addNavButton(sidebar, "➕", "Submit Complaint", "SUBMIT", 1);
        addNavButton(sidebar, "📋", "My Complaints", "MY_COMPLAINTS", 2);
        addNavButton(sidebar, "🔍", "Track Complaint", "TRACK", 3);
        addNavButton(sidebar, "👤", "Citizen Profile", "PROFILE", 4);

        sidebar.add(Box.createVerticalGlue());

        // Logout
        SidebarButton logoutBtn = new SidebarButton("🚪", "Sign Out");
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int ans = JOptionPane.showConfirmDialog(CitizenDashboard.this,
                    "Are you sure you want to sign out?", "Confirm Sign Out",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (ans == JOptionPane.YES_OPTION) {
                    SessionManager.logout();
                    dispose();
                    new LoginFrame().setVisible(true);
                }
            }
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private void addNavButton(JPanel sidebar, String icon, String title, String cardKey, int index) {
        SidebarButton btn = new SidebarButton(icon, title);
        sidebarButtons.add(btn);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectNav(index);
                contentLayout.show(mainContent, cardKey);
                if ("MY_COMPLAINTS".equals(cardKey)) {
                    refreshMyComplaints();
                }
            }
        });
        sidebar.add(btn);
        sidebar.add(Box.createVerticalStrut(4));
    }

    private void selectNav(int index) {
        for (int i = 0; i < sidebarButtons.size(); i++) {
            sidebarButtons.get(i).setActive(i == index);
        }
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(1000, 60));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_LIGHT),
            BorderFactory.createEmptyBorder(0, 24, 0, 24)
        ));

        JLabel title = new JLabel("Municipal Citizen Services Portal");
        title.setFont(UIUtils.FONT_HEADER_SMALL);
        title.setForeground(UIUtils.TEXT_MAIN);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        userPanel.setOpaque(false);

        String name = currentUser != null ? currentUser.getFullName() : "Citizen User";
        JLabel nameLbl = new JLabel("👤 " + name);
        nameLbl.setFont(UIUtils.FONT_BODY_BOLD);
        nameLbl.setForeground(UIUtils.TEXT_MAIN);

        JLabel badge = new JLabel("CITIZEN");
        badge.setFont(UIUtils.FONT_CAPTION_BOLD);
        badge.setForeground(UIUtils.COLOR_PRIMARY);
        badge.setBackground(UIUtils.COLOR_PRIMARY_LIGHT);
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        userPanel.add(nameLbl);
        userPanel.add(badge);

        header.add(title, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);
        return header;
    }

    // -------------------------------------------------------------
    // 1. Overview Panel
    // -------------------------------------------------------------
    private JPanel createDashboardOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Welcome Card
        RoundedPanel welcomeCard = new RoundedPanel(12, UIUtils.NAVY_DARK, UIUtils.NAVY_LIGHT, 1, false);
        welcomeCard.setLayout(new BorderLayout());
        welcomeCard.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel leftW = new JPanel();
        leftW.setOpaque(false);
        leftW.setLayout(new BoxLayout(leftW, BoxLayout.Y_AXIS));

        String welcomeName = currentUser != null ? currentUser.getFullName() : "Citizen";
        JLabel w1 = new JLabel("Welcome, " + welcomeName + "!");
        w1.setFont(UIUtils.FONT_HEADER_LARGE);
        w1.setForeground(Color.WHITE);

        JLabel w2 = new JLabel("<html>Report civic sanitation issues in your ward with real-time AI priority scoring.<br>" +
                               "Our municipal collection crews are automatically dispatched area-wise.</html>");
        w2.setFont(UIUtils.FONT_BODY);
        w2.setForeground(new Color(203, 213, 225));

        leftW.add(w1);
        leftW.add(Box.createVerticalStrut(8));
        leftW.add(w2);

        ModernButton reportFastBtn = new ModernButton("➕ Report a Waste Issue Now", ButtonStyle.PRIMARY, 10);
        reportFastBtn.setPreferredSize(new Dimension(240, 44));
        reportFastBtn.addActionListener(e -> {
            selectNav(1);
            contentLayout.show(mainContent, "SUBMIT");
        });

        welcomeCard.add(leftW, BorderLayout.CENTER);
        welcomeCard.add(reportFastBtn, BorderLayout.EAST);

        panel.add(welcomeCard, BorderLayout.NORTH);

        // Center Quick Action Cards
        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 16));
        grid.setOpaque(false);

        grid.add(createActionCard("➕ Submit Complaint", "Quickly report garbage overflow, dead animals, or illegal dumping.", "Report Now", () -> {
            selectNav(1);
            contentLayout.show(mainContent, "SUBMIT");
        }));

        grid.add(createActionCard("📋 View My Complaints", "Check status, assigned crews, and resolution times for your complaints.", "Open List", () -> {
            selectNav(2);
            contentLayout.show(mainContent, "MY_COMPLAINTS");
            refreshMyComplaints();
        }));

        grid.add(createActionCard("🔍 Track Complaint", "Enter any complaint ticket ID to view real-time lifecycle history.", "Track Ticket", () -> {
            selectNav(3);
            contentLayout.show(mainContent, "TRACK");
        }));

        panel.add(grid, BorderLayout.CENTER);

        return panel;
    }

    private RoundedPanel createActionCard(String title, String desc, String btnText, Runnable action) {
        RoundedPanel card = new RoundedPanel(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel c = new JPanel();
        c.setOpaque(false);
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(UIUtils.FONT_HEADER_SMALL);
        t.setForeground(UIUtils.TEXT_MAIN);

        JLabel d = new JLabel("<html><body style='width: 180px;'>" + desc + "</body></html>");
        d.setFont(UIUtils.FONT_BODY);
        d.setForeground(UIUtils.TEXT_MUTED);

        c.add(t);
        c.add(Box.createVerticalStrut(10));
        c.add(d);

        ModernButton btn = new ModernButton(btnText, ButtonStyle.SECONDARY, 8);
        btn.addActionListener(e -> action.run());

        card.add(c, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }

    // -------------------------------------------------------------
    // 2. Submit Complaint Form (with Live AI Priority Preview)
    // -------------------------------------------------------------
    private JPanel createSubmitComplaintPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        RoundedPanel formCard = new RoundedPanel(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
        formCard.setLayout(new BorderLayout(24, 0));
        formCard.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Form Fields (Left Column)
        JPanel leftForm = new JPanel(new GridBagLayout());
        leftForm.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 4, 0);

        JLabel formTitle = new JLabel("Register Municipal Waste Complaint");
        formTitle.setFont(UIUtils.FONT_HEADER_MEDIUM);
        formTitle.setForeground(UIUtils.TEXT_MAIN);
        leftForm.add(formTitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        JLabel formSub = new JLabel("Provide accurate ward location and severity details for rapid routing");
        formSub.setFont(UIUtils.FONT_BODY);
        formSub.setForeground(UIUtils.TEXT_MUTED);
        leftForm.add(formSub, gbc);

        // Category
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        leftForm.add(createFieldLabel("Waste Complaint Category *"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 12, 0);
        categoryCombo = new JComboBox<>(ComplaintCategory.values());
        categoryCombo.setFont(UIUtils.FONT_BODY);
        categoryCombo.addActionListener(e -> updateLivePriorityPreview());
        leftForm.add(categoryCombo, gbc);

        // Area & Ward
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        leftForm.add(createFieldLabel("Area & Ward *"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 12, 0);

        JPanel rowAreaWard = new JPanel(new GridLayout(1, 2, 10, 0));
        rowAreaWard.setOpaque(false);

        List<Area> areas = areaDAO.getAllAreas();
        areaCombo = new JComboBox<>(areas.toArray(new Area[0]));
        areaCombo.setFont(UIUtils.FONT_BODY);

        wardCombo = new JComboBox<>();
        wardCombo.setFont(UIUtils.FONT_BODY);

        areaCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Area sel = (Area) areaCombo.getSelectedItem();
                populateWardsForArea(sel);
                updateLivePriorityPreview();
            }
        });

        if (!areas.isEmpty()) {
            populateWardsForArea(areas.get(0));
        }

        wardCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateLivePriorityPreview();
            }
        });

        rowAreaWard.add(areaCombo);
        rowAreaWard.add(wardCombo);
        leftForm.add(rowAreaWard, gbc);

        // Locality / Colony / Sector
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        leftForm.add(createFieldLabel("Locality / Colony / Sector *"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 12, 0);
        localityField = new JTextField();
        UIUtils.styleTextField(localityField);
        leftForm.add(localityField, gbc);

        // Street Address
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        leftForm.add(createFieldLabel("Specific Landmark / Street Address *"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 12, 0);
        addressField = new JTextField();
        UIUtils.styleTextField(addressField);
        leftForm.add(addressField, gbc);

        // Description
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        leftForm.add(createFieldLabel("Description & Observed Hazards (min 10 chars) *"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 12, 0);
        descArea = new JTextArea(3, 20);
        descArea.setFont(UIUtils.FONT_BODY);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(UIUtils.createFieldBorder());
        leftForm.add(new JScrollPane(descArea), gbc);

        // Checkbox factors for AI Priority Engine
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        leftForm.add(createFieldLabel("Critical Condition Flags"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 12, 0);
        JPanel checkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        checkRow.setOpaque(false);

        healthRiskCheck = new JCheckBox("🚨 Public Health Risk (+30)");
        healthRiskCheck.setFont(UIUtils.FONT_BODY);
        healthRiskCheck.setOpaque(false);
        healthRiskCheck.addActionListener(e -> updateLivePriorityPreview());

        overflowCheck = new JCheckBox("🗑️ Garbage Overflow (+25)");
        overflowCheck.setFont(UIUtils.FONT_BODY);
        overflowCheck.setOpaque(false);
        overflowCheck.addActionListener(e -> updateLivePriorityPreview());

        sensitiveCheck = new JCheckBox("🏛️ Hospital / School / Public Landmark (+15)");
        sensitiveCheck.setFont(UIUtils.FONT_BODY);
        sensitiveCheck.setOpaque(false);
        sensitiveCheck.addActionListener(e -> updateLivePriorityPreview());

        checkRow.add(healthRiskCheck);
        checkRow.add(overflowCheck);
        checkRow.add(sensitiveCheck);
        leftForm.add(checkRow, gbc);

        // Optional Image Attachment
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        leftForm.add(createFieldLabel("Optional Proof Photo Path"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);

        JPanel imageRow = new JPanel(new BorderLayout(8, 0));
        imageRow.setOpaque(false);
        imagePathField = new JTextField();
        UIUtils.styleTextField(imagePathField);
        ModernButton browseBtn = new ModernButton("📁 Browse...", ButtonStyle.SECONDARY, 8);
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(CitizenDashboard.this);
            if (res == JFileChooser.APPROVE_OPTION) {
                imagePathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        imageRow.add(imagePathField, BorderLayout.CENTER);
        imageRow.add(browseBtn, BorderLayout.EAST);
        leftForm.add(imageRow, gbc);

        // Submit Button
        gbc.gridy++;
        ModernButton submitBtn = new ModernButton("🚀 Submit Complaint & Dispatch Crew", ButtonStyle.PRIMARY, 10);
        submitBtn.setPreferredSize(new Dimension(300, 44));
        submitBtn.addActionListener(e -> submitComplaintForm());
        leftForm.add(submitBtn, gbc);

        // Right Column: Live AI Priority Engine Card
        JPanel rightPreview = createLivePriorityPreviewCard();

        formCard.add(new JScrollPane(leftForm), BorderLayout.CENTER);
        formCard.add(rightPreview, BorderLayout.EAST);

        panel.add(formCard, BorderLayout.CENTER);

        updateLivePriorityPreview();
        return panel;
    }

    private JPanel createLivePriorityPreviewCard() {
        RoundedPanel card = new RoundedPanel(12, UIUtils.NAVY_DARK, UIUtils.COLOR_PRIMARY, 2, false);
        card.setPreferredSize(new Dimension(340, 500));
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("🤖 AI PRIORITY ENGINE");
        title.setFont(UIUtils.FONT_CAPTION_BOLD);
        title.setForeground(UIUtils.COLOR_PRIMARY);

        JLabel sub = new JLabel("<html>Live intelligent scoring based on smart city risk factors</html>");
        sub.setFont(UIUtils.FONT_CAPTION);
        sub.setForeground(new Color(148, 163, 184));

        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(sub);
        content.add(Box.createVerticalStrut(24));

        // Score Meter Circle / Display
        previewScoreLabel = new JLabel("50");
        previewScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        previewScoreLabel.setForeground(Color.WHITE);
        previewScoreLabel.setAlignmentX(0.0f);

        previewLevelLabel = new JLabel("LEVEL: MEDIUM");
        previewLevelLabel.setFont(UIUtils.FONT_HEADER_SMALL);
        previewLevelLabel.setForeground(UIUtils.PRIORITY_MEDIUM);
        previewLevelLabel.setAlignmentX(0.0f);

        content.add(new JLabel("PREDICTED PRIORITY SCORE:"));
        content.getComponent(content.getComponentCount() - 1).setForeground(new Color(203, 213, 225));
        content.add(Box.createVerticalStrut(4));
        content.add(previewScoreLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(previewLevelLabel);
        content.add(Box.createVerticalStrut(20));

        JLabel reasonHead = new JLabel("SCORING BREAKDOWN:");
        reasonHead.setFont(UIUtils.FONT_CAPTION_BOLD);
        reasonHead.setForeground(UIUtils.COLOR_ACCENT_CYAN);
        content.add(reasonHead);
        content.add(Box.createVerticalStrut(6));

        previewReasonLabel = new JLabel("<html><body style='width: 260px; color: #E2E8F0; font-size: 11px;'>Calculating...</body></html>");
        content.add(previewReasonLabel);

        content.add(Box.createVerticalGlue());

        JLabel note = new JLabel("<html><i style='color: #94A3B8; font-size: 10px;'>Rule-based intelligent dispatch automatically routes tickets to the least occupied crew in your ward.</i></html>");
        content.add(note);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void updateLivePriorityPreview() {
        if (categoryCombo == null || previewScoreLabel == null) return;

        Complaint dummy = new Complaint();
        ComplaintCategory cat = (ComplaintCategory) categoryCombo.getSelectedItem();
        dummy.setCategory(cat != null ? cat.name() : ComplaintCategory.OTHER.name());

        Area a = (Area) areaCombo.getSelectedItem();
        dummy.setAreaId(a != null ? a.getId() : 1);
        dummy.setSensitiveArea((a != null && a.isSensitive()) || sensitiveCheck.isSelected());

        Ward w = (Ward) wardCombo.getSelectedItem();
        dummy.setWardId(w != null ? w.getId() : 1);

        dummy.setHealthRisk(healthRiskCheck.isSelected());
        dummy.setGarbageOverflow(overflowCheck.isSelected());

        PriorityEngine.PriorityResult res = complaintService.getPriorityEngine().calculatePriority(dummy);

        previewScoreLabel.setText(String.valueOf(res.getScore()));
        previewLevelLabel.setText("LEVEL: " + res.getLevel().name());
        previewLevelLabel.setForeground(UIUtils.getPriorityColor(res.getLevel()));
        previewReasonLabel.setText("<html><body style='width: 260px; color: #E2E8F0; font-size: 11px;'>" +
            res.getReason().replace(" • ", "<br>• ") + "</body></html>");
    }

    private void populateWardsForArea(Area area) {
        if (area == null) return;
        List<Ward> wards = areaDAO.getWardsByAreaId(area.getId());
        wardCombo.removeAllItems();
        for (Ward w : wards) {
            wardCombo.addItem(w);
        }
    }

    private void submitComplaintForm() {
        String locality = localityField.getText().trim();
        String address = addressField.getText().trim();
        String desc = descArea.getText().trim();

        if (!ValidationUtils.isNotBlank(locality)) {
            JOptionPane.showMessageDialog(this, "Please enter the Locality / Colony / Sector (e.g. Sector 3, Shipra Path).", "Missing Locality", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtils.isNotBlank(address)) {
            JOptionPane.showMessageDialog(this, "Please enter the specific landmark or street address.", "Missing Address", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ValidationUtils.hasMinLength(desc, 10)) {
            JOptionPane.showMessageDialog(this, "Please provide a detailed description (at least 10 characters).", "Description Too Short", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Area area = (Area) areaCombo.getSelectedItem();
        Ward ward = (Ward) wardCombo.getSelectedItem();
        ComplaintCategory cat = (ComplaintCategory) categoryCombo.getSelectedItem();

        if (area == null || ward == null || cat == null) {
            JOptionPane.showMessageDialog(this, "Please select valid Area and Ward.", "Selection Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Complaint c = new Complaint();
        c.setCitizenUserId(currentUser != null ? currentUser.getId() : null);
        c.setCitizenName(currentUser != null ? currentUser.getFullName() : "Anonymous Citizen");
        c.setPhone(currentUser != null ? currentUser.getPhone() : "9829000000");
        c.setEmail(currentUser != null ? currentUser.getEmail() : null);
        c.setAreaId(area.getId());
        c.setAreaName(area.getName());
        c.setWardId(ward.getId());
        c.setWardNumber(ward.getWardNumber());
        c.setWardName(ward.getWardName());
        c.setLocality(locality);
        c.setAddress(address);
        c.setCategory(cat.getDisplayName());
        c.setDescription(desc);
        c.setImagePath(imagePathField.getText().trim().isEmpty() ? null : imagePathField.getText().trim());
        c.setHealthRisk(healthRiskCheck.isSelected());
        c.setGarbageOverflow(overflowCheck.isSelected());
        c.setSensitiveArea(area.isSensitive() || sensitiveCheck.isSelected());

        SwingWorker<Complaint, Void> worker = new SwingWorker<Complaint, Void>() {
            @Override
            protected Complaint doInBackground() {
                return complaintService.submitComplaint(c, true); // Auto-route enabled!
            }

            @Override
            protected void done() {
                try {
                    Complaint result = get();
                    if (result != null) {
                        showSubmissionSuccessModal(result);
                        // Reset form
                        localityField.setText("");
                        addressField.setText("");
                        descArea.setText("");
                        imagePathField.setText("");
                        healthRiskCheck.setSelected(false);
                        overflowCheck.setSelected(false);
                        sensitiveCheck.setSelected(false);
                        updateLivePriorityPreview();
                    } else {
                        JOptionPane.showMessageDialog(CitizenDashboard.this,
                            "Failed to submit complaint. Please check database connection.",
                            "Submission Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CitizenDashboard.this,
                        "Submission error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showSubmissionSuccessModal(Complaint c) {
        JDialog dialog = new JDialog(this, "Complaint Successfully Registered", true);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("✅ COMPLAINT REGISTERED", SwingConstants.CENTER);
        icon.setFont(UIUtils.FONT_HEADER_MEDIUM);
        icon.setForeground(UIUtils.COLOR_PRIMARY);

        content.add(icon);
        content.add(Box.createVerticalStrut(16));

        RoundedPanel summary = new RoundedPanel(10, UIUtils.BG_LIGHT, UIUtils.BORDER_LIGHT, 1, false);
        summary.setLayout(new GridLayout(7, 2, 8, 10));
        summary.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        addSummaryRow(summary, "Complaint ID:", c.getComplaintCode());
        addSummaryRow(summary, "Priority Level:", c.getPriority().name() + " (Score: " + c.getPriorityScore() + ")");
        addSummaryRow(summary, "Current Status:", c.getStatus().getDisplayName());
        addSummaryRow(summary, "Locality / Colony:", c.getLocality() != null ? c.getLocality() : "N/A");
        addSummaryRow(summary, "Assigned Crew:", c.getAssignedCrewName() != null ? c.getAssignedCrewName() :
            (c.getStatus() == ComplaintStatus.PENDING_ASSIGNMENT ? "Pending Zone Dispatcher" : "Auto-routing pending"));
        addSummaryRow(summary, "Expected Resolution:", c.getExpectedResolutionTime() != null ? sdf.format(c.getExpectedResolutionTime()) : "Within 24 Hours");
        addSummaryRow(summary, "Area / Ward:", c.getAreaName() + " (" + c.getWardNumber() + ")");

        content.add(summary);
        content.add(Box.createVerticalStrut(14));

        JLabel reason = new JLabel("<html><b>Priority Reason:</b><br><span style='color: #475569;'>" + c.getPriorityReason() + "</span></html>");
        reason.setFont(UIUtils.FONT_CAPTION);
        content.add(reason);

        p.add(content, BorderLayout.CENTER);

        ModernButton okBtn = new ModernButton("Done & View My Complaints", ButtonStyle.PRIMARY, 8);
        okBtn.addActionListener(e -> {
            dialog.dispose();
            selectNav(2);
            contentLayout.show(mainContent, "MY_COMPLAINTS");
            refreshMyComplaints();
        });
        p.add(okBtn, BorderLayout.SOUTH);

        dialog.setContentPane(p);
        dialog.setVisible(true);
    }

    private void addSummaryRow(JPanel p, String label, String val) {
        JLabel l = new JLabel(label);
        l.setFont(UIUtils.FONT_CAPTION_BOLD);
        l.setForeground(UIUtils.TEXT_MUTED);

        JLabel v = new JLabel(val);
        v.setFont(UIUtils.FONT_BODY_BOLD);
        v.setForeground(UIUtils.TEXT_MAIN);

        p.add(l);
        p.add(v);
    }

    // -------------------------------------------------------------
    // 3. My Complaints Panel
    // -------------------------------------------------------------
    private JPanel createMyComplaintsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // Header with Refresh and Leave Feedback button
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("My Submitted Complaints");
        title.setFont(UIUtils.FONT_HEADER_MEDIUM);
        title.setForeground(UIUtils.TEXT_MAIN);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        ModernButton refreshBtn = new ModernButton("🔄 Refresh", ButtonStyle.SECONDARY, 8);
        refreshBtn.addActionListener(e -> refreshMyComplaints());

        ModernButton feedbackBtn = new ModernButton("⭐ Rate Resolved Complaint", ButtonStyle.PRIMARY, 8);
        feedbackBtn.addActionListener(e -> openFeedbackModalForSelected());

        btnRow.add(refreshBtn);
        btnRow.add(feedbackBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnRow, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Ticket ID", "Category", "Area & Ward", "Locality", "Priority", "Score", "Status", "Assigned Crew", "Date Reported"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        complaintsTable = new StyledTable(tableModel);
        panel.add(new JScrollPane(complaintsTable), BorderLayout.CENTER);

        return panel;
    }

    private void refreshMyComplaints() {
        if (currentUser == null) return;
        SwingWorker<List<Complaint>, Void> worker = new SwingWorker<List<Complaint>, Void>() {
            @Override
            protected List<Complaint> doInBackground() {
                return complaintDAO.getByCitizenId(currentUser.getId());
            }

            @Override
            protected void done() {
                try {
                    List<Complaint> list = get();
                    tableModel.setRowCount(0);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
                    for (Complaint c : list) {
                        tableModel.addRow(new Object[]{
                            c.getComplaintCode(),
                            c.getCategory(),
                            c.getAreaName() + " (" + c.getWardNumber() + ")",
                            c.getLocality() != null ? c.getLocality() : "-",
                            c.getPriority(),
                            c.getPriorityScore(),
                            c.getStatus(),
                            c.getAssignedCrewName() != null ? c.getAssignedCrewName() :
                                (c.getStatus() == ComplaintStatus.PENDING_ASSIGNMENT ? "Pending Dispatch" : "Unassigned"),
                            sdf.format(c.getCreatedAt())
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void openFeedbackModalForSelected() {
        int row = complaintsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a resolved complaint from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String code = (String) tableModel.getValueAt(row, 0);
        Complaint c = complaintDAO.findByCode(code);
        if (c == null) return;

        if (c.getStatus() != ComplaintStatus.RESOLVED) {
            JOptionPane.showMessageDialog(this, "Feedback can only be provided for RESOLVED complaints.", "Not Resolved Yet", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Feedback existing = feedbackDAO.getByComplaintId(c.getId());

        JDialog dlg = new JDialog(this, "Rate Service Quality: " + c.getComplaintCode(), true);
        dlg.setSize(440, 380);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Municipal Service Feedback");
        title.setFont(UIUtils.FONT_HEADER_SMALL);
        title.setForeground(UIUtils.TEXT_MAIN);

        JLabel sub = new JLabel("How satisfied were you with the resolution by " + c.getAssignedCrewName() + "?");
        sub.setFont(UIUtils.FONT_BODY);
        sub.setForeground(UIUtils.TEXT_MUTED);

        form.add(title);
        form.add(Box.createVerticalStrut(6));
        form.add(sub);
        form.add(Box.createVerticalStrut(20));

        JLabel starLbl = new JLabel("Rating: ⭐⭐⭐⭐⭐ (5 / 5)");
        starLbl.setFont(UIUtils.FONT_BODY_BOLD);
        starLbl.setForeground(UIUtils.COLOR_PRIMARY);

        JSlider ratingSlider = new JSlider(1, 5, existing != null ? existing.getRating() : 5);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);
        ratingSlider.setOpaque(false);
        ratingSlider.addChangeListener(e -> {
            int val = ratingSlider.getValue();
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < val; i++) stars.append("⭐");
            starLbl.setText("Rating: " + stars + " (" + val + " / 5)");
        });

        form.add(starLbl);
        form.add(Box.createVerticalStrut(8));
        form.add(ratingSlider);
        form.add(Box.createVerticalStrut(14));

        JLabel commentLbl = new JLabel("Feedback Comments:");
        commentLbl.setFont(UIUtils.FONT_CAPTION_BOLD);
        form.add(commentLbl);
        form.add(Box.createVerticalStrut(6));

        JTextArea commentArea = new JTextArea(3, 20);
        commentArea.setFont(UIUtils.FONT_BODY);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setText(existing != null && existing.getComments() != null ? existing.getComments() : "");
        commentArea.setBorder(UIUtils.createFieldBorder());
        form.add(new JScrollPane(commentArea));

        p.add(form, BorderLayout.CENTER);

        ModernButton submitFbBtn = new ModernButton("Submit Citizen Feedback", ButtonStyle.PRIMARY, 8);
        submitFbBtn.addActionListener(e -> {
            boolean ok = complaintService.submitFeedback(c.getId(), currentUser != null ? currentUser.getId() : null,
                ratingSlider.getValue(), commentArea.getText().trim());
            if (ok) {
                JOptionPane.showMessageDialog(dlg, "Thank you! Your feedback has been submitted.", "Feedback Saved", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to record feedback.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        p.add(submitFbBtn, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    // -------------------------------------------------------------
    // 4. Track Complaint Panel (Visual Timeline)
    // -------------------------------------------------------------
    private JPanel createTrackComplaintPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // Search bar on top
        RoundedPanel searchCard = new RoundedPanel(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 16));

        JLabel lbl = new JLabel("Enter Complaint Ticket ID:");
        lbl.setFont(UIUtils.FONT_BODY_BOLD);
        lbl.setForeground(UIUtils.TEXT_MAIN);

        trackCodeField = new JTextField(16);
        trackCodeField.setFont(UIUtils.FONT_BODY);
        trackCodeField.setText("CMP-2026-1001");
        UIUtils.styleTextField(trackCodeField);

        ModernButton trackBtn = new ModernButton("🔍 Track Ticket", ButtonStyle.PRIMARY, 8);
        trackBtn.addActionListener(e -> performTrackLookup());

        searchCard.add(lbl);
        searchCard.add(trackCodeField);
        searchCard.add(trackBtn);

        panel.add(searchCard, BorderLayout.NORTH);

        // Result Container
        trackResultContainer = new JPanel(new BorderLayout());
        trackResultContainer.setOpaque(false);
        panel.add(new JScrollPane(trackResultContainer), BorderLayout.CENTER);

        return panel;
    }

    private void performTrackLookup() {
        String code = trackCodeField.getText().trim();
        if (code.isEmpty()) return;

        SwingWorker<Complaint, Void> worker = new SwingWorker<Complaint, Void>() {
            @Override
            protected Complaint doInBackground() {
                return complaintDAO.findByCode(code);
            }

            @Override
            protected void done() {
                try {
                    Complaint c = get();
                    trackResultContainer.removeAll();
                    if (c != null) {
                        trackResultContainer.add(buildTrackDetailsView(c), BorderLayout.NORTH);
                    } else {
                        JPanel notFound = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
                        notFound.setOpaque(false);
                        JLabel nf = new JLabel("❌ No complaint ticket found matching ID: " + code);
                        nf.setFont(UIUtils.FONT_HEADER_SMALL);
                        nf.setForeground(UIUtils.PRIORITY_CRITICAL);
                        notFound.add(nf);
                        trackResultContainer.add(notFound, BorderLayout.NORTH);
                    }
                    trackResultContainer.revalidate();
                    trackResultContainer.repaint();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CitizenDashboard.this, "Search error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel buildTrackDetailsView(Complaint c) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        // Top Status Header Card
        RoundedPanel head = new RoundedPanel(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
        head.setLayout(new BorderLayout(16, 0));
        head.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel leftH = new JPanel();
        leftH.setOpaque(false);
        leftH.setLayout(new BoxLayout(leftH, BoxLayout.Y_AXIS));

        JLabel codeLbl = new JLabel(c.getComplaintCode() + " - " + c.getCategory());
        codeLbl.setFont(UIUtils.FONT_HEADER_MEDIUM);
        codeLbl.setForeground(UIUtils.TEXT_MAIN);

        JLabel locLbl = new JLabel("📍 " + c.getAddress() + (c.getLocality() != null ? ", " + c.getLocality() : "") + ", " + c.getAreaName() + " (" + c.getWardNumber() + ")");
        locLbl.setFont(UIUtils.FONT_BODY);
        locLbl.setForeground(UIUtils.TEXT_MUTED);

        leftH.add(codeLbl);
        leftH.add(Box.createVerticalStrut(4));
        leftH.add(locLbl);

        JPanel rightH = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightH.setOpaque(false);
        rightH.add(new PriorityBadge(c.getPriority()));
        rightH.add(new StatusBadge(c.getStatus()));

        head.add(leftH, BorderLayout.CENTER);
        head.add(rightH, BorderLayout.EAST);
        container.add(head);
        container.add(Box.createVerticalStrut(16));

        // Visual Progress Stepper (NEW -> ASSIGNED -> IN_PROGRESS -> RESOLVED)
        RoundedPanel stepper = createProgressTimeline(c.getStatus());
        container.add(stepper);
        container.add(Box.createVerticalStrut(16));

        // Details Grid (Assigned Crew, Expected Time, Reason, Notes)
        RoundedPanel details = new RoundedPanel(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
        details.setLayout(new GridLayout(4, 2, 16, 12));
        details.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        addSummaryRow(details, "Assigned Collection Crew:", c.getAssignedCrewName() != null ? c.getAssignedCrewName() + " (" + c.getCrewVehicleNumber() + ")" :
            (c.getStatus() == ComplaintStatus.PENDING_ASSIGNMENT ? "Pending Area Dispatcher Assignment" : "Pending Area Dispatch"));
        addSummaryRow(details, "Crew Contact:", c.getCrewPhone() != null ? c.getCrewPhone() : "N/A");
        addSummaryRow(details, "Reported At:", sdf.format(c.getCreatedAt()));
        addSummaryRow(details, "Target Resolution Deadline:", c.getExpectedResolutionTime() != null ? sdf.format(c.getExpectedResolutionTime()) : "Within 24 Hours");
        addSummaryRow(details, "AI Priority Score:", c.getPriorityScore() + " / 100 (" + c.getPriority() + ")");
        addSummaryRow(details, "Resolved At:", c.getResolvedAt() != null ? sdf.format(c.getResolvedAt()) : "Pending");
        addSummaryRow(details, "Priority Scoring Reason:", c.getPriorityReason());
        addSummaryRow(details, "Crew Resolution Notes:", c.getResolutionNotes() != null ? c.getResolutionNotes() : "No notes logged yet");

        container.add(details);
        container.add(Box.createVerticalStrut(16));

        // Audit Trail History
        List<StatusHistory> histories = statusHistoryDAO.getByComplaintId(c.getId());
        if (!histories.isEmpty()) {
            RoundedPanel historyCard = new RoundedPanel(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
            historyCard.setLayout(new BoxLayout(historyCard, BoxLayout.Y_AXIS));
            historyCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

            JLabel hTitle = new JLabel("Audit Log & Lifecycle History");
            hTitle.setFont(UIUtils.FONT_BODY_BOLD);
            hTitle.setForeground(UIUtils.TEXT_MAIN);
            historyCard.add(hTitle);
            historyCard.add(Box.createVerticalStrut(10));

            for (StatusHistory sh : histories) {
                JLabel row = new JLabel("• " + sdf.format(sh.getChangedAt()) + " — [" + sh.getNewStatus() + "] " +
                    (sh.getComments() != null ? sh.getComments() : "") +
                    (sh.getChangedByName() != null ? " (by " + sh.getChangedByName() + ")" : ""));
                row.setFont(UIUtils.FONT_CAPTION);
                row.setForeground(UIUtils.TEXT_MUTED);
                historyCard.add(row);
                historyCard.add(Box.createVerticalStrut(4));
            }
            container.add(historyCard);
        }

        return container;
    }

    private RoundedPanel createProgressTimeline(ComplaintStatus currentStatus) {
        RoundedPanel p = new RoundedPanel(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
        p.setLayout(new GridLayout(1, 4, 12, 0));
        p.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        int step = 1;
        if (currentStatus == ComplaintStatus.ASSIGNED) step = 2;
        else if (currentStatus == ComplaintStatus.IN_PROGRESS) step = 3;
        else if (currentStatus == ComplaintStatus.RESOLVED) step = 4;
        else if (currentStatus == ComplaintStatus.REJECTED) step = -1;

        p.add(createStepNode("1. Submitted", "Ticket Registered", step >= 1));
        p.add(createStepNode("2. Dispatched", "Crew Assigned", step >= 2));
        p.add(createStepNode("3. In Progress", "Crew On Site", step >= 3));
        p.add(createStepNode(step == -1 ? "4. Rejected" : "4. Resolved", step == -1 ? "Disputed" : "Work Completed", step >= 4));

        return p;
    }

    private JPanel createStepNode(String title, String sub, boolean active) {
        JPanel node = new JPanel();
        node.setOpaque(false);
        node.setLayout(new BoxLayout(node, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(UIUtils.FONT_BODY_BOLD);
        t.setForeground(active ? UIUtils.COLOR_PRIMARY : UIUtils.TEXT_MUTED);

        JLabel s = new JLabel(sub);
        s.setFont(UIUtils.FONT_CAPTION);
        s.setForeground(active ? UIUtils.TEXT_MAIN : UIUtils.TEXT_MUTED);

        node.add(t);
        node.add(Box.createVerticalStrut(2));
        node.add(s);
        return node;
    }

    // -------------------------------------------------------------
    // 5. Profile Panel
    // -------------------------------------------------------------
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        RoundedPanel card = new RoundedPanel(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Citizen Profile");
        title.setFont(UIUtils.FONT_HEADER_MEDIUM);
        title.setForeground(UIUtils.TEXT_MAIN);

        content.add(title);
        content.add(Box.createVerticalStrut(24));

        if (currentUser != null) {
            content.add(createProfileRow("Full Name:", currentUser.getFullName()));
            content.add(Box.createVerticalStrut(12));
            content.add(createProfileRow("Username:", currentUser.getUsername()));
            content.add(Box.createVerticalStrut(12));
            content.add(createProfileRow("Registered Phone:", currentUser.getPhone()));
            content.add(Box.createVerticalStrut(12));
            content.add(createProfileRow("Email Address:", currentUser.getEmail() != null ? currentUser.getEmail() : "Not provided"));
            content.add(Box.createVerticalStrut(12));
            content.add(createProfileRow("Portal Role:", currentUser.getRole().getDisplayName()));
        }

        card.add(content, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProfileRow(String label, String val) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(140, 24));
        l.setFont(UIUtils.FONT_BODY_BOLD);
        l.setForeground(UIUtils.TEXT_MUTED);

        JLabel v = new JLabel(val);
        v.setFont(UIUtils.FONT_BODY);
        v.setForeground(UIUtils.TEXT_MAIN);

        p.add(l);
        p.add(v);
        return p;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIUtils.FONT_CAPTION_BOLD);
        lbl.setForeground(UIUtils.TEXT_MAIN);
        return lbl;
    }
}
