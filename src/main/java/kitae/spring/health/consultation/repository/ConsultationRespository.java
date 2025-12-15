package kitae.spring.health.consultation.repository;

import kitae.spring.health.consultation.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultationRespository extends JpaRepository<Consultation, Integer> {

    Optional<Consultation> findByAppointmentId(Long appointmentId);

    List<Consultation> findByAppointmentPatientIdOrderByConsultationDateDesc(Long patientId);

}
