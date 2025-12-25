package kitae.spring.health.appointment.repository;

import kitae.spring.health.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // 의사 ID로 예약 목록을 내림차순으로 반환하는 메서드
    List<Appointment> findByDoctor_User_IdOrderByIdDesc(Long userId);

    // 환자 ID로 예약 목록을 내림차순으로 반환하는 메서드
    List<Appointment> findByPatient_User_IdOrderByIdDesc(Long userId);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.status = 'SCHEDULED' " +
            "AND (" +
            "   (a.startTime < :newEndTime AND a.endTime > :newStartTime)" +
            ")")
    List<Appointment> findConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime
    );

}
