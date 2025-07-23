package com.citynote.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "counties")
public class CountyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "county_id")
    private int id;

    @Column(name = "county_name", nullable = false, updatable = false)
    private String countyName;

    @Column(name = "county_state", nullable = false, updatable = false)
    private String countyState;

    @Column(nullable = false, updatable = false, unique = true)
    private String countyKey;

    // Manual getters and setters for Lombok compatibility
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getCountyState() {
        return countyState;
    }

    public void setCountyState(String countyState) {
        this.countyState = countyState;
    }

    public String getCountyKey() {
        return countyKey;
    }

    public void setCountyKey(String countyKey) {
        this.countyKey = countyKey;
    }
}
