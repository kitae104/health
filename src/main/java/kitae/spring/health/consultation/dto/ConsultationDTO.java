package kitae.spring.health.consultation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ConsultationDTO {

    private Long id;    // 상담 ID

    private Long appointmentId; // 연관된 예약 ID

    private LocalDateTime consultationDate; // 상담 날짜 및 시간

    private String subjectiveNotes; // 주관적 소견

    private String objectiveFindings; // 객관적 소견

    private String assessment; // 평가

    private String plan; // 계획
}
