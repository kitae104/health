package kitae.spring.health.doctor.repository;

import kitae.spring.health.doctor.entity.Doctor;
import kitae.spring.health.enums.Specialization;
import kitae.spring.health.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor,Long> {

    Optional<Doctor> findByUser(User user);

    // 특정 분야의 의사 목록을 반환하는 메서드
    List<Doctor> findBySpecialization(Specialization specialization);
}
