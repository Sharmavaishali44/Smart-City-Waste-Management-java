package model;

import java.sql.Timestamp;

public class Ward {
    private int id;
    private String wardNumber;
    private String wardName;
    private int areaId;
    private String areaName;
    private Timestamp createdAt;

    public Ward() {}

    public Ward(int id, String wardNumber, String wardName, int areaId) {
        this.id = id;
        this.wardNumber = wardNumber;
        this.wardName = wardName;
        this.areaId = areaId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getWardNumber() { return wardNumber; }
    public void setWardNumber(String wardNumber) { this.wardNumber = wardNumber; }

    public String getWardName() { return wardName; }
    public void setWardName(String wardName) { this.wardName = wardName; }

    public int getAreaId() { return areaId; }
    public void setAreaId(int areaId) { this.areaId = areaId; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return wardNumber + " - " + wardName;
    }
}
