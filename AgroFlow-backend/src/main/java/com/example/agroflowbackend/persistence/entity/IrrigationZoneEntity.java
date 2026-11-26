package com.example.agroflowbackend.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "irrigation_zone")
// Entitatea descrie cum este salvata zona de irigare in baza de date.
public class IrrigationZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mai multe zone pot apartine aceleiasi ferme, dar o zona apartine unei singure ferme.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private FarmEntity farm;

    private String name;
    private String cropType;
    private Double areaSize;
    private Double moistureThreshold;

    public IrrigationZoneEntity() {
        // Required by JPA to create entity instances through reflection.
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FarmEntity getFarm() {
        return farm;
    }

    public void setFarm(FarmEntity farm) {
        this.farm = farm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCropType() {
        return cropType;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public Double getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(Double areaSize) {
        this.areaSize = areaSize;
    }

    public Double getMoistureThreshold() {
        return moistureThreshold;
    }

    public void setMoistureThreshold(Double moistureThreshold) {
        this.moistureThreshold = moistureThreshold;
    }
}
