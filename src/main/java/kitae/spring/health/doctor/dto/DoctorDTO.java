package kitae.spring.health.doctor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kitae.spring.health.enums.Specialization;
import kitae.spring.health.users.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DoctorDTO {

    private Long id; // 아이디

    private String firstName;   // 이름
    private String lastName;    // 성

    private Specialization specialization;  // 전문 분야

    private String licenseNumber; // 면허 번호

    private UserDTO user; // 연관된 사용자 정보

}