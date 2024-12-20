package com.brscapstone1.brscapstone1.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.brscapstone1.brscapstone1.Constants;
import com.brscapstone1.brscapstone1.DTO.ReservedDateDTO;
import com.brscapstone1.brscapstone1.Entity.ReservationEntity;
import com.brscapstone1.brscapstone1.Entity.ReservationVehicleEntity;
import com.brscapstone1.brscapstone1.Entity.VehicleEntity;
import com.brscapstone1.brscapstone1.Repository.ReservationRepository;
import com.brscapstone1.brscapstone1.Repository.ReservationVehicleRepository;
import com.brscapstone1.brscapstone1.Repository.VehicleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReservationService {
    
    @Autowired
    private ReservationRepository resRepo;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ReservationVehicleRepository reservationVehicleRepository;

    public String generateTransactionId() {
    return UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" +
           UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" +
           UUID.randomUUID().toString().substring(0, 6).toUpperCase();
}

    //[POST] approved reservations by HEAD
    public void headApproveReservation(int reservationId) {
        ReservationEntity reservation = resRepo.findById(reservationId).orElseThrow(() -> new IllegalArgumentException(Constants.ExceptionMessage.RESERVATION_NOT_FOUND));
        reservation.setHeadIsApproved(true); 
        resRepo.save(reservation);
    }

    //[POST] approved reservations by OPC
    public void opcApproveReservation(int reservationId, int driverId, String driverName) {
        ReservationEntity reservation = resRepo.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException(Constants.ExceptionMessage.RESERVATION_NOT_FOUND));
    
        // Check if driver name is set
        if (reservation.getDriverName() == null || reservation.getDriverName().isEmpty()) {
            reservation.setDriverName(Constants.Annotation.NO_DRIVER);
        }
    
        // Set the reservation status to "Approved"
        reservation.setStatus(Constants.Annotation.APPROVED);
        reservation.setOpcIsApproved(true);
        reservation.setOpcTimestamp(LocalDateTime.now());
        reservation.setDriverId(driverId);
        reservation.setDriverName(driverName);
    
        // Fetch associated ReservationVehicleEntities
        List<ReservationVehicleEntity> reservationVehicles = reservationVehicleRepository.findByReservation(reservation);
        for (ReservationVehicleEntity vehicle : reservationVehicles) {
            vehicle.setStatus(Constants.Annotation.APPROVED); 
            reservationVehicleRepository.save(vehicle); 
        }
    
        resRepo.save(reservation);
    }
    
    //[POST] approved reservations
    public void assignDriverToAddedVehicles(int reservationId, String plateNumber, int driverId, String driverName) {
        ReservationVehicleEntity vehicle = reservationVehicleRepository
                .findByReservationIdAndPlateNumber(reservationId, plateNumber)
                .orElseThrow(() -> new RuntimeException(Constants.ExceptionMessage.VEHICLE_NOT_FOUND));
    
        vehicle.setDriverId(driverId);
        vehicle.setDriverName(driverName);
    
        reservationVehicleRepository.save(vehicle);
    }
    
    //[isRejected] rejects a reservation and returns boolean output
    public void rejectReservation(int reservationId, String feedback) {
        ReservationEntity reservation = resRepo.findById(reservationId).orElseThrow(() -> new IllegalArgumentException(Constants.ExceptionMessage.RESERVATION_NOT_FOUND));
        reservation.setStatus(Constants.Annotation.REJECTED);
        reservation.setRejected(true); 
        reservation.setFeedback(feedback);
        resRepo.save(reservation);
    }

    public ReservationEntity saveReservation(String userName, ReservationEntity reservation, List<Integer> vehicleIds, String fileUrl) throws IOException {
        if (fileUrl != null && !fileUrl.isEmpty()) {
            reservation.setFileUrl(fileUrl); 
        } else {
            reservation.setFileUrl(Constants.Annotation.NO_FILE);
        }
    
        if (reservation.getStatus() == null || reservation.getStatus().isEmpty()) {
            reservation.setStatus(Constants.Annotation.PENDING);
        }
        if (reservation.getFeedback() == null || reservation.getFeedback().isEmpty()) {
            reservation.setFeedback(Constants.Annotation.NO_FEEDBACK);
        }
        reservation.setUserName(userName);
        reservation.setTransactionId(generateTransactionId());
        reservation.setReservationTimestamp(LocalDateTime.now());    

        ReservationEntity savedReservation = resRepo.save(reservation);
        List<VehicleEntity> vehicles = vehicleRepository.findAllById(vehicleIds);
    
        for (VehicleEntity vehicle : vehicles) {
            ReservationVehicleEntity reservationVehicle = new ReservationVehicleEntity();
            reservationVehicle.setReservation(savedReservation);
            reservationVehicle.setVehicleType(vehicle.getVehicleType());
            reservationVehicle.setPlateNumber(vehicle.getPlateNumber());
            reservationVehicle.setCapacity(vehicle.getCapacity());
    
            // Inherit scheduling details from the reservation
            reservationVehicle.setSchedule(reservation.getSchedule());
            reservationVehicle.setReturnSchedule(reservation.getReturnSchedule());
            reservationVehicle.setPickUpTime(reservation.getPickUpTime());
            reservationVehicle.setDepartureTime(reservation.getDepartureTime());
            reservationVehicle.setStatus(reservation.getStatus());
    
            reservationVehicleRepository.save(reservationVehicle);
        }
    
        return savedReservation;
    }

    //[GET] all Reservations
    public List<ReservationEntity> getAllReservations() {
        return resRepo.findAll();
    }

    //[GET] all Reservations by their ID
    public ReservationEntity getReservationById(int id) {
        return resRepo.findById(id).orElse(null);
    }

    //[GET] all user's reservations
    public List<ReservationEntity> getUserReservations(String userName) {
        return resRepo.findByUserName(userName);
    }

    //[POST] || add assigned driver
    public void updateAssignedDriver(int reservationId, int driverId, String assignedDriverName) {
        ReservationEntity reservation = resRepo.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(Constants.ExceptionMessage.RESERVATION_NOT_FOUND));
        reservation.setDriverId(driverId);
        reservation.setDriverName(assignedDriverName);
        resRepo.save(reservation);
    }

    //[PUT] update a reservation
    public ReservationEntity updateReservation(int reservationId, ReservationEntity updatedReservation, MultipartFile file, boolean isResending) throws IOException {
        ReservationEntity existingReservation = resRepo.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException(Constants.ExceptionMessage.RESERVATION_NOT_FOUND));
    
        if (isResending) {
            existingReservation.setRejected(false);
            existingReservation.setStatus(Constants.Annotation.PENDING);
    
            if (Constants.Annotation.OPC.equals(existingReservation.getRejectedBy())) {
                existingReservation.setOpcIsApproved(false);
            } else if (Constants.Annotation.HEAD.equals(existingReservation.getRejectedBy())) {
                existingReservation.setHeadIsApproved(false);
            }
        } else {
            if (updatedReservation.isRejected() != null && updatedReservation.isRejected()) {
                if (updatedReservation.isOpcIsApproved() != null && !updatedReservation.isOpcIsApproved()) {
                    existingReservation.setRejectedBy(Constants.Annotation.OPC);
                } else if (updatedReservation.isHeadIsApproved() != null && !updatedReservation.isHeadIsApproved()) {
                    existingReservation.setRejectedBy(Constants.Annotation.HEAD);
                }
            } else {
                if (updatedReservation.isOpcIsApproved() != null && updatedReservation.isOpcIsApproved()) {
                    existingReservation.setOpcIsApproved(true);
                    existingReservation.setOpcTimestamp(LocalDateTime.now());
                }
    
                if (updatedReservation.isHeadIsApproved() != null && updatedReservation.isHeadIsApproved()) {
                    existingReservation.setHeadIsApproved(true);
                    existingReservation.setHeadTimestamp(LocalDateTime.now());
                }
            }
    
            updateFields(existingReservation, updatedReservation);
    
            if (updatedReservation.getDriverId() > 0) {
                existingReservation.setDriverId(updatedReservation.getDriverId());
                existingReservation.setDriverName(updatedReservation.getDriverName());
            }
    
            if (updatedReservation.getReservedVehicles() != null) {
                for (ReservationVehicleEntity updatedVehicle : updatedReservation.getReservedVehicles()) {
                    for (ReservationVehicleEntity existingVehicle : existingReservation.getReservedVehicles()) {
                        if (existingVehicle.getId() == updatedVehicle.getId()) {
                            existingVehicle.setDriverId(updatedVehicle.getDriverId());
                            existingVehicle.setDriverName(updatedVehicle.getDriverName());
                        }
                    }
                }
            }
        }
        return resRepo.save(existingReservation);
    }
    
    //Update method to be call in the update reservation
    private void updateFields(ReservationEntity existingReservation, ReservationEntity updatedReservation) {
        if (updatedReservation.getTypeOfTrip() != null) existingReservation.setTypeOfTrip(updatedReservation.getTypeOfTrip());
        if (updatedReservation.getDestinationTo() != null) existingReservation.setDestinationTo(updatedReservation.getDestinationTo());
        if (updatedReservation.getDestinationFrom() != null) existingReservation.setDestinationFrom(updatedReservation.getDestinationFrom());
        if (updatedReservation.getCapacity() > 0) existingReservation.setCapacity(updatedReservation.getCapacity());
        if (updatedReservation.getDepartment() != null) existingReservation.setDepartment(updatedReservation.getDepartment());
        if (updatedReservation.getSchedule() != null) existingReservation.setSchedule(updatedReservation.getSchedule());
        if (updatedReservation.getVehicleType() != null) existingReservation.setVehicleType(updatedReservation.getVehicleType());
        if (updatedReservation.getPickUpTime() != null) existingReservation.setPickUpTime(updatedReservation.getPickUpTime());
        if (updatedReservation.getDepartureTime() != null) existingReservation.setDepartureTime(updatedReservation.getDepartureTime());
        if (updatedReservation.getReason() != null) existingReservation.setReason(updatedReservation.getReason());
        if (updatedReservation.getStatus() != null) existingReservation.setStatus(updatedReservation.getStatus());
        if (updatedReservation.isOpcIsApproved() != null) existingReservation.setOpcIsApproved(updatedReservation.isOpcIsApproved());
        if (updatedReservation.isRejected() != null) existingReservation.setRejected(updatedReservation.isRejected());
        if (updatedReservation.isHeadIsApproved() != null) existingReservation.setHeadIsApproved(updatedReservation.isHeadIsApproved());
        if (updatedReservation.getUserName() != null) existingReservation.setUserName(updatedReservation.getUserName());
        if (updatedReservation.getFeedback() != null) existingReservation.setFeedback(updatedReservation.getFeedback());
    }    

    //[GET] all OPC approved
    public List<ReservationEntity> getOpcApprovedReservation() {
        return resRepo.findByOpcIsApproved(true);
    }

    //[GET] all reservations that is approved by HEAD
    public List<ReservationEntity> getHeadApprovedReservations() {
        return resRepo.findByHeadIsApproved(true);
    }
    
    //fetchinge reserved dates of the multiple availability
    public List<ReservedDateDTO> getAllReservedDatesByPlateNumber(String plateNumber) {
        List<ReservationVehicleEntity> reservedVehicles = reservationVehicleRepository.findByPlateNumber(plateNumber);
        return reservedVehicles.stream()
        .filter(vehicle -> Constants.Annotation.APPROVED.equals(vehicle.getStatus()))
            .map(vehicle -> new ReservedDateDTO(
                vehicle.getSchedule(),
                vehicle.getReturnSchedule(),
                vehicle.getPickUpTime(),
                vehicle.getDepartureTime(),
                vehicle.getReservation().getStatus(),
                vehicle.getPlateNumber()
            ))
            .collect(Collectors.toList());
    }

    //fetchinge reserved dates of reserved vehicle on reservation
    public List<ReservedDateDTO> getAllReservationDatesByPlateNumber(String plateNumber) {
        List<ReservationEntity> reservations = resRepo.findByPlateNumber(plateNumber);
        return reservations.stream()
            .filter(res -> Constants.Annotation.APPROVED.equals(res.getStatus()))
            .map(res -> new ReservedDateDTO(
                res.getSchedule(),
                res.getReturnSchedule(),
                res.getPickUpTime(),
                res.getDepartureTime(),
                res.getStatus(),
                res.getPlateNumber()
            ))
            .collect(Collectors.toList());
    }

    //fetching time of the main vehicle
    public List<ReservedDateDTO> getReservationsByPlateAndDate(String plateNumber, LocalDate date) {
        List<ReservationEntity> reservations = resRepo.findByPlateNumberAndDate(plateNumber, date);
        return reservations.stream()
            .filter(res -> Constants.Annotation.APPROVED.equals(res.getStatus()))
            .map(res -> new ReservedDateDTO(
                res.getSchedule(),
                res.getReturnSchedule(),
                res.getPickUpTime(),
                res.getDepartureTime(),
                res.getStatus(),
                res.getPlateNumber()
            ))
            .collect(Collectors.toList());
    }

    public List<ReservedDateDTO> getReservedByPlateAndDate(String plateNumber, LocalDate date) {
        List<ReservationVehicleEntity> reservedVehicles = reservationVehicleRepository.findByPlateNumberAndSchedule(plateNumber, date);
        return reservedVehicles.stream()
            .filter(vehicle -> Constants.Annotation.APPROVED.equals(vehicle.getStatus()) && vehicle.getSchedule().equals(date))
            .map(vehicle -> new ReservedDateDTO(
                vehicle.getSchedule(),
                vehicle.getReturnSchedule(),
                vehicle.getPickUpTime(),
                vehicle.getDepartureTime(),
                vehicle.getReservation().getStatus(),
                vehicle.getPlateNumber()
            ))
            .collect(Collectors.toList());
    }

    //[DELETE] a reservation
    public String delete(int id){
        String msg = "";

        if(resRepo.findById(id).isPresent()){
            resRepo.deleteById(id);
            
            msg = Constants.ResponseMessages.RESERVATION_DELETE_SUCCESS;
        }else{
            msg = Constants.ResponseMessages.RESERVATION_NOT_EXISTS;
        }
        return msg;
    }

    //Fetch the platenumbers of the multiple vehicles being reserved
    public List<ReservedDateDTO> getPlateNumbersByScheduleOrReturnSchedule(LocalDate schedule, LocalDate returnSchedule) {
        List<ReservationVehicleEntity> reservedVehicles = reservationVehicleRepository.findByScheduleOrReturnSchedule(schedule, returnSchedule);
    
        return reservedVehicles.stream()
            .map(vehicle -> new ReservedDateDTO(
                vehicle.getSchedule(),
                vehicle.getReturnSchedule(),
                vehicle.getPickUpTime(),
                vehicle.getDepartureTime(),
                vehicle.getReservation().getStatus(), 
                vehicle.getPlateNumber()
            ))
            .collect(Collectors.toList());
    }
    
    public List<ReservedDateDTO> getMainPlateNumbersByScheduleOrReturnSchedule(LocalDate schedule, LocalDate returnSchedule) {
        List<ReservationEntity> reservations = resRepo.findByMainScheduleOrReturnSchedule(schedule, returnSchedule); 
        
        return reservations.stream()
            .map(reservation -> new ReservedDateDTO(
                reservation.getSchedule(),
                reservation.getReturnSchedule(),
                reservation.getPickUpTime(),
                reservation.getDepartureTime(),
                reservation.getStatus(), 
                reservation.getPlateNumber()
            ))
            .collect(Collectors.toList());
    } 

    // Resend request and update reservation status
    public ReservationEntity resendReservationStatus(int reservationId, ReservationEntity updatedReservation, String fileUrl) {
        ReservationEntity existingReservation = resRepo.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException(Constants.ExceptionMessage.RESERVATION_NOT_FOUND));

        // Update fields as needed
        existingReservation.setTypeOfTrip(updatedReservation.getTypeOfTrip());
        existingReservation.setDestinationFrom(updatedReservation.getDestinationFrom());
        existingReservation.setDestinationTo(updatedReservation.getDestinationTo());
        existingReservation.setCapacity(updatedReservation.getCapacity());
        existingReservation.setVehicleType(updatedReservation.getVehicleType());
        existingReservation.setPlateNumber(updatedReservation.getPlateNumber());
        existingReservation.setSchedule(updatedReservation.getSchedule());
        existingReservation.setReturnSchedule(updatedReservation.getReturnSchedule());
        existingReservation.setDepartureTime(updatedReservation.getDepartureTime());
        existingReservation.setPickUpTime(updatedReservation.getPickUpTime());
        existingReservation.setDepartment(updatedReservation.getDepartment());
        existingReservation.setReason(updatedReservation.getReason());

        // Update file URL (use the URL from the request if provided)
        if (fileUrl != null && !fileUrl.isEmpty()) {
            existingReservation.setFileUrl(fileUrl);
        } else if (updatedReservation.getFileUrl() != null && !updatedReservation.getFileUrl().isEmpty()) {
            existingReservation.setFileUrl(updatedReservation.getFileUrl());
        }

        // Reset rejection status if applicable
        resetRejectionStatus(existingReservation);

        // Set rejection status if applicable
        setRejectionStatus(existingReservation, updatedReservation);

        // Set status to Pending and reset rejection
        existingReservation.setStatus(Constants.Annotation.PENDING);
        existingReservation.setRejected(false);

        // Save and return the updated reservation
        return resRepo.save(existingReservation);
    }
    
    // Existing methods remain unchanged
    private void resetRejectionStatus(ReservationEntity reservation) {
        if (Constants.Annotation.OPC.equals(reservation.getRejectedBy())) {
            reservation.setOpcIsApproved(false);
            reservation.setRejectedBy(null);
        } else if (Constants.Annotation.HEAD.equals(reservation.getRejectedBy())) {
            reservation.setHeadIsApproved(false);
            reservation.setRejectedBy(null);
        }
    }
    
    private void setRejectionStatus(ReservationEntity existingReservation, ReservationEntity updatedReservation) {
        if (updatedReservation.isOpcIsApproved() != null && !updatedReservation.isOpcIsApproved()) {
            existingReservation.setRejectedBy(Constants.Annotation.OPC);
        } else if (updatedReservation.isHeadIsApproved() != null && !updatedReservation.isHeadIsApproved()) {
            existingReservation.setRejectedBy(Constants.Annotation.HEAD);
        }
    }

    public ReservationEntity completeReservation(int reservationId) {
        ReservationEntity reservation = resRepo.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException(Constants.ExceptionMessage.RESERVATION_NOT_FOUND));

        reservation.setStatus(Constants.Annotation.COMPLETED);
        reservation.setIsCompleted(true);

        if (reservation.getReservedVehicles() != null) {
            for (ReservationVehicleEntity vehicle : reservation.getReservedVehicles()) {
                vehicle.setStatus(Constants.Annotation.COMPLETED);
                vehicle.setIsCompleted(true); 
                reservationVehicleRepository.save(vehicle); 
            }
        }

        return resRepo.save(reservation);
    }

    public ReservationEntity cancelReservation(int reservationId) {
        ReservationEntity reservation = resRepo.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException(Constants.ExceptionMessage.RESERVATION_NOT_FOUND));

        reservation.setStatus(Constants.Annotation.CANCELED);
        reservation.setIsCanceled(true);

        if (reservation.getReservedVehicles() != null) {
            for (ReservationVehicleEntity vehicle : reservation.getReservedVehicles()) {
                vehicle.setStatus(Constants.Annotation.CANCELED);
                vehicle.setIsCanceled(true); 
                reservationVehicleRepository.save(vehicle); 
            }
        }

        return resRepo.save(reservation);
    }
}