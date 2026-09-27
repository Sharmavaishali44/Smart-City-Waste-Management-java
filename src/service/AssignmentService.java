package service;

import dao.ComplaintDAO;
import dao.CrewDAO;
import dao.StatusHistoryDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import model.Complaint;
import model.Crew;
import model.Enums.ComplaintStatus;
import model.Enums.PriorityLevel;

/**
 * Intelligent Area-Wise Routing Engine for Collection Crews.
 */
public class AssignmentService {

    private final CrewDAO crewDAO;
    private final ComplaintDAO complaintDAO;
    private final StatusHistoryDAO statusHistoryDAO;

    public AssignmentService() {
        this.crewDAO = new CrewDAO();
        this.complaintDAO = new ComplaintDAO();
        this.statusHistoryDAO = new StatusHistoryDAO();
    }

    public AssignmentService(CrewDAO crewDAO, ComplaintDAO complaintDAO, StatusHistoryDAO statusHistoryDAO) {
        this.crewDAO = crewDAO;
        this.complaintDAO = complaintDAO;
        this.statusHistoryDAO = statusHistoryDAO;
    }

    public static class RoutingResult {
        private final boolean success;
        private final Crew assignedCrew;
        private final Timestamp expectedResolutionTime;
        private final String message;

        public RoutingResult(boolean success, Crew assignedCrew, Timestamp expectedResolutionTime, String message) {
            this.success = success;
            this.assignedCrew = assignedCrew;
            this.expectedResolutionTime = expectedResolutionTime;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public Crew getAssignedCrew() { return assignedCrew; }
        public Timestamp getExpectedResolutionTime() { return expectedResolutionTime; }
        public String getMessage() { return message; }
    }

    /**
     * Automatically routes a complaint to the best available crew.
     */
    public RoutingResult autoRouteComplaint(Complaint complaint, Integer assignedByUserId) {
        if (complaint == null) {
            return new RoutingResult(false, null, null, "Invalid complaint object.");
        }

        List<Crew> candidates = crewDAO.findAvailableCrewsForRouting(complaint.getAreaId(), complaint.getWardId());
        if (candidates.isEmpty()) {
            complaintDAO.setPendingAssignment(complaint.getId());
            complaint.setStatus(ComplaintStatus.PENDING_ASSIGNMENT);
            statusHistoryDAO.logTransition(
                complaint.getId(),
                complaint.getStatus() != null ? complaint.getStatus().name() : "NEW",
                ComplaintStatus.PENDING_ASSIGNMENT.name(),
                assignedByUserId,
                "No active crew available in " + complaint.getAreaName() + ". Moved to PENDING_ASSIGNMENT queue."
            );
            sendNotification(null, "ADMIN", "Pending Assignment: " + complaint.getComplaintCode(),
                "No collection crew currently available in " + complaint.getAreaName() + " (Ward " + complaint.getWardNumber() + "). Action required.");
            AuditService.logAction(assignedByUserId, "SMART_ROUTING", "STATUS_CHANGED", "COMPLAINT", complaint.getId(),
                "No crew available in " + complaint.getAreaName() + ". Ticket set to PENDING_ASSIGNMENT.");
            return new RoutingResult(false, null, null, "No available active crew in " + complaint.getAreaName() + ". Set to PENDING_ASSIGNMENT.");
        }

        // Candidates are already sorted by: (exact ward match first, then lowest workload)
        Crew selectedCrew = candidates.get(0);

        // Compute expected resolution deadline
        Timestamp deadline = computeResolutionDeadline(complaint.getPriority(), selectedCrew.getCurrentWorkload());

        // Update DB
        boolean assigned = complaintDAO.assignCrew(complaint.getId(), selectedCrew.getId(), deadline);
        if (assigned) {
            crewDAO.updateWorkload(selectedCrew.getId(), 1);
            recordAssignmentLog(complaint.getId(), selectedCrew.getId(), assignedByUserId,
                "Smart Routing auto-dispatched to " + selectedCrew.getCrewName() +
                " (Workload: " + selectedCrew.getCurrentWorkload() + ")");

            statusHistoryDAO.logTransition(
                complaint.getId(),
                complaint.getStatus() != null ? complaint.getStatus().name() : "NEW",
                ComplaintStatus.ASSIGNED.name(),
                assignedByUserId,
                "Auto-assigned to Crew: " + selectedCrew.getCrewName() + " (" + selectedCrew.getVehicleNumber() + ")"
            );

            AuditService.logCrewAssigned(complaint.getId(), selectedCrew.getId(), selectedCrew.getCrewName(), null);

            // Update in-memory complaint
            complaint.setAssignedCrewId(selectedCrew.getId());
            complaint.setAssignedCrewName(selectedCrew.getCrewName());
            complaint.setStatus(ComplaintStatus.ASSIGNED);
            complaint.setExpectedResolutionTime(deadline);

            return new RoutingResult(true, selectedCrew, deadline,
                "Assigned to " + selectedCrew.getCrewName() + " (" + selectedCrew.getVehicleNumber() + ")");
        }

        return new RoutingResult(false, null, null, "Failed to update database assignment.");
    }

    /**
     * Manual crew assignment/reassignment by municipal admin.
     */
    public boolean manualAssignComplaint(int complaintId, int crewId, Integer assignedByUserId, String notes) {
        Complaint complaint = complaintDAO.findById(complaintId);
        Crew crew = crewDAO.getCrewById(crewId);
        if (complaint == null || crew == null) return false;

        // If previously assigned to another crew, decrement their workload
        if (complaint.getAssignedCrewId() != null && complaint.getAssignedCrewId() != crewId) {
            crewDAO.updateWorkload(complaint.getAssignedCrewId(), -1);
        }

        Timestamp deadline = computeResolutionDeadline(complaint.getPriority(), crew.getCurrentWorkload());
        boolean ok = complaintDAO.assignCrew(complaintId, crewId, deadline);
        if (ok) {
            crewDAO.updateWorkload(crewId, 1);
            recordAssignmentLog(complaintId, crewId, assignedByUserId, notes != null ? notes : "Admin reassigned to " + crew.getCrewName());
            statusHistoryDAO.logTransition(
                complaintId,
                complaint.getStatus().name(),
                ComplaintStatus.ASSIGNED.name(),
                assignedByUserId,
                "Manual assignment to " + crew.getCrewName() + ". " + (notes != null ? notes : "")
            );
            AuditService.logCrewReassigned(complaintId, crewId, crew.getCrewName(), null, notes);
            sendNotification(null, "CREW", "Manual Task Assignment",
                "Complaint " + complaint.getComplaintCode() + " manually assigned to " + crew.getCrewName());
            return true;
        }
        return false;
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

    private Timestamp computeResolutionDeadline(PriorityLevel priority, int crewWorkload) {
        Calendar cal = Calendar.getInstance();
        int baseHours;
        switch (priority != null ? priority : PriorityLevel.MEDIUM) {
            case CRITICAL:
                baseHours = 4;
                break;
            case HIGH:
                baseHours = 12;
                break;
            case MEDIUM:
                baseHours = 24;
                break;
            case LOW:
            default:
                baseHours = 48;
                break;
        }

        // Slight adjustment for crew workload queue (e.g. +1 hour per existing task)
        baseHours += Math.min(crewWorkload * 1, 12);

        cal.add(Calendar.HOUR_OF_DAY, baseHours);
        return new Timestamp(cal.getTimeInMillis());
    }

    private void recordAssignmentLog(int complaintId, int crewId, Integer userId, String notes) {
        String sql = "INSERT INTO complaint_assignments (complaint_id, crew_id, assigned_by_user_id, notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = config.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            ps.setInt(2, crewId);
            if (userId != null) ps.setInt(3, userId); else ps.setNull(3, java.sql.Types.INTEGER);
            ps.setString(4, notes);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[AssignmentService] Error recordAssignmentLog: " + e.getMessage());
        }
    }
}
