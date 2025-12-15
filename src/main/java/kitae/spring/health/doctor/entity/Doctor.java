package kitae.spring.health.doctor.entity;

import jakarta.persistence.*;
import kitae.spring.health.appointment.entity.Appointment;
import kitae.spring.health.enums.Specialization;
import kitae.spring.health.users.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "doctors")
public class Doctor {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 아이디

    private String firstName;   // 이름
    private String lastName;    // 성

    @Enumerated(EnumType.STRING)
    private Specialization specialization;  // 전문 분야

    private String licenseNumber; // 면허 번호

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user; // 사용자 정보

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments; // 진료 예약 목록
}
