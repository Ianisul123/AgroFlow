package com.example.agroflowbackend.domain;

// Clasa de domain reprezinta o zona de irigare folosita in logica aplicatiei.
public class IrrigationZone {

    private final Long id;
    private final Long farmId;
    private String name;
    private String cropType;
    private Double areaSize;
    private Double moistureThreshold;

    public IrrigationZone(Long id, Long farmId, String name, String cropType, Double areaSize, Double moistureThreshold) {
        this.id = id;
        this.farmId = farmId;
        this.name = name;
        this.cropType = cropType;
        this.areaSize = areaSize;
        this.moistureThreshold = moistureThreshold;
    }

    public IrrigationZone(Long farmId, String name, String cropType, Double areaSize, Double moistureThreshold) {
        this(null, farmId, name, cropType, areaSize, moistureThreshold);
    }

    public Long getId() {
        return id;
    }

    public Long getFarmId() {
        return farmId;
    }

    public String getName() {
        return name;
    }

    public String getCropType() {
        return cropType;
    }

    public Double getAreaSize() {
        return areaSize;
    }

    public Double getMoistureThreshold() {
        return moistureThreshold;
    }
}
