package ui;

import dao.ComplaintDAO;
import dao.CrewDAO;
import dao.StatusHistoryDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
import model.Complaint;
import model.Crew;
import model.Enums.ComplaintStatus;
import model.StatusHistory;
import model.User;
import service.ComplaintService;
import ui.components.ModernButton;
import ui.components.ModernButton.ButtonStyle;
import ui.components.NotificationToast;
import ui.components.NotificationToast.ToastType;
import ui.components.PriorityBadge;
import ui.components.RoundedPanel;
import ui.components.StatusBadge;
import ui.components.StyledTable;
import utils.SessionManager;
import utils.UIUtils;

/**
 * Modern Collection Crew Field Dashboard.
 */
public class CrewDashboard extends JFrame {

    private final ComplaintService complaintService;
    private final ComplaintDAO complaintDAO;
    private final CrewDAO crewDAO;
    private final StatusHistoryDAO statusHistoryDAO;

    private final User currentUser;
    private Crew currentCrew;

    private JLabel crewWorkloadBadge;
    private StyledTable tasksTable;
    private DefaultTableModel tasksModel;
    private JCheckBox onlyActiveCheck;

    public CrewDashboard() {
        this.complaintService = new ComplaintService();
        this.complaintDAO = new ComplaintDAO();
        this.crewDAO = new CrewDAO();
        this.statusHistoryDAO = new StatusHistoryDAO();

        this.currentUser = SessionManager.getCurrentUser();
        this.currentCrew = SessionManager.getCurrentCrew();

        if (this.currentCrew == null && this.currentUser != null) {
            this.currentCrew = crewDAO.getCrewByUserId(this.currentUser.getId());
            SessionManager.setCurrentCrew(this.currentCrew);
        }

        // Fallback to Crew 1 if none bound to user
        if (this.currentCrew == null) {
            this.currentCrew = crewDAO.getCrewById(1);
            SessionManager.setCurrentCrew(this.currentCrew);
        }

        setTitle("Smart City Waste Portal - Collection Crew Field Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);

        initUI();
        refreshTasks();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG_LIGHT);

        // Header
        root.add(createHeader(), BorderLayout.NORTH);

        // Main Table Area
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Sub-header with Active filter & actions
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBar.setOpaque(false);

        JLabel t = new JLabel("Assigned Waste Collection Tasks");
        t.setFont(UIUtils.FONT_HEADER_SMALL);
        t.setForeground(UIUtils.TEXT_MAIN);

        onlyActiveCheck = new JCheckBox("Show only active (Pending / In Progress) tasks", true);
        onlyActiveCheck.setFont(UIUtils.FONT_BODY);
        onlyActiveCheck.setOpaque(false);
        onlyActiveCheck.addActionListener(e -> refreshTasks());

        leftBar.add(t);
        leftBar.add(onlyActiveCheck);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBar.setOpaque(false);

        ModernButton refBtn = new ModernButton("🔄 Refresh", ButtonStyle.SECONDARY, 8);
        refBtn.addActionListener(e -> refreshTasks());
        rightBar.add(refBtn);

        toolbar.add(leftBar, BorderLayout.WEST);
        toolbar.add(rightBar, BorderLayout.EAST);
        center.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Ticket ID", "Area & Ward", "Locality", "Landmark Address", "Category", "Priority", "Score", "Status", "Reported Time", "Target Deadline"};
        tasksModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tasksTable = new StyledTable(tasksModel);
        center.add(new JScrollPane(tasksTable), BorderLayout.CENTER);

        // Bottom Action Bar: Accept / Start Work, Mark Resolved
        RoundedPanel actionCard = new RoundedPanel(10, Color.WHITE, UIUtils.BORDER_LIGHT, 1, false);
        actionCard.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 10));

        ModernButton viewBtn = new ModernButton("👁️ View Details", ButtonStyle.SECONDARY, 8);
        viewBtn.addActionListener(e -> openViewDetailsModal());

        ModernButton startBtn = new ModernButton("🚀 Accept & Start Work (In Progress)", ButtonStyle.DARK, 8);
        startBtn.addActionListener(e -> startWorkOnSelected());

        ModernButton resolveBtn = new ModernButton("✅ Mark Resolved & Add Notes", ButtonStyle.PRIMARY, 8);
        resolveBtn.addActionListener(e -> openResolveModal());

        actionCard.add(viewBtn);
        actionCard.add(startBtn);
        actionCard.add(resolveBtn);

        center.add(actionCard, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(1000, 78));
        header.setBackground(UIUtils.NAVY_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        // Left Crew Info
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        String crewName = currentCrew != null ? currentCrew.getCrewName() : "Collection Crew";
        String vehicle = currentCrew != null ? currentCrew.getVehicleNumber() : "RJ-14-EA-1001";
        double cap = currentCrew != null ? currentCrew.getVehicleCapacity() : 3.5;
        String area = currentCrew != null ? currentCrew.getAreaName() : "Mansarovar";
        String wards = currentCrew != null && currentCrew.getWardCoverage() != null ? currentCrew.getWardCoverage() : "All Wards";
        int members = currentCrew != null ? currentCrew.getMembersCount() : 4;
        String desc = currentCrew != null && currentCrew.getMembersDescription() != null ? currentCrew.getMembersDescription() : "Driver + 3 Field Staff";

        JLabel cName = new JLabel("🚛 " + crewName + " • " + vehicle + " (" + cap + "T Capacity)");
        cName.setFont(UIUtils.FONT_HEADER_SMALL);
        cName.setForeground(Color.WHITE);

        JLabel cSub = new JLabel("Zone: " + area + " [Wards: " + wards + "] • Team: " + members + " Staff (" + desc + ")");
        cSub.setFont(UIUtils.FONT_CAPTION);
        cSub.setForeground(new Color(148, 163, 184));

        left.add(Box.createVerticalStrut(14));
        left.add(cName);
        left.add(Box.createVerticalStrut(4));
        left.add(cSub);

        // Right Workload Badge & Sign Out
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 20));
        right.setOpaque(false);

        crewWorkloadBadge = new JLabel("WORKLOAD: 0 ACTIVE TASKS");
        crewWorkloadBadge.setFont(UIUtils.FONT_CAPTION_BOLD);
        crewWorkloadBadge.setForeground(UIUtils.COLOR_PRIMARY);
        crewWorkloadBadge.setBackground(UIUtils.NAVY_SURFACE);
        crewWorkloadBadge.setOpaque(true);
        crewWorkloadBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        ModernButton logoutBtn = new ModernButton("Sign Out", ButtonStyle.SECONDARY, 6);
        logoutBtn.addActionListener(e -> {
            SessionManager.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });

        right.add(crewWorkloadBadge);
        right.add(logoutBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void refreshTasks() {
        if (currentCrew == null) return;
        boolean onlyActive = onlyActiveCheck != null && onlyActiveCheck.isSelected();

        SwingWorker<List<Complaint>, Void> worker = new SwingWorker<List<Complaint>, Void>() {
            @Override
            protected List<Complaint> doInBackground() {
                return complaintDAO.getByCrewId(currentCrew.getId(), onlyActive);
            }

            @Override
            protected void done() {
                try {
                    List<Complaint> list = get();
                    tasksModel.setRowCount(0);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a");
                    int activeCount = 0;

                    for (Complaint c : list) {
                        if (c.getStatus() == ComplaintStatus.ASSIGNED || c.getStatus() == ComplaintStatus.IN_PROGRESS) {
                            activeCount++;
                        }
                        tasksModel.addRow(new Object[]{
                            c.getComplaintCode(),
                            c.getAreaName() + " (" + c.getWardNumber() + ")",
                            c.getLocality() != null ? c.getLocality() : "-",
                            c.getAddress(),
                            c.getCategory(),
                            c.getPriority(),
                            c.getPriorityScore(),
                            c.getStatus(),
                            sdf.format(c.getCreatedAt()),
                            c.getExpectedResolutionTime() != null ? sdf.format(c.getExpectedResolutionTime()) : "Within 24 Hours"
                        });
                    }

                    crewWorkloadBadge.setText("WORKLOAD: " + activeCount + " ACTIVE TASKS");
                    if (activeCount > 3) {
                        crewWorkloadBadge.setForeground(UIUtils.PRIORITY_HIGH);
                    } else {
                        crewWorkloadBadge.setForeground(UIUtils.COLOR_PRIMARY);
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void startWorkOnSelected() {
        int row = tasksTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a complaint from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String code = (String) tasksModel.getValueAt(row, 0);
        Complaint c = complaintDAO.findByCode(code);
        if (c == null) return;

        if (c.getStatus() == ComplaintStatus.IN_PROGRESS) {
            JOptionPane.showMessageDialog(this, "Work is already in progress for this complaint.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (c.getStatus() == ComplaintStatus.RESOLVED) {
            JOptionPane.showMessageDialog(this, "This complaint has already been resolved.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean ok = complaintService.updateStatus(c.getId(), ComplaintStatus.IN_PROGRESS,
            "Crew " + (currentCrew != null ? currentCrew.getCrewName() : "Field Unit") + " arrived on site and commenced waste clearing.",
            currentUser != null ? currentUser.getId() : null);

        if (ok) {
            refreshTasks();
            NotificationToast.show(this, "Task Started", "Complaint " + c.getComplaintCode() + " is now In Progress.", ToastType.SUCCESS);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update task status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openResolveModal() {
        int row = tasksTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a complaint to resolve.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String code = (String) tasksModel.getValueAt(row, 0);
        Complaint c = complaintDAO.findByCode(code);
        if (c == null) return;

        if (c.getStatus() == ComplaintStatus.RESOLVED) {
            JOptionPane.showMessageDialog(this, "Complaint is already marked as resolved.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(this, "Complete & Resolve: " + c.getComplaintCode(), true);
        dlg.setSize(520, 420);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel t = new JLabel("Record Work Completion & Resolution Notes");
        t.setFont(UIUtils.FONT_HEADER_SMALL);
        form.add(t);
        form.add(Box.createVerticalStrut(4));

        JLabel loc = new JLabel("Location: " + c.getAddress() + " (" + c.getAreaName() + ")");
        loc.setFont(UIUtils.FONT_BODY);
        loc.setForeground(UIUtils.TEXT_MUTED);
        form.add(loc);
        form.add(Box.createVerticalStrut(14));

        JLabel notesLbl = new JLabel("Resolution Action Notes *:");
        notesLbl.setFont(UIUtils.FONT_CAPTION_BOLD);
        form.add(notesLbl);
        form.add(Box.createVerticalStrut(6));

        JTextArea notesArea = new JTextArea(4, 20);
        notesArea.setFont(UIUtils.FONT_BODY);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setText("Waste completely cleared, compactor vehicle loaded, and site sanitized with disinfectant.");
        notesArea.setBorder(UIUtils.createFieldBorder());
        form.add(new JScrollPane(notesArea));
        form.add(Box.createVerticalStrut(12));

        JLabel photoLbl = new JLabel("Proof Photo (Optional):");
        photoLbl.setFont(UIUtils.FONT_CAPTION_BOLD);
        form.add(photoLbl);
        form.add(Box.createVerticalStrut(6));

        JPanel photoRow = new JPanel(new BorderLayout(8, 0));
        photoRow.setOpaque(false);
        JTextField photoField = new JTextField();
        UIUtils.styleTextField(photoField);
        ModernButton browseBtn = new ModernButton("Browse...", ButtonStyle.SECONDARY, 8);
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                photoField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        photoRow.add(photoField, BorderLayout.CENTER);
        photoRow.add(browseBtn, BorderLayout.EAST);
        form.add(photoRow);

        p.add(form, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        ModernButton cancelBtn = new ModernButton("Cancel", ButtonStyle.SECONDARY, 8);
        cancelBtn.addActionListener(e -> dlg.dispose());

        ModernButton confirmBtn = new ModernButton("Mark as Resolved", ButtonStyle.PRIMARY, 8);
        confirmBtn.addActionListener(e -> {
            String notes = notesArea.getText().trim();
            if (notes.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please provide brief resolution notes.", "Notes Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = complaintService.updateStatus(c.getId(), ComplaintStatus.RESOLVED, notes,
                currentUser != null ? currentUser.getId() : null);

            if (ok) {
                dlg.dispose();
                refreshTasks();
                NotificationToast.show(CrewDashboard.this, "Complaint Resolved",
                    "Ticket " + c.getComplaintCode() + " cleared successfully.", ToastType.SUCCESS);
            } else {
                JOptionPane.showMessageDialog(dlg, "Failed to resolve complaint.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);
        p.add(btnRow, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void openViewDetailsModal() {
        int row = tasksTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a complaint from the table first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String code = (String) tasksModel.getValueAt(row, 0);
        Complaint c = complaintDAO.findByCode(code);
        if (c == null) return;

        JDialog dlg = new JDialog(this, "Task Details: " + c.getComplaintCode(), true);
        dlg.setSize(560, 480);
        dlg.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(c.getComplaintCode() + " (" + c.getCategory() + ")");
        title.setFont(UIUtils.FONT_HEADER_MEDIUM);
        form.add(title);
        form.add(Box.createVerticalStrut(14));

        RoundedPanel grid = new RoundedPanel(10, UIUtils.BG_LIGHT, UIUtils.BORDER_LIGHT, 1, false);
        grid.setLayout(new GridLayout(5, 2, 8, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");

        addSummaryRow(grid, "Location:", c.getAddress() + ", " + c.getAreaName() + " (" + c.getWardNumber() + ")");
        addSummaryRow(grid, "Citizen Name:", c.getCitizenName());
        addSummaryRow(grid, "Contact Phone:", c.getPhone());
        addSummaryRow(grid, "Priority Level:", c.getPriority() + " (Score: " + c.getPriorityScore() + ")");
        addSummaryRow(grid, "Current Status:", c.getStatus().getDisplayName());
        addSummaryRow(grid, "Reported Date:", sdf.format(c.getCreatedAt()));
        addSummaryRow(grid, "Target Resolution:", c.getExpectedResolutionTime() != null ? sdf.format(c.getExpectedResolutionTime()) : "N/A");
        addSummaryRow(grid, "Public Health Risk:", c.isHealthRisk() ? "YES 🚨" : "No");
        addSummaryRow(grid, "Garbage Overflow:", c.isGarbageOverflow() ? "YES 🗑️" : "No");
        addSummaryRow(grid, "Proof Photo:", c.getImagePath() != null ? c.getImagePath() : "None");

        form.add(grid);
        form.add(Box.createVerticalStrut(12));

        JLabel desc = new JLabel("<html><b>Citizen Description & Hazard Notes:</b><br><span style='color: #334155;'>" + c.getDescription() + "</span></html>");
        desc.setFont(UIUtils.FONT_CAPTION);
        form.add(desc);

        p.add(new JScrollPane(form), BorderLayout.CENTER);

        ModernButton closeBtn = new ModernButton("Close", ButtonStyle.SECONDARY, 8);
        closeBtn.addActionListener(e -> dlg.dispose());
        p.add(closeBtn, BorderLayout.SOUTH);

        dlg.setContentPane(p);
        dlg.setVisible(true);
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
}
