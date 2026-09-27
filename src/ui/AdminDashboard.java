package ui;

import dao.AnalyticsDAO;
import dao.AreaDAO;
import dao.ComplaintDAO;
import dao.CrewDAO;
import dao.StatusHistoryDAO;
import dao.UserDAO;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Area;
import model.Complaint;
import model.Crew;
import model.Enums.ComplaintCategory;
import model.Enums.ComplaintStatus;
import model.Enums.CrewAvailability;
import model.Enums.PriorityLevel;
import model.StatusHistory;
import model.User;
import model.Ward;
import service.AnalyticsService;
import service.AssignmentService;
import service.ComplaintService;
import ui.components.CustomChartPanel.DonutChartPanel;
import ui.components.CustomChartPanel.HorizontalBarChartPanel;
import ui.components.CustomChartPanel.TrendLineChartPanel;
import ui.components.ModernButton;
import ui.components.ModernButton.ButtonStyle;
import ui.components.NotificationToast;
import ui.components.NotificationToast.ToastType;
import ui.components.PriorityBadge;
import ui.components.RoundedPanel;
import ui.components.SearchField;
import ui.components.SidebarButton;
import ui.components.StatCard;
import ui.components.StatusBadge;
import ui.components.StyledTable;
import utils.SessionManager;
import utils.UIUtils;
import utils.ValidationUtils;

/**
 * Modern Smart City Municipal Admin & Staff Control Center Dashboard.
 */
public class AdminDashboard extends JFrame {

    private final AnalyticsService analyticsService;
    private final ComplaintService complaintService;
    private final ComplaintDAO complaintDAO;
    private final CrewDAO crewDAO;
    private final AreaDAO areaDAO;
    private final StatusHistoryDAO statusHistoryDAO;
    private final User currentUser;

    private JPanel mainContent;
    private CardLayout contentLayout;
    private List<SidebarButton> sidebarButtons = new ArrayList<>();

    // Stat Cards
    private StatCard cardTotal;
    private StatCard cardPending;
    private StatCard cardInProgress;
    private StatCard cardResolved;
    private StatCard cardCritical;
    private StatCard cardCrews;

    // Charts
    private DonutChartPanel donutCategoryChart;
    private HorizontalBarChartPanel barAreaChart;
    private TrendLineChartPanel trendChart;

    // Complaint Management Components
    private SearchField searchField;
    private JComboBox<String> filterStatusCombo;
    private JComboBox<String> filterPriorityCombo;
    private JComboBox<String> filterAreaCombo;
    private JComboBox<String> filterCategoryCombo;
    private JComboBox<String> sortCombo;
    private StyledTable complaintsTable;
    private DefaultTableModel complaintsModel;

    // Crew Management Components
    private StyledTable crewTable;
    private DefaultTableModel crewModel;

    // Area Intelligence Components
    private StyledTable areaIntelTable;
    private DefaultTableModel areaIntelModel;

    public AdminDashboard() {
        this.analyticsService = new AnalyticsService();
        this.complaintService = new ComplaintService();
        this.complaintDAO = new ComplaintDAO();
        this.crewDAO = new CrewDAO();
        this.areaDAO = new AreaDAO();
        this.statusHistoryDAO = new StatusHistoryDAO();
        this.currentUser = SessionManager.getCurrentUser();

        setTitle("Smart City Control Center - Municipal Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1340, 860);
        setMinimumSize(new Dimension(1150, 720));
        setLocationRelativeTo(null);

        initUI();
        refreshAllData();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG_LIGHT);

        // Sidebar
        root.add(createSidebar(), BorderLayout.WEST);

        // Right Layout (Header + Card Content)
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(createTopHeader(), BorderLayout.NORTH);

        contentLayout = new CardLayout();
        mainContent = new JPanel(contentLayout);
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        mainContent.add(createOverviewPanel(), "OVERVIEW");
        mainContent.add(createComplaintsManagementPanel(), "COMPLAINTS");
        mainContent.add(createCrewManagementPanel(), "CREWS");
        mainContent.add(createAreaIntelligencePanel(), "AREA_INTEL");

        right.add(mainContent, BorderLayout.CENTER);
        root.add(right, BorderLayout.CENTER);

        setContentPane(root);
        selectNav(0);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(250, 800));
        sidebar.setBackground(UIUtils.NAVY_DARK);
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 0, 24, 0));

        // Brand
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        brand.setOpaque(false);
        JLabel logo = new JLabel("♻️ CONTROL CENTER");
        logo.setFont(UIUtils.FONT_HEADER_MEDIUM);
        logo.setForeground(UIUtils.COLOR_PRIMARY);
        brand.add(logo);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(28));

        // Nav buttons
        addNavButton(sidebar, "📊", "Command Overview", "OVERVIEW", 0);
        addNavButton(sidebar, "📋", "Complaint Operations", "COMPLAINTS", 1);
        addNavButton(sidebar, "🚛", "Collection Crews", "CREWS", 2);
        addNavButton(sidebar, "🗺️", "Area Intelligence", "AREA_INTEL", 3);

        sidebar.add(Box.createVerticalGlue());

        // Refresh Data button
        SidebarButton refBtn = new SidebarButton("🔄", "Refresh Live Data");
        refBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshAllData();
                NotificationToast.show(AdminDashboard.this, "Data Refreshed", "Live municipal metrics successfully updated.", ToastType.INFO);
            }
        });
        sidebar.add(refBtn);
        sidebar.add(Box.createVerticalStrut(6));

        // Logout
        SidebarButton logoutBtn = new SidebarButton("🚪", "Sign Out");
        logoutBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int ans = JOptionPane.showConfirmDialog(AdminDashboard.this,
                    "Sign out from Municipal Admin Portal?", "Sign Out", JOptionPane.YES_NO_OPTION);
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
                if ("COMPLAINTS".equals(cardKey)) {
                    refreshComplaintsTable();
                } else if ("CREWS".equals(cardKey)) {
                    refreshCrewsTable();
                } else if ("AREA_INTEL".equals(cardKey)) {
                    refreshAreaIntelligence();
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
        header.setPreferredSize(new Dimension(1100, 64));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_LIGHT),
            BorderFactory.createEmptyBorder(0, 24, 0, 24)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 16));
        left.setOpaque(false);
        JLabel title = new JLabel("Municipal Waste Collection & Complaint System");
        title.setFont(UIUtils.FONT_HEADER_SMALL);
        title.setForeground(UIUtils.TEXT_MAIN);
        left.add(title);

        // Center Live Digital Clock
        JLabel clockLabel = new JLabel();
        clockLabel.setFont(UIUtils.FONT_CAPTION_BOLD);
        clockLabel.setForeground(UIUtils.TEXT_MUTED);
        SimpleDateFormat clockFormat = new SimpleDateFormat("EEE, dd MMM yyyy • hh:mm:ss a");
        clockLabel.setText("🕒 " + clockFormat.format(new java.util.Date()));
        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, e -> clockLabel.setText("🕒 " + clockFormat.format(new java.util.Date())));
        clockTimer.start();

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 18));
        centerPanel.setOpaque(false);
        centerPanel.add(clockLabel);
        header.add(centerPanel, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        right.setOpaque(false);

        // Notification Bell
        ModernButton notifBtn = new ModernButton("🔔 Alerts", ButtonStyle.SECONDARY, 8);
        notifBtn.addActionListener(e -> showSystemAlertsDialog());

        String name = currentUser != null ? currentUser.getFullName() : "Administrator";
        JLabel userLbl = new JLabel("👤 " + name);
        userLbl.setFont(UIUtils.FONT_BODY_BOLD);
        userLbl.setForeground(UIUtils.TEXT_MAIN);

        JLabel roleBadge = new JLabel("MUNICIPAL ADMIN");
        roleBadge.setFont(UIUtils.FONT_CAPTION_BOLD);
        roleBadge.setForeground(UIUtils.COLOR_ACCENT_BLUE);
        roleBadge.setBackground(new Color(239, 246, 255));
        roleBadge.setOpaque(true);
        roleBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        right.add(notifBtn);
        right.add(userLbl);
        right.add(roleBadge);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // -------------------------------------------------------------
    // 1. Overview Dashboard (6 KPI Cards + 3 Analytics Charts)
    // -------------------------------------------------------------
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // 6 KPI Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 6, 12, 0));
        statsRow.setOpaque(false);

        cardTotal = new StatCard("Total Complaints", "0", "All time logged", UIUtils.COLOR_ACCENT_BLUE);
        cardPending = new StatCard("Pending Action", "0", "New & unassigned", UIUtils.PRIORITY_MEDIUM);
        cardInProgress = new StatCard("In Progress", "0", "Crews on field", UIUtils.STATUS_IN_PROGRESS);
        cardResolved = new StatCard("Resolved", "0", "Cleared & verified", UIUtils.COLOR_PRIMARY);
        cardCritical = new StatCard("Critical Risk", "0", "Requires rapid action", UIUtils.PRIORITY_CRITICAL);
        cardCrews = new StatCard("Active Crews", "0", "Field units ready", UIUtils.COLOR_ACCENT_PURPLE);

        statsRow.add(cardTotal);
        statsRow.add(cardPending);
        statsRow.add(cardInProgress);
        statsRow.add(cardResolved);
        statsRow.add(cardCritical);
        statsRow.add(cardCrews);

        panel.add(statsRow, BorderLayout.NORTH);

        // Charts Section (Grid of 3 charts)
        JPanel chartsGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        chartsGrid.setOpaque(false);

        donutCategoryChart = new DonutChartPanel("Complaints by Category", null);
        barAreaChart = new HorizontalBarChartPanel("Complaints by Municipal Area", null);
        trendChart = new TrendLineChartPanel("Complaint Influx (7 Days)", null);

        chartsGrid.add(donutCategoryChart);
        chartsGrid.add(barAreaChart);
        chartsGrid.add(trendChart);

        // Center container with charts and Recent Critical Table
        JPanel centerContainer = new JPanel(new BorderLayout(0, 16));
        centerContainer.setOpaque(false);
        centerContainer.add(chartsGrid, BorderLayout.CENTER);

        panel.add(centerContainer, BorderLayout.CENTER);

        return panel;
    }

    // -------------------------------------------------------------
    // 2. Complaint Operations (Search, Multi-Filter, Assign, Update)
    // -------------------------------------------------------------
    private JPanel createComplaintsManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // Filter Bar
        RoundedPanel filterCard = new RoundedPanel(12, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 12));

        searchField = new SearchField("Search Ticket ID, Citizen, Ward, Landmark...");
        searchField.setPreferredSize(new Dimension(280, 38));
        searchField.getTextField().addActionListener(e -> refreshComplaintsTable());

        // Status Combo
        filterStatusCombo = new JComboBox<>(new String[]{"All Statuses", "NEW", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "REJECTED"});
        filterStatusCombo.setFont(UIUtils.FONT_BODY);

        // Priority Combo
        filterPriorityCombo = new JComboBox<>(new String[]{"All Priorities", "CRITICAL", "HIGH", "MEDIUM", "LOW"});
        filterPriorityCombo.setFont(UIUtils.FONT_BODY);

        // Area Combo
        filterAreaCombo = new JComboBox<>();
        filterAreaCombo.setFont(UIUtils.FONT_BODY);
        filterAreaCombo.addItem("All Areas");
        List<Area> areas = areaDAO.getAllAreas();
        for (Area a : areas) filterAreaCombo.addItem(a.getName());

        // Category Combo
        filterCategoryCombo = new JComboBox<>();
        filterCategoryCombo.setFont(UIUtils.FONT_BODY);
        filterCategoryCombo.addItem("All Categories");
        for (ComplaintCategory cat : ComplaintCategory.values()) filterCategoryCombo.addItem(cat.getDisplayName());

        // Sort Combo
        sortCombo = new JComboBox<>(new String[]{"Sort: Date (Newest)", "Sort: Priority (Highest)", "Sort: Status", "Sort: Area"});
        sortCombo.setFont(UIUtils.FONT_BODY);

        ModernButton applyBtn = new ModernButton("🔍 Apply Filters", ButtonStyle.PRIMARY, 8);
        applyBtn.addActionListener(e -> refreshComplaintsTable());

        ModernButton resetBtn = new ModernButton("✕ Reset", ButtonStyle.SECONDARY, 8);
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            filterStatusCombo.setSelectedIndex(0);
            filterPriorityCombo.setSelectedIndex(0);
            filterAreaCombo.setSelectedIndex(0);
            filterCategoryCombo.setSelectedIndex(0);
            sortCombo.setSelectedIndex(0);
            refreshComplaintsTable();
        });

        filterCard.add(searchField);
        filterCard.add(filterStatusCombo);
        filterCard.add(filterPriorityCombo);
        filterCard.add(filterAreaCombo);
        filterCard.add(filterCategoryCombo);
        filterCard.add(sortCombo);
        filterCard.add(applyBtn);
        filterCard.add(resetBtn);

        panel.add(filterCard, BorderLayout.NORTH);

        // Table
        String[] cols = {"Ticket ID", "Citizen Name", "Phone", "Area & Ward", "Category", "Priority", "Score", "Status", "Assigned Crew", "Reported"};
        complaintsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        complaintsTable = new StyledTable(complaintsModel);
        panel.add(new JScrollPane(complaintsTable), BorderLayout.CENTER);

        // Bottom Action Bar: View Details, Assign Crew, Update Status
        RoundedPanel actionCard = new RoundedPanel(10, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
        actionCard.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 10));

        ModernButton viewBtn = new ModernButton("👁️ View Full Details", ButtonStyle.SECONDARY, 8);
        viewBtn.addActionListener(e -> openComplaintDetailsModal());

        ModernButton assignBtn = new ModernButton("🚛 Assign / Reassign Crew", ButtonStyle.PRIMARY, 8);
        assignBtn.addActionListener(e -> openAssignCrewModal());

        ModernButton statusBtn = new ModernButton("✏️ Update Status", ButtonStyle.DARK, 8);
        statusBtn.addActionListener(e -> openUpdateStatusModal());

        actionCard.add(viewBtn);
        actionCard.add(assignBtn);
        actionCard.add(statusBtn);

        panel.add(actionCard, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshComplaintsTable() {
        String query = searchField != null ? searchField.getText() : "";
        ComplaintStatus status = null;
        if (filterStatusCombo != null && filterStatusCombo.getSelectedIndex() > 0) {
            status = ComplaintStatus.valueOf((String) filterStatusCombo.getSelectedItem());
        }

        PriorityLevel priority = null;
        if (filterPriorityCombo != null && filterPriorityCombo.getSelectedIndex() > 0) {
            priority = PriorityLevel.valueOf((String) filterPriorityCombo.getSelectedItem());
        }

        Integer areaId = null;
        if (filterAreaCombo != null && filterAreaCombo.getSelectedIndex() > 0) {
            String areaName = (String) filterAreaCombo.getSelectedItem();
            List<Area> areas = areaDAO.getAllAreas();
            for (Area a : areas) {
                if (a.getName().equalsIgnoreCase(areaName)) {
                    areaId = a.getId();
                    break;
                }
            }
        }

        String cat = null;
        if (filterCategoryCombo != null && filterCategoryCombo.getSelectedIndex() > 0) {
            cat = (String) filterCategoryCombo.getSelectedItem();
        }

        String sortBy = "date";
        boolean sortAsc = false;
        if (sortCombo != null) {
            int idx = sortCombo.getSelectedIndex();
            if (idx == 1) sortBy = "priority";
            else if (idx == 2) sortBy = "status";
            else if (idx == 3) { sortBy = "area"; sortAsc = true; }
        }

        final Integer finalAreaId = areaId;
        final String finalCat = cat;
        final String finalSortBy = sortBy;
        final boolean finalSortAsc = sortAsc;
        final ComplaintStatus finalStatus = status;
        final PriorityLevel finalPriority = priority;

        SwingWorker<List<Complaint>, Void> worker = new SwingWorker<List<Complaint>, Void>() {
            @Override
            protected List<Complaint> doInBackground() {
                return complaintDAO.searchAndFilter(query, finalStatus, finalPriority, finalAreaId, null, finalCat, finalSortBy, finalSortAsc);
            }

            @Override
            protected void done() {
                try {
                    List<Complaint> list = get();
                    complaintsModel.setRowCount(0);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a");
                    for (Complaint c : list) {
                        complaintsModel.addRow(new Object[]{
                            c.getComplaintCode(),
                            c.getCitizenName(),
                            c.getPhone(),
                            c.getAreaName() + " (" + c.getWardNumber() + ")",
                            c.getCategory(),
                            c.getPriority(),
                            c.getPriorityScore(),
                            c.getStatus(),
                            c.getAssignedCrewName() != null ? c.getAssignedCrewName() : "Unassigned",
                            sdf.format(c.getCreatedAt())
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void openComplaintDetailsModal() {
        int row = complaintsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a complaint from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String code = (String) complaintsModel.getValueAt(row, 0);
        Complaint c = complaintDAO.findByCode(code);
        if (c == null) return;

        JDialog dlg = new JDialog(this, "Complaint Details: " + c.getComplaintCode(), true);
        dlg.setSize(640, 560);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(c.getComplaintCode() + " - " + c.getCategory());
        title.setFont(UIUtils.FONT_HEADER_MEDIUM);
        title.setForeground(UIUtils.TEXT_MAIN);
        form.add(title);
        form.add(Box.createVerticalStrut(16));

        RoundedPanel grid = new RoundedPanel(10, UIUtils.BG_LIGHT, UIUtils.BORDER_LIGHT, 1, false);
        grid.setLayout(new GridLayout(6, 2, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        addSummaryRow(grid, "Citizen Name:", c.getCitizenName());
        addSummaryRow(grid, "Phone:", c.getPhone());
        addSummaryRow(grid, "Area & Ward:", c.getAreaName() + " (" + c.getWardNumber() + ")");
        addSummaryRow(grid, "Landmark Address:", c.getAddress());
        addSummaryRow(grid, "Priority Level:", c.getPriority() + " (Score: " + c.getPriorityScore() + ")");
        addSummaryRow(grid, "Status:", c.getStatus().getDisplayName());
        addSummaryRow(grid, "Assigned Crew:", c.getAssignedCrewName() != null ? c.getAssignedCrewName() : "None");
        addSummaryRow(grid, "Crew Vehicle:", c.getCrewVehicleNumber() != null ? c.getCrewVehicleNumber() : "N/A");
        addSummaryRow(grid, "Reported Date:", sdf.format(c.getCreatedAt()));
        addSummaryRow(grid, "Expected Resolution:", c.getExpectedResolutionTime() != null ? sdf.format(c.getExpectedResolutionTime()) : "N/A");
        addSummaryRow(grid, "Resolved Date:", c.getResolvedAt() != null ? sdf.format(c.getResolvedAt()) : "Pending");
        addSummaryRow(grid, "Proof Photo:", c.getImagePath() != null ? c.getImagePath() : "None attached");

        form.add(grid);
        form.add(Box.createVerticalStrut(14));

        JLabel reason = new JLabel("<html><b>Priority Scoring Breakdown:</b><br><span style='color: #475569;'>" + c.getPriorityReason() + "</span></html>");
        reason.setFont(UIUtils.FONT_CAPTION);
        form.add(reason);
        form.add(Box.createVerticalStrut(10));

        JLabel desc = new JLabel("<html><b>Citizen Description:</b><br><span style='color: #1E293B;'>" + c.getDescription() + "</span></html>");
        desc.setFont(UIUtils.FONT_CAPTION);
        form.add(desc);

        p.add(new JScrollPane(form), BorderLayout.CENTER);

        ModernButton closeBtn = new ModernButton("Close", ButtonStyle.SECONDARY, 8);
        closeBtn.addActionListener(e -> dlg.dispose());
        p.add(closeBtn, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void openAssignCrewModal() {
        int row = complaintsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a complaint to assign.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String code = (String) complaintsModel.getValueAt(row, 0);
        Complaint c = complaintDAO.findByCode(code);
        if (c == null) return;

        JDialog dlg = new JDialog(this, "Assign Collection Crew: " + c.getComplaintCode(), true);
        dlg.setSize(480, 360);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel t = new JLabel("Dispatch Collection Crew");
        t.setFont(UIUtils.FONT_HEADER_SMALL);
        form.add(t);
        form.add(Box.createVerticalStrut(6));

        JLabel loc = new JLabel("Complaint Location: " + c.getAreaName() + " (" + c.getWardNumber() + ")");
        loc.setFont(UIUtils.FONT_BODY);
        loc.setForeground(UIUtils.TEXT_MUTED);
        form.add(loc);
        form.add(Box.createVerticalStrut(16));

        // Available crews list
        List<Crew> candidateCrews = crewDAO.findAvailableCrewsForRouting(c.getAreaId(), c.getWardId());
        if (candidateCrews.isEmpty()) {
            candidateCrews = crewDAO.getAllCrews();
        }

        JLabel selLbl = new JLabel("Select Crew (sorted by proximity & least workload):");
        selLbl.setFont(UIUtils.FONT_CAPTION_BOLD);
        form.add(selLbl);
        form.add(Box.createVerticalStrut(6));

        JComboBox<Crew> crewCombo = new JComboBox<>(candidateCrews.toArray(new Crew[0]));
        crewCombo.setFont(UIUtils.FONT_BODY);
        form.add(crewCombo);
        form.add(Box.createVerticalStrut(16));

        JLabel notesLbl = new JLabel("Assignment Notes / Instructions:");
        notesLbl.setFont(UIUtils.FONT_CAPTION_BOLD);
        form.add(notesLbl);
        form.add(Box.createVerticalStrut(6));

        JTextField notesField = new JTextField("Dispatched for priority clearance");
        UIUtils.styleTextField(notesField);
        form.add(notesField);

        p.add(form, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        ModernButton cancelBtn = new ModernButton("Cancel", ButtonStyle.SECONDARY, 8);
        cancelBtn.addActionListener(e -> dlg.dispose());

        ModernButton confirmBtn = new ModernButton("Confirm Assignment", ButtonStyle.PRIMARY, 8);
        confirmBtn.addActionListener(e -> {
            Crew selected = (Crew) crewCombo.getSelectedItem();
            if (selected != null) {
                boolean ok = complaintService.getAssignmentService().manualAssignComplaint(
                    c.getId(), selected.getId(),
                    currentUser != null ? currentUser.getId() : null,
                    notesField.getText().trim()
                );
                if (ok) {
                    dlg.dispose();
                    refreshComplaintsTable();
                    refreshAllData();
                    NotificationToast.show(AdminDashboard.this, "Crew Assigned",
                        "Ticket " + c.getComplaintCode() + " assigned to " + selected.getCrewName(), ToastType.SUCCESS);
                } else {
                    JOptionPane.showMessageDialog(dlg, "Failed to assign crew.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);
        p.add(btnRow, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void openUpdateStatusModal() {
        int row = complaintsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a complaint to update status.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String code = (String) complaintsModel.getValueAt(row, 0);
        Complaint c = complaintDAO.findByCode(code);
        if (c == null) return;

        JDialog dlg = new JDialog(this, "Update Complaint Status: " + c.getComplaintCode(), true);
        dlg.setSize(440, 320);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel t = new JLabel("Progress Status Transition");
        t.setFont(UIUtils.FONT_HEADER_SMALL);
        form.add(t);
        form.add(Box.createVerticalStrut(14));

        JLabel selLbl = new JLabel("New Status:");
        selLbl.setFont(UIUtils.FONT_CAPTION_BOLD);
        form.add(selLbl);
        form.add(Box.createVerticalStrut(6));

        JComboBox<ComplaintStatus> statusCombo = new JComboBox<>(ComplaintStatus.values());
        statusCombo.setFont(UIUtils.FONT_BODY);
        statusCombo.setSelectedItem(c.getStatus());
        form.add(statusCombo);
        form.add(Box.createVerticalStrut(14));

        JLabel notesLbl = new JLabel("Supervisor Comments / Resolution Notes:");
        notesLbl.setFont(UIUtils.FONT_CAPTION_BOLD);
        form.add(notesLbl);
        form.add(Box.createVerticalStrut(6));

        JTextArea notesArea = new JTextArea(3, 20);
        notesArea.setFont(UIUtils.FONT_BODY);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(UIUtils.createFieldBorder());
        form.add(new JScrollPane(notesArea));

        p.add(form, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        ModernButton cancelBtn = new ModernButton("Cancel", ButtonStyle.SECONDARY, 8);
        cancelBtn.addActionListener(e -> dlg.dispose());

        ModernButton confirmBtn = new ModernButton("Save Transition", ButtonStyle.PRIMARY, 8);
        confirmBtn.addActionListener(e -> {
            ComplaintStatus newSt = (ComplaintStatus) statusCombo.getSelectedItem();
            boolean ok = complaintService.updateStatus(c.getId(), newSt, notesArea.getText().trim(),
                currentUser != null ? currentUser.getId() : null);
            if (ok) {
                dlg.dispose();
                refreshComplaintsTable();
                refreshAllData();
                NotificationToast.show(AdminDashboard.this, "Status Updated",
                    "Ticket " + c.getComplaintCode() + " is now " + newSt.getDisplayName(), ToastType.SUCCESS);
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);
        p.add(btnRow, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    // -------------------------------------------------------------
    // 3. Crew Management Panel
    // -------------------------------------------------------------
    private JPanel createCrewManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // Header with Add Crew & Refresh
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Municipal Collection Crews & Field Units");
        title.setFont(UIUtils.FONT_HEADER_MEDIUM);
        title.setForeground(UIUtils.TEXT_MAIN);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);

        ModernButton addCrewBtn = new ModernButton("➕ Add New Crew", ButtonStyle.PRIMARY, 8);
        addCrewBtn.addActionListener(e -> openAddCrewModal());

        ModernButton toggleAvailBtn = new ModernButton("⚡ Toggle Availability", ButtonStyle.SECONDARY, 8);
        toggleAvailBtn.addActionListener(e -> toggleSelectedCrewAvailability());

        rightBtns.add(toggleAvailBtn);
        rightBtns.add(addCrewBtn);

        top.add(title, BorderLayout.WEST);
        top.add(rightBtns, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        // Crew Table
        String[] cols = {"Crew ID", "Crew Name", "Leader", "Contact Phone", "Assigned Area", "Ward", "Vehicle Number", "Active Tasks", "Availability", "Status"};
        crewModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        crewTable = new StyledTable(crewModel);
        panel.add(new JScrollPane(crewTable), BorderLayout.CENTER);

        return panel;
    }

    private void refreshCrewsTable() {
        crewDAO.recalculateAllWorkloads();
        SwingWorker<List<Crew>, Void> worker = new SwingWorker<List<Crew>, Void>() {
            @Override
            protected List<Crew> doInBackground() {
                return crewDAO.getAllCrews();
            }

            @Override
            protected void done() {
                try {
                    List<Crew> list = get();
                    crewModel.setRowCount(0);
                    for (Crew c : list) {
                        crewModel.addRow(new Object[]{
                            "CRW-" + c.getId(),
                            c.getCrewName(),
                            c.getLeaderName(),
                            c.getPhone(),
                            c.getAreaName(),
                            c.getWardNumber(),
                            c.getVehicleNumber(),
                            c.getCurrentWorkload() + " active tasks",
                            c.getAvailability().getDisplayName(),
                            c.getStatus()
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void toggleSelectedCrewAvailability() {
        int row = crewTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a crew to change availability.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String rawId = (String) crewModel.getValueAt(row, 0);
        int crewId = Integer.parseInt(rawId.replace("CRW-", ""));

        Crew c = crewDAO.getCrewById(crewId);
        if (c == null) return;

        CrewAvailability next = (c.getAvailability() == CrewAvailability.AVAILABLE) ? CrewAvailability.BUSY : CrewAvailability.AVAILABLE;
        crewDAO.updateAvailability(crewId, next);
        refreshCrewsTable();
        NotificationToast.show(this, "Availability Updated", c.getCrewName() + " is now " + next.getDisplayName(), ToastType.INFO);
    }

    private void openAddCrewModal() {
        JDialog dlg = new JDialog(this, "Add New Municipal Collection Crew", true);
        dlg.setSize(480, 480);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 4, 0);

        form.add(createFieldLabel("Crew Unit Name *"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JTextField nameField = new JTextField();
        UIUtils.styleTextField(nameField);
        form.add(nameField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(createFieldLabel("Crew Leader Name *"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JTextField leaderField = new JTextField();
        UIUtils.styleTextField(leaderField);
        form.add(leaderField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(createFieldLabel("Phone Number (10 digits) *"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JTextField phoneField = new JTextField();
        UIUtils.styleTextField(phoneField);
        form.add(phoneField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(createFieldLabel("Vehicle Registration Number *"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        JTextField vehicleField = new JTextField("RJ-14-");
        UIUtils.styleTextField(vehicleField);
        form.add(vehicleField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        form.add(createFieldLabel("Assigned Area & Ward *"), gbc);
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);

        JPanel rowAW = new JPanel(new GridLayout(1, 2, 8, 0));
        rowAW.setOpaque(false);

        List<Area> areas = areaDAO.getAllAreas();
        JComboBox<Area> aCombo = new JComboBox<>(areas.toArray(new Area[0]));
        JComboBox<Ward> wCombo = new JComboBox<>();

        aCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Area a = (Area) aCombo.getSelectedItem();
                if (a != null) {
                    wCombo.removeAllItems();
                    for (Ward w : areaDAO.getWardsByAreaId(a.getId())) wCombo.addItem(w);
                }
            }
        });
        if (!areas.isEmpty()) {
            for (Ward w : areaDAO.getWardsByAreaId(areas.get(0).getId())) wCombo.addItem(w);
        }

        rowAW.add(aCombo);
        rowAW.add(wCombo);
        form.add(rowAW, gbc);

        p.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        ModernButton cancelBtn = new ModernButton("Cancel", ButtonStyle.SECONDARY, 8);
        cancelBtn.addActionListener(e -> dlg.dispose());

        ModernButton saveBtn = new ModernButton("Create Crew Unit", ButtonStyle.PRIMARY, 8);
        saveBtn.addActionListener(e -> {
            String cName = nameField.getText().trim();
            String lName = leaderField.getText().trim();
            String phone = phoneField.getText().trim();
            String veh = vehicleField.getText().trim();
            Area a = (Area) aCombo.getSelectedItem();
            Ward w = (Ward) wCombo.getSelectedItem();

            if (cName.isEmpty() || lName.isEmpty() || !ValidationUtils.isValidMobile(phone) || veh.isEmpty() || a == null || w == null) {
                JOptionPane.showMessageDialog(dlg, "Please fill in all fields with valid data.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Crew crew = new Crew(0, cName, lName, ValidationUtils.sanitizePhone(phone), a.getId(), w.getId(), veh);
            boolean ok = crewDAO.saveCrew(crew);
            if (ok) {
                dlg.dispose();
                refreshCrewsTable();
                refreshAllData();
                NotificationToast.show(AdminDashboard.this, "Crew Added", "New crew unit " + cName + " deployed.", ToastType.SUCCESS);
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to create crew record.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);
        p.add(btnRow, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    // -------------------------------------------------------------
    // 4. Area Intelligence Panel (Hotspots & Resolution Time)
    // -------------------------------------------------------------
    private JPanel createAreaIntelligencePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Smart City Area Intelligence & Problem Hotspots");
        title.setFont(UIUtils.FONT_HEADER_MEDIUM);
        title.setForeground(UIUtils.TEXT_MAIN);

        JLabel sub = new JLabel("Identifies complaint density, critical risk clusters, and average resolution speed across municipal zones");
        sub.setFont(UIUtils.FONT_BODY);
        sub.setForeground(UIUtils.TEXT_MUTED);

        JPanel leftT = new JPanel();
        leftT.setOpaque(false);
        leftT.setLayout(new BoxLayout(leftT, BoxLayout.Y_AXIS));
        leftT.add(title);
        leftT.add(Box.createVerticalStrut(4));
        leftT.add(sub);

        top.add(leftT, BorderLayout.WEST);

        ModernButton refBtn = new ModernButton("🔄 Refresh Hotspot Data", ButtonStyle.SECONDARY, 8);
        refBtn.addActionListener(e -> refreshAreaIntelligence());
        top.add(refBtn, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);

        // Table
        String[] cols = {"Area Name", "Total Complaints", "Critical Priority", "Pending Action", "Resolved", "Avg Resolution (Hrs)", "Hotspot Status"};
        areaIntelModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        areaIntelTable = new StyledTable(areaIntelModel);
        panel.add(new JScrollPane(areaIntelTable), BorderLayout.CENTER);

        return panel;
    }

    private void refreshAreaIntelligence() {
        SwingWorker<List<AnalyticsDAO.AreaIntelligenceRecord>, Void> worker = new SwingWorker<List<AnalyticsDAO.AreaIntelligenceRecord>, Void>() {
            @Override
            protected List<AnalyticsDAO.AreaIntelligenceRecord> doInBackground() {
                return analyticsService.getAreaIntelligence();
            }

            @Override
            protected void done() {
                try {
                    List<AnalyticsDAO.AreaIntelligenceRecord> list = get();
                    areaIntelModel.setRowCount(0);
                    for (AnalyticsDAO.AreaIntelligenceRecord r : list) {
                        String hotspotStatus = r.criticalCount >= 3 ? "🔥 High Risk Cluster" : (r.pendingCount >= 5 ? "⚠️ Pending Backlog" : "✅ Normal Beat");
                        areaIntelModel.addRow(new Object[]{
                            r.areaName,
                            r.totalComplaints,
                            r.criticalCount,
                            r.pendingCount,
                            r.resolvedCount,
                            r.avgResolutionHours + " hrs",
                            hotspotStatus
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void refreshAllData() {
        SwingWorker<AnalyticsService.DashboardSummary, Void> worker = new SwingWorker<AnalyticsService.DashboardSummary, Void>() {
            private Map<String, Integer> catMap;
            private Map<String, Integer> areaMap;
            private Map<String, Integer> trendMap;

            @Override
            protected AnalyticsService.DashboardSummary doInBackground() {
                catMap = analyticsService.getComplaintsByCategory();
                areaMap = analyticsService.getComplaintsByArea();
                trendMap = analyticsService.getDailyTrend();
                return analyticsService.getDashboardSummary();
            }

            @Override
            protected void done() {
                try {
                    AnalyticsService.DashboardSummary s = get();
                    cardTotal.setValue(String.valueOf(s.totalComplaints));
                    cardPending.setValue(String.valueOf(s.pendingComplaints));
                    cardInProgress.setValue(String.valueOf(s.inProgressComplaints));
                    cardResolved.setValue(String.valueOf(s.resolvedComplaints));
                    cardCritical.setValue(String.valueOf(s.criticalComplaints));
                    cardCrews.setValue(String.valueOf(s.activeCrews));

                    donutCategoryChart.setData(catMap);
                    barAreaChart.setData(areaMap);
                    trendChart.setData(trendMap);

                    refreshComplaintsTable();
                    refreshCrewsTable();
                    refreshAreaIntelligence();
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void showSystemAlertsDialog() {
        JDialog dlg = new JDialog(this, "Municipal System Alerts & Notifications", true);
        dlg.setSize(500, 420);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Recent Municipal Alerts");
        title.setFont(UIUtils.FONT_HEADER_SMALL);
        p.add(title, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setOpaque(false);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        listPanel.add(createNotifItem("🚨 Critical Complaint Logged", "Dead animal waste reported near Metro Pillar 68 (CMP-2026-1002)", "4 hours ago"));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createNotifItem("⚠️ Hotspot Alert: Mansarovar Ward 43", "7 complaints logged in Ward 43 in past 48 hours. Consider dispatching supplementary crew.", "6 hours ago"));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createNotifItem("🚛 Crew Auto-Dispatched", "Ticket CMP-2026-1001 auto-assigned to Mansarovar Green Cleaners.", "8 hours ago"));
        listPanel.add(Box.createVerticalStrut(10));
        listPanel.add(createNotifItem("✅ High Priority Ticket Resolved", "CMP-2026-1011 (Sector 2 Main Ring Road) resolved in 6 hours.", "18 hours ago"));

        p.add(new JScrollPane(listPanel), BorderLayout.CENTER);

        ModernButton closeBtn = new ModernButton("Close", ButtonStyle.SECONDARY, 8);
        closeBtn.addActionListener(e -> dlg.dispose());
        p.add(closeBtn, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private RoundedPanel createNotifItem(String title, String desc, String time) {
        RoundedPanel p = new RoundedPanel(8, UIUtils.BG_LIGHT, UIUtils.BORDER_LIGHT, 1, false);
        p.setLayout(new BorderLayout(8, 4));
        p.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel t = new JLabel(title);
        t.setFont(UIUtils.FONT_BODY_BOLD);
        t.setForeground(UIUtils.TEXT_MAIN);

        JLabel d = new JLabel("<html><body style='width: 360px; color: #64748B;'>" + desc + "</body></html>");
        d.setFont(UIUtils.FONT_CAPTION);

        JLabel tm = new JLabel(time);
        tm.setFont(UIUtils.FONT_CAPTION);
        tm.setForeground(UIUtils.TEXT_MUTED);

        p.add(t, BorderLayout.NORTH);
        p.add(d, BorderLayout.CENTER);
        p.add(tm, BorderLayout.EAST);
        return p;
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

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIUtils.FONT_CAPTION_BOLD);
        lbl.setForeground(UIUtils.TEXT_MAIN);
        return lbl;
    }
}
