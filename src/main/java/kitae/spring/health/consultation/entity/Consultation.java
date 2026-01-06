package kitae.spring.health.consultation.entity;

import jakarta.persistence.*;
import kitae.spring.health.appointment.entity.Appointment;
import kitae.spring.health.audit.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "consultations")
public class Consultation extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 아이디

    private LocalDateTime consultationDate; // 상담 날짜

    @Lob
    private String subjectiveNotes; // 주관적 소견

    @Lob
    private String objectiveFindings; // 객관적 소견

    @Lob
    private String assessment; // 평가

    @Lob
    private String plan; // 계획

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true, nullable = false)
    private Appointment appointment;    // 진료 예약
}