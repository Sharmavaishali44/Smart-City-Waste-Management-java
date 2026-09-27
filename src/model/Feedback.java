package model;

import java.sql.Timestamp;

public class Feedback {
    private int id;
    private int complaintId;
    private Integer citizenUserId;
    private String citizenName;
    private int rating;
    private String comments;
    private Timestamp createdAt;

    public Feedback() {}

    public Feedback(int complaintId, Integer citizenUserId, int rating, String comments) {
        this.complaintId = complaintId;
        this.citizenUserId = citizenUserId;
        this.rating = rating;
        this.comments = comments;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public Integer getCitizenUserId() { return citizenUserId; }
    public void setCitizenUserId(Integer citizenUserId) { this.citizenUserId = citizenUserId; }

    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
