package model;

import java.sql.Timestamp;
import model.Enums.CrewAvailability;

public class Crew {
    private int id;
    private String crewName;
    private String leaderName;
    private int membersCount;
    private String membersDescription;
    private String phone;
    private int areaId;
    private String areaName;
    private int wardId;
    private String wardNumber;
    private String wardName;
    private String wardCoverage;
    private String vehicleNumber;
    private double vehicleCapacity; // in metric tons
    private int currentWorkload;
    private CrewAvailability availability;
    private String status;
    private Integer userId;
    private Timestamp createdAt;

    public Crew() {
        this.membersCount = 4;
        this.membersDescription = "4 Field Sanitation Staff";
        this.vehicleCapacity = 3.50;
        this.availability = CrewAvailability.AVAILABLE;
        this.status = "ACTIVE";
    }

    public Crew(int id, String crewName, String leaderName, String phone, int areaId, int wardId, String vehicleNumber) {
        this.id = id;
        this.crewName = crewName;
        this.leaderName = leaderName;
        this.phone = phone;
        this.areaId = areaId;
        this.wardId = wardId;
        this.vehicleNumber = vehicleNumber;
        this.membersCount = 4;
        this.membersDescription = "4 Field Sanitation Staff";
        this.vehicleCapacity = 3.50;
        this.wardCoverage = "All Wards in Zone";
        this.currentWorkload = 0;
        this.availability = CrewAvailability.AVAILABLE;
        this.status = "ACTIVE";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCrewName() { return crewName; }
    public void setCrewName(String crewName) { this.crewName = crewName; }

    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }

    public int getMembersCount() { return membersCount; }
    public void setMembersCount(int membersCount) { this.membersCount = membersCount; }

    public String getMembersDescription() { return membersDescription; }
    public void setMembersDescription(String membersDescription) { this.membersDescription = membersDescription; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

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

    public String getWardCoverage() { return wardCoverage; }
    public void setWardCoverage(String wardCoverage) { this.wardCoverage = wardCoverage; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public double getVehicleCapacity() { return vehicleCapacity; }
    public void setVehicleCapacity(double vehicleCapacity) { this.vehicleCapacity = vehicleCapacity; }

    public int getCurrentWorkload() { return currentWorkload; }
    public void setCurrentWorkload(int currentWorkload) { this.currentWorkload = currentWorkload; }

    public CrewAvailability getAvailability() { return availability; }
    public void setAvailability(CrewAvailability availability) { this.availability = availability; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return crewName + " [" + vehicleNumber + "] (" + vehicleCapacity + "T, Workload: " + currentWorkload + ")";
    }
}
