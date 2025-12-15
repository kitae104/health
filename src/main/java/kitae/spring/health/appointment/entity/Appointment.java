package kitae.spring.health.appointment.entity;

import jakarta.persistence.*;
import kitae.spring.health.consultation.entity.Consultation;
import kitae.spring.health.doctor.entity.Doctor;
import kitae.spring.health.enums.AppointmentStatus;
import kitae.spring.health.patient.entity.Patient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 아이디

    @Column(nullable = false)
    private LocalDateTime startTime; // 시작 시간
    private LocalDateTime endTime; // 종료 시간
    private String meetingLink; // 화상 회의 링크

    private String purposeOfConsultation; // 상담 목적

    private String initialSymptoms; // 초기 증상

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;   // 예약 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor; // 의사

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient; // 환자

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Consultation consultation;  // 상담 기록
}