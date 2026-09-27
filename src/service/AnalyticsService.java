package service;

import dao.AnalyticsDAO;
import dao.ComplaintDAO;
import dao.CrewDAO;
import java.util.List;
import java.util.Map;
import model.Enums.ComplaintStatus;

public class AnalyticsService {

    private final AnalyticsDAO analyticsDAO;
    private final ComplaintDAO complaintDAO;
    private final CrewDAO crewDAO;

    public AnalyticsService() {
        this.analyticsDAO = new AnalyticsDAO();
        this.complaintDAO = new ComplaintDAO();
        this.crewDAO = new CrewDAO();
    }

    public static class DashboardSummary {
        public final int totalComplaints;
        public final int pendingComplaints;
        public final int inProgressComplaints;
        public final int resolvedComplaints;
        public final int criticalComplaints;
        public final int highComplaints;
        public final int activeCrews;

        public DashboardSummary(int total, int pending, int inProgress, int resolved, int critical, int high, int activeCrews) {
            this.totalComplaints = total;
            this.pendingComplaints = pending;
            this.inProgressComplaints = inProgress;
            this.resolvedComplaints = resolved;
            this.criticalComplaints = critical;
            this.highComplaints = high;
            this.activeCrews = activeCrews;
        }
    }

    public DashboardSummary getDashboardSummary() {
        int total = complaintDAO.countTotal();
        int pending = complaintDAO.countByStatus(ComplaintStatus.NEW) +
                      complaintDAO.countByStatus(ComplaintStatus.ASSIGNED) +
                      complaintDAO.countByStatus(ComplaintStatus.PENDING_ASSIGNMENT);
        int inProgress = complaintDAO.countByStatus(ComplaintStatus.IN_PROGRESS);
        int resolved = complaintDAO.countByStatus(ComplaintStatus.RESOLVED);
        int critical = complaintDAO.countCritical();
        int high = complaintDAO.countHigh();
        int activeCrews = crewDAO.getAllCrews().size();

        return new DashboardSummary(total, pending, inProgress, resolved, critical, high, activeCrews);
    }

    public Map<String, Integer> getComplaintsByCategory() {
        return analyticsDAO.getComplaintsByCategory();
    }

    public Map<String, Integer> getComplaintsByArea() {
        return analyticsDAO.getComplaintsByArea();
    }

    public Map<String, Integer> getPriorityDistribution() {
        return analyticsDAO.getPriorityDistribution();
    }

    public List<AnalyticsDAO.AreaIntelligenceRecord> getAreaIntelligence() {
        return analyticsDAO.getAreaIntelligence();
    }

    public Map<String, Integer> getDailyTrend() {
        return analyticsDAO.getDailyTrendLast7Days();
    }
}
