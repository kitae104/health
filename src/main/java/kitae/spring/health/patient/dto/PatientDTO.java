package kitae.spring.health.patient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kitae.spring.health.enums.BloodGroup;
import kitae.spring.health.enums.Genotype;
import kitae.spring.health.users.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientDTO {

    private Long id; // 환자 ID

    private String firstName; // 이름

    private String lastName;    // 성

    private LocalDate dateOfBirth; // 생년월일

    private String phone; // 전화번호

    private String knownAllergies; // 알려진 알레르기

    private BloodGroup bloodGroup; // 혈액형

    private Genotype genotype; // 유전자형

    private UserDTO user; // 연관된 사용자 정보
}
