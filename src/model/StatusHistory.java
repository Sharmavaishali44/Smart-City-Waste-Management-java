package model;

import java.sql.Timestamp;

public class StatusHistory {
    private int id;
    private int complaintId;
    private String oldStatus;
    private String newStatus;
    private Integer changedByUserId;
    private String changedByName;
    private String comments;
    private Timestamp changedAt;

    public StatusHistory() {}

    public StatusHistory(int complaintId, String oldStatus, String newStatus, Integer changedByUserId, String comments) {
        this.complaintId = complaintId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedByUserId = changedByUserId;
        this.comments = comments;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public Integer getChangedByUserId() { return changedByUserId; }
    public void setChangedByUserId(Integer changedByUserId) { this.changedByUserId = changedByUserId; }

    public String getChangedByName() { return changedByName; }
    public void setChangedByName(String changedByName) { this.changedByName = changedByName; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Timestamp getChangedAt() { return changedAt; }
    public void setChangedAt(Timestamp changedAt) { this.changedAt = changedAt; }
}
