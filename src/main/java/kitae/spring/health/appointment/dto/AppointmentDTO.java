package kitae.spring.health.appointment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import kitae.spring.health.doctor.dto.DoctorDTO;
import kitae.spring.health.enums.AppointmentStatus;
import kitae.spring.health.enums.Specialization;
import kitae.spring.health.patient.dto.PatientDTO;
import kitae.spring.health.users.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppointmentDTO {

    private Long id; // 아이디

    @NotNull(message = "Doctor ID is required for booking.")
    private Long doctorId; // 의사 ID

    private String purposeOfConsultation;   // 상담 목적

    private String initialSymptoms; // 초기 증상 설명

    @NotNull(message = "Start time is required for the appointment.")
    @Future(message = "Appointment must be scheduled for a future date and time.")
    private LocalDateTime startTime;    // 예약 시작 시간

    private LocalDateTime endTime;  // 예약 종료 시간

    private String meetingLink; // 온라인 미팅 링크

    private AppointmentStatus status; // 예약 상태

    private DoctorDTO doctor; // 의사 정보

    private PatientDTO patient; // 환자 정보
}
