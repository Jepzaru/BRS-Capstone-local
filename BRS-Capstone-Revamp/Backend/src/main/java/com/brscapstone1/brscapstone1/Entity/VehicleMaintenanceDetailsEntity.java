package com.brscapstone1.brscapstone1.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vehicle_maintenance_details")
public class VehicleMaintenanceDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false) 
    private VehicleEntity vehicle;


    private String vehicleType;
    private String maintenanceDetails;
    private LocalDate maintenanceStartDate;
    private LocalDate maintenanceEndDate;

    public VehicleMaintenanceDetailsEntity() {
        super();
    }

    public VehicleMaintenanceDetailsEntity(VehicleEntity vehicle, String vehicleType, String maintenanceDetails, LocalDate maintenanceStartDate, LocalDate maintenanceEndDate) {
        this.vehicle = vehicle;
        this.vehicleType = vehicleType;
        this.maintenanceDetails = maintenanceDetails;
        this.maintenanceStartDate = maintenanceStartDate;
        this.maintenanceEndDate = maintenanceEndDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVehicleType() {
        return vehicleType;
      }
    
      public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
      }

    public VehicleEntity getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleEntity vehicle) {
        this.vehicle = vehicle;
    }

    public String getMaintenanceDetails() {
        return maintenanceDetails;
    }

    public void setMaintenanceDetails(String maintenanceDetails) {
        this.maintenanceDetails = maintenanceDetails;
    }

    public LocalDate getMaintenanceStartDate() {
        return maintenanceStartDate;
    }

    public void setMaintenanceStartDate(LocalDate maintenanceStartDate) {
        this.maintenanceStartDate = maintenanceStartDate;
    }

    public LocalDate getMaintenanceEndDate() {
        return maintenanceEndDate;
    }

    public void setMaintenanceEndDate(LocalDate maintenanceEndDate) {
        this.maintenanceEndDate = maintenanceEndDate;
    }
}
