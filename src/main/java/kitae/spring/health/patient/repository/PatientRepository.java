package kitae.spring.health.patient.repository;

import kitae.spring.health.patient.entity.Patient;
import kitae.spring.health.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByUser(User user);
}
