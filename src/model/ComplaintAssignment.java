package model;

import java.sql.Timestamp;

public class ComplaintAssignment {
    private int id;
    private int complaintId;
    private int crewId;
    private String crewName;
    private Integer assignedByUserId;
    private String assignedByName;
    private Timestamp assignedAt;
    private String notes;

    public ComplaintAssignment() {}

    public ComplaintAssignment(int complaintId, int crewId, Integer assignedByUserId, String notes) {
        this.complaintId = complaintId;
        this.crewId = crewId;
        this.assignedByUserId = assignedByUserId;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public int getCrewId() { return crewId; }
    public void setCrewId(int crewId) { this.crewId = crewId; }

    public String getCrewName() { return crewName; }
    public void setCrewName(String crewName) { this.crewName = crewName; }

    public Integer getAssignedByUserId() { return assignedByUserId; }
    public void setAssignedByUserId(Integer assignedByUserId) { this.assignedByUserId = assignedByUserId; }

    public String getAssignedByName() { return assignedByName; }
    public void setAssignedByName(String assignedByName) { this.assignedByName = assignedByName; }

    public Timestamp getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Timestamp assignedAt) { this.assignedAt = assignedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
