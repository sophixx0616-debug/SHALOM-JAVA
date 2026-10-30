package com.shalom.repository;

import com.shalom.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserIdOrderByDateDescTimeDesc(Long userId);

    List<Appointment> findAllByOrderByDateDescTimeDesc();

    boolean existsByDateAndTime(LocalDate date, LocalTime time);

    boolean existsByDateAndTimeAndIdNot(
            LocalDate date,
            LocalTime time,
            Long id
    );

    boolean existsByUserId(Long userId);

    boolean existsByServiceId(Long serviceId);

    /*
     * Busca citas activas que se crucen con el intervalo
     * de una nueva cita.
     *
     * Una cita se considera ocupada si:
     *
     * nuevaHoraInicio < horaFinExistente
     * Y
     * nuevaHoraFin > horaInicioExistente
     *
     * También permite filtrar por especialista.
     */

    @Query("""
        SELECT a
        FROM Appointment a
        WHERE a.date = :date
          AND LOWER(a.status) <> 'cancelada'
          AND a.time < :endTime
          AND FUNCTION('ADDTIME', a.time, FUNCTION('SEC_TO_TIME', a.service.duration * 60)) > :startTime
    """)
    List<Appointment> findOverlappingAppointments(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /*
     * Consulta para una cita de un especialista específico.
     */
    @Query("""
        SELECT a
        FROM Appointment a
        WHERE a.date = :date
          AND LOWER(a.status) <> 'cancelada'
          AND a.worker = :worker
          AND a.time < :endTime
          AND FUNCTION('ADDTIME', a.time, FUNCTION('SEC_TO_TIME', a.service.duration * 60)) > :startTime
          AND a.id <> :appointmentId
    """)
    List<Appointment> findOverlappingAppointmentsForWorker(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("worker") String worker,
            @Param("appointmentId") Long appointmentId
    );

    @Query("SELECT a.worker, COUNT(a) FROM Appointment a GROUP BY a.worker ORDER BY COUNT(a) DESC")
    List<Object[]> countByWorker();

    @Query("SELECT a.service.id, COUNT(a) FROM Appointment a GROUP BY a.service.id ORDER BY COUNT(a) DESC")
    List<Object[]> countByServiceId();
}