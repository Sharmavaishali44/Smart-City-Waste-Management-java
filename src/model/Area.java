package model;

import java.sql.Timestamp;

public class Area {
    private int id;
    private String name;
    private String city;
    private int population;
    private boolean sensitive;
    private Timestamp createdAt;

    public Area() {}

    public Area(int id, String name, String city, int population, boolean sensitive) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.population = population;
        this.sensitive = sensitive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public int getPopulation() { return population; }
    public void setPopulation(int population) { this.population = population; }

    public boolean isSensitive() { return sensitive; }
    public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name;
    }
}
