package service;

import dao.AuditLogDAO;
import model.AuditLog;
import model.Complaint;
import model.Crew;
import model.User;

public class AuditService {

    private static final AuditLogDAO dao = new AuditLogDAO();

    public static void logAction(Integer userId, String username, String action, String targetType, Integer targetId, String details) {
        AuditLog log = new AuditLog(userId, username != null ? username : "SYSTEM", action, targetType, targetId, details);
        new Thread(() -> dao.insertLog(log)).start();
    }

    public static void logLogin(User user) {
        if (user == null) return;
        logAction(user.getId(), user.getUsername(), "LOGIN", "USER", user.getId(),
            user.getFullName() + " (" + user.getRole().getDisplayName() + ") logged in successfully.");
    }

    public static void logComplaintCreated(Complaint c) {
        if (c == null) return;
        logAction(c.getCitizenUserId(), c.getCitizenName(), "COMPLAINT_CREATED", "COMPLAINT", c.getId(),
            "Registered ticket " + c.getComplaintCode() + " in " + c.getAreaName() + " (" + c.getWardNumber() +
            ") [Priority: " + c.getPriority() + " Score: " + c.getPriorityScore() + "]");
    }

    public static void logPriorityCalculated(Complaint c, String details) {
        if (c == null) return;
        logAction(null, "AI_ENGINE", "PRIORITY_CALCULATED", "COMPLAINT", c.getId(),
            "Evaluated " + c.getComplaintCode() + ": " + details);
    }

    public static void logCrewAssigned(int complaintId, int crewId, String crewName, User assignedBy) {
        String actor = assignedBy != null ? assignedBy.getUsername() : "SMART_ROUTING";
        Integer uid = assignedBy != null ? assignedBy.getId() : null;
        logAction(uid, actor, "CREW_ASSIGNED", "COMPLAINT", complaintId,
            "Assigned to " + crewName + " (Crew ID: " + crewId + ")");
    }

    public static void logCrewReassigned(int complaintId, int crewId, String crewName, User assignedBy, String reason) {
        String actor = assignedBy != null ? assignedBy.getUsername() : "ADMIN";
        Integer uid = assignedBy != null ? assignedBy.getId() : null;
        logAction(uid, actor, "CREW_REASSIGNED", "COMPLAINT", complaintId,
            "Reassigned to " + crewName + ". Note: " + reason);
    }

    public static void logStatusChanged(int complaintId, String oldStatus, String newStatus, User user, String notes) {
        String actor = user != null ? user.getUsername() : "SYSTEM";
        Integer uid = user != null ? user.getId() : null;
        logAction(uid, actor, "STATUS_CHANGED", "COMPLAINT", complaintId,
            "Transitioned from " + oldStatus + " to " + newStatus + ". Notes: " + (notes != null ? notes : "None"));
    }

    public static void logCrewCreated(Crew crew, User user) {
        String actor = user != null ? user.getUsername() : "ADMIN";
        Integer uid = user != null ? user.getId() : null;
        logAction(uid, actor, "CREW_CREATED", "CREW", crew.getId(),
            "Created crew unit '" + crew.getCrewName() + "' (Vehicle: " + crew.getVehicleNumber() +
            ", Capacity: " + crew.getVehicleCapacity() + "T)");
    }

    public static void logCrewUpdated(Crew crew, User user, String action) {
        String actor = user != null ? user.getUsername() : "ADMIN";
        Integer uid = user != null ? user.getId() : null;
        logAction(uid, actor, "CREW_MODIFIED", "CREW", crew.getId(),
            "Updated crew '" + crew.getCrewName() + "' - " + action);
    }

    public static void logAreaWardModified(String details, User user) {
        String actor = user != null ? user.getUsername() : "ADMIN";
        Integer uid = user != null ? user.getId() : null;
        logAction(uid, actor, "AREA_WARD_MODIFIED", "AREA_WARD", null, details);
    }

    public static AuditLogDAO getDao() {
        return dao;
    }
}
