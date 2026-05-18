package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class responsible for handling business logic
 * related to Appointment operations such as booking,
 * updating, cancelling, and retrieving appointments.
 */
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    /**
     * Constructor for dependency injection
     */
    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Books a new appointment and saves it to the database
     * @param appointment Appointment object containing details
     * @return 1 if successful, 0 if failed
     */
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Updates an existing appointment if the patient is authorized
     * Also checks for doctor time conflicts
     *
     * @param appointmentId ID of the appointment to update
     * @param updatedAppointment New appointment details
     * @param patientId ID of the patient requesting update
     * @return status message
     */
    @Transactional
    public String updateAppointment(Long appointmentId, Appointment updatedAppointment, Long patientId) {
        Optional<Appointment> optional = appointmentRepository.findById(appointmentId);
        if (optional.isEmpty()) return "Appointment not found";

        Appointment existing = optional.get();

        if (!existing.getPatient().getId().equals(patientId)) {
            return "Unauthorized access";
        }

        LocalDateTime newTime = updatedAppointment.getAppointmentTime();
        Long doctorId = updatedAppointment.getDoctor().getId();

        // Check for time conflicts (within ±59 minutes)
        List<Appointment> conflicts = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                doctorId,
                newTime.minusMinutes(59),
                newTime.plusMinutes(59)
        );

        if (!conflicts.isEmpty()) return "Doctor is not available at the selected time";

        // Update appointment details
        existing.setDoctor(updatedAppointment.getDoctor());
        existing.setAppointmentTime(updatedAppointment.getAppointmentTime());
        existing.setStatus(updatedAppointment.getStatus());

        appointmentRepository.save(existing);
        return "Appointment updated successfully";
    }

    /**
     * Cancels an appointment if the patient is authorized
     *
     * @param appointmentId ID of the appointment
     * @param patientId ID of the patient requesting cancellation
     * @return status message
     */
    @Transactional
    public String cancelAppointment(Long appointmentId, Long patientId) {
        Optional<Appointment> optional = appointmentRepository.findById(appointmentId);
        if (optional.isEmpty()) return "Appointment not found";

        Appointment appointment = optional.get();

        if (!appointment.getPatient().getId().equals(patientId)) {
            return "Unauthorized cancellation";
        }

        appointmentRepository.delete(appointment);
        return "Appointment canceled successfully";
    }

    /**
     * Retrieves all appointments for a specific doctor on a given date
     * Optionally filters by patient name
     *
     * @param doctorId ID of the doctor
     * @param date Date of appointments
     * @param patientName Optional patient name filter
     * @return list of appointments
     */
    @Transactional
    public List<Appointment> getAppointmentsForDoctorOnDate(Long doctorId, LocalDate date, String patientName) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        if (patientName != null && !patientName.isEmpty()) {
            return appointmentRepository
                    .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                            doctorId, patientName, start, end
                    );
        } else {
            return appointmentRepository
                    .findByDoctorIdAndAppointmentTimeBetween(
                            doctorId, start, end
                    );
        }
    }

    /**
     * Updates the status of an appointment
     *
     * @param appointmentId ID of the appointment
     * @param status New status (0 = Scheduled, 1 = Completed)
     */
    @Transactional
    public void changeAppointmentStatus(Long appointmentId, int status) {
        appointmentRepository.updateStatus(status, appointmentId);
    }
}
