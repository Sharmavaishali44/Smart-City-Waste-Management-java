package model;

import java.sql.Timestamp;

public class Notification {
    private int id;
    private Integer userId;
    private String roleTarget;
    private String title;
    private String message;
    private boolean read;
    private Timestamp createdAt;

    public Notification() {}

    public Notification(Integer userId, String roleTarget, String title, String message) {
        this.userId = userId;
        this.roleTarget = roleTarget;
        this.title = title;
        this.message = message;
        this.read = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getRoleTarget() { return roleTarget; }
    public void setRoleTarget(String roleTarget) { this.roleTarget = roleTarget; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
