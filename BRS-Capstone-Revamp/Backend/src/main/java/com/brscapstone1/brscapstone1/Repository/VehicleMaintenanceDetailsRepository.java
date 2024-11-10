package com.brscapstone1.brscapstone1.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brscapstone1.brscapstone1.Entity.VehicleEntity;
import com.brscapstone1.brscapstone1.Entity.VehicleMaintenanceDetailsEntity;

@Repository
public interface VehicleMaintenanceDetailsRepository extends JpaRepository<VehicleMaintenanceDetailsEntity, Integer> {
    
    Optional<VehicleMaintenanceDetailsEntity> findByVehicle(VehicleEntity vehicle); 
    List<VehicleMaintenanceDetailsEntity> findAll();
}
