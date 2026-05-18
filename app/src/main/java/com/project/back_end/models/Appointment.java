package com.project.back_end.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity class representing an Appointment in the Smart Clinic Management System.
 * Maps to the "appointments" table in the database.
 */
@Entity
@Table(name = "appointments")
public class Appointment {

    /**
     * Unique identifier for each appointment (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Doctor associated with the appointment
     * Many appointments can belong to one doctor
     */
    @NotNull(message = "Doctor must be assigned")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    /**
     * Patient associated with the appointment
     * Many appointments can belong to one patient
     */
    @NotNull(message = "Patient must be assigned")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    /**
     * Date and time of the appointment
     * Must be a future date-time value
     */
    @NotNull(message = "Appointment time must be provided")
    @Future(message = "Appointment time must be in the future")
    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    /**
     * Status of appointment
     * 0 = Scheduled, 1 = Completed
     */
    @NotNull(message = "Status is required")
    @Column(nullable = false)
    private Integer status;

    /**
     * Default constructor required by JPA
     */
    public Appointment() {
    }

    /**
     * Parameterized constructor to initialize appointment details
     */
    public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, Integer status) {
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    /**
     * Calculates end time of appointment (not stored in DB)
     * Assumes each appointment lasts 1 hour
     * @return appointment end time
     */
    @Transient
    public LocalDateTime getEndTime() {
        return appointmentTime.plusHours(1);
    }

    /**
     * Extracts only the date from appointment time
     * @return appointment date
     */
    @Transient
    public LocalDate getAppointmentDate() {
        return appointmentTime.toLocalDate();
    }

    /**
     * Extracts only the time from appointment time
     * @return appointment time (HH:MM)
     */
    @Transient
    public LocalTime getAppointmentTimeOnly() {
        return appointmentTime.toLocalTime();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return doctor assigned to appointment
     */
    public Doctor getDoctor() {
        return doctor;
    }

    /**
     * @param doctor sets doctor for appointment
     */
    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    /**
     * @return patient assigned to appointment
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * @param patient sets patient for appointment
     */
    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    /**
     * @return appointment date and time
     */
    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    /**
     * @param appointmentTime sets appointment date and time
     */
    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    /**
     * @return appointment status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status sets appointment status
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
}
