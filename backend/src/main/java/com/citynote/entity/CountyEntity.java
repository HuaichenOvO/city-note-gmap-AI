package com.citynote.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "counties")
public class CountyEntity {
    @Id
    private int id;
    private String name;
    private String state;
}
