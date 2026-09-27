package service;

import dao.ComplaintDAO;
import dao.CrewDAO;
import dao.FeedbackDAO;
import dao.StatusHistoryDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import model.Complaint;
import model.Enums.ComplaintStatus;
import model.Enums.PriorityLevel;
import model.Feedback;

/**
 * Main Service for Municipal Complaint Lifecycle.
 */
public class ComplaintService {

    private final ComplaintDAO complaintDAO;
    private final PriorityEngine priorityEngine;
    private final AssignmentService assignmentService;
    private final StatusHistoryDAO statusHistoryDAO;
    private final FeedbackDAO feedbackDAO;
    private final CrewDAO crewDAO;

    public ComplaintService() {
        this.complaintDAO = new ComplaintDAO();
        this.priorityEngine = new PriorityEngine(this.complaintDAO);
        this.assignmentService = new AssignmentService();
        this.statusHistoryDAO = new StatusHistoryDAO();
        this.feedbackDAO = new FeedbackDAO();
        this.crewDAO = new CrewDAO();
    }

    /**
     * Submits a complaint, calculates AI-assisted priority score,
     * persists it, and optionally auto-routes to an area crew.
     */
    public Complaint submitComplaint(Complaint complaint, boolean autoRoute) {
        if (complaint == null) return null;

        // 1. Calculate Priority using AI Priority Engine
        PriorityEngine.PriorityResult pr = priorityEngine.calculatePriority(complaint);
        complaint.setPriority(pr.getLevel());
        complaint.setPriorityScore(pr.getScore());
        complaint.setPriorityReason(pr.getReason());
        complaint.setStatus(ComplaintStatus.NEW);

        // 2. Persist to DB
        boolean created = complaintDAO.createComplaint(complaint);
        if (!created) {
            return null;
        }

        // 3. Log initial status history and audit events
        statusHistoryDAO.logTransition(
            complaint.getId(),
            null,
            ComplaintStatus.NEW.name(),
            complaint.getCitizenUserId(),
            "Complaint reported. Priority: " + pr.getLevel() + " (Score: " + pr.getScore() + ")"
        );
        AuditService.logComplaintCreated(complaint);
        AuditService.logPriorityCalculated(complaint, "Assigned " + pr.getLevel() + " (Score " + pr.getScore() + "/100). " + pr.getReason());

        // 4. Send notification for Critical complaints
        if (complaint.getPriority() == PriorityLevel.CRITICAL) {
            sendNotification(null, "ADMIN", "URGENT: Critical Complaint " + complaint.getComplaintCode(),
                "Critical complaint reported in " + complaint.getAreaName() + ": " + complaint.getCategory());
        }

        // 5. Intelligent Area-Wise Auto-Routing
        if (autoRoute) {
            AssignmentService.RoutingResult rr = assignmentService.autoRouteComplaint(complaint, complaint.getCitizenUserId());
            if (rr.isSuccess()) {
                sendNotification(null, "CREW", "New Task Dispatched",
                    "Complaint " + complaint.getComplaintCode() + " assigned to your crew in " + complaint.getAreaName());
            }
        }

        return complaint;
    }

    /**
     * Updates complaint status with audit logging and crew workload adjustment.
     */
    public boolean updateStatus(int complaintId, ComplaintStatus newStatus, String notes, Integer changedByUserId) {
        Complaint complaint = complaintDAO.findById(complaintId);
        if (complaint == null) return false;

        ComplaintStatus oldStatus = complaint.getStatus();
        if (oldStatus == newStatus) return true;

        boolean ok = complaintDAO.updateStatus(complaintId, newStatus, notes);
        if (ok) {
            // Decrement crew workload if completed or rejected
            if ((newStatus == ComplaintStatus.RESOLVED || newStatus == ComplaintStatus.REJECTED) &&
                (oldStatus == ComplaintStatus.ASSIGNED || oldStatus == ComplaintStatus.IN_PROGRESS)) {
                if (complaint.getAssignedCrewId() != null) {
                    crewDAO.updateWorkload(complaint.getAssignedCrewId(), -1);
                }
            }

            statusHistoryDAO.logTransition(complaintId, oldStatus.name(), newStatus.name(), changedByUserId, notes);
            AuditService.logStatusChanged(complaintId, oldStatus.name(), newStatus.name(), null, notes);

            // Notify citizen if resolved
            if (newStatus == ComplaintStatus.RESOLVED && complaint.getCitizenUserId() != null) {
                sendNotification(complaint.getCitizenUserId(), "CITIZEN", "Complaint Resolved",
                    "Your complaint #" + complaint.getComplaintCode() + " has been resolved. Please rate our service.");
            }
            return true;
        }
        return false;
    }

    public boolean submitFeedback(int complaintId, Integer citizenUserId, int rating, String comments) {
        Feedback fb = new Feedback(complaintId, citizenUserId, rating, comments);
        return feedbackDAO.submitFeedback(fb);
    }

    public PriorityEngine getPriorityEngine() {
        return priorityEngine;
    }

    public AssignmentService getAssignmentService() {
        return assignmentService;
    }

    private void sendNotification(Integer userId, String roleTarget, String title, String message) {
        String sql = "INSERT INTO notifications (user_id, role_target, title, message) VALUES (?, ?, ?, ?)";
        try (Connection conn = config.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) ps.setInt(1, userId); else ps.setNull(1, java.sql.Types.INTEGER);
            ps.setString(2, roleTarget);
            ps.setString(3, title);
            ps.setString(4, message);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }
}
