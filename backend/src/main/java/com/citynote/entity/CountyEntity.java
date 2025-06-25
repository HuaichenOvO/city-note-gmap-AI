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

    @Column(nullable = false, updatable = false)
    private String countyName;

    @Column(nullable = false, updatable = false)
    private String countyState;

    @Column(nullable = false, updatable = false, unique = true)
    private String countyKey;
}
