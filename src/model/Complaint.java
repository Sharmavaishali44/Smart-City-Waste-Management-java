package model;

import java.sql.Timestamp;
import model.Enums.ComplaintCategory;
import model.Enums.ComplaintStatus;
import model.Enums.PriorityLevel;

public class Complaint {
    private int id;
    private String complaintCode;
    private Integer citizenUserId;
    private String citizenName;
    private String phone;
    private String email;
    private int areaId;
    private String areaName;
    private int wardId;
    private String wardNumber;
    private String wardName;
    private String locality;
    private String address;
    private String category;
    private String description;
    private String imagePath;
    private ComplaintStatus status;
    private PriorityLevel priority;
    private int priorityScore;
    private String priorityReason;
    private boolean healthRisk;
    private boolean garbageOverflow;
    private boolean sensitiveArea;
    private Integer assignedCrewId;
    private String assignedCrewName;
    private String crewPhone;
    private String crewVehicleNumber;
    private Timestamp expectedResolutionTime;
    private String resolutionNotes;
    private String resolutionImagePath;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp resolvedAt;

    public Complaint() {
        this.status = ComplaintStatus.NEW;
        this.priority = PriorityLevel.MEDIUM;
        this.priorityScore = 50;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getComplaintCode() { return complaintCode; }
    public void setComplaintCode(String complaintCode) { this.complaintCode = complaintCode; }

    public Integer getCitizenUserId() { return citizenUserId; }
    public void setCitizenUserId(Integer citizenUserId) { this.citizenUserId = citizenUserId; }

    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAreaId() { return areaId; }
    public void setAreaId(int areaId) { this.areaId = areaId; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public int getWardId() { return wardId; }
    public void setWardId(int wardId) { this.wardId = wardId; }

    public String getWardNumber() { return wardNumber; }
    public void setWardNumber(String wardNumber) { this.wardNumber = wardNumber; }

    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public ComplaintCategory getCategoryEnum() {
        return ComplaintCategory.fromDisplayName(category);
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { this.status = status; }

    public PriorityLevel getPriority() { return priority; }
    public void setPriority(PriorityLevel priority) { this.priority = priority; }

    public int getPriorityScore() { return priorityScore; }
    public void setPriorityScore(int priorityScore) { this.priorityScore = priorityScore; }

    public String getPriorityReason() { return priorityReason; }
    public void setPriorityReason(String priorityReason) { this.priorityReason = priorityReason; }

    public boolean isHealthRisk() { return healthRisk; }
    public void setHealthRisk(boolean healthRisk) { this.healthRisk = healthRisk; }

    public boolean isGarbageOverflow() { return garbageOverflow; }
    public void setGarbageOverflow(boolean garbageOverflow) { this.garbageOverflow = garbageOverflow; }

    public boolean isSensitiveArea() { return sensitiveArea; }
    public void setSensitiveArea(boolean sensitiveArea) { this.sensitiveArea = sensitiveArea; }

    public Integer getAssignedCrewId() { return assignedCrewId; }
    public void setAssignedCrewId(Integer assignedCrewId) { this.assignedCrewId = assignedCrewId; }

    public String getAssignedCrewName() { return assignedCrewName; }
    public void setAssignedCrewName(String assignedCrewName) { this.assignedCrewName = assignedCrewName; }

    public String getCrewPhone() { return crewPhone; }
    public void setCrewPhone(String crewPhone) { this.crewPhone = crewPhone; }

    public String getCrewVehicleNumber() { return crewVehicleNumber; }
    public void setCrewVehicleNumber(String crewVehicleNumber) { this.crewVehicleNumber = crewVehicleNumber; }

    public Timestamp getExpectedResolutionTime() { return expectedResolutionTime; }
    public void setExpectedResolutionTime(Timestamp expectedResolutionTime) { this.expectedResolutionTime = expectedResolutionTime; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getResolutionImagePath() { return resolutionImagePath; }
    public void setResolutionImagePath(String resolutionImagePath) { this.resolutionImagePath = resolutionImagePath; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }
}
