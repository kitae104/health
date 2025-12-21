package kitae.spring.health.users.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import kitae.spring.health.enums.Specialization;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistrationRequest {

    @NotBlank(message = "이름은 필수 항목 입니다.")
    private String name;    // 이름

    private Specialization specialization; // 전문 분야(의사인 경우)

    private String licenseNumber; // 면허 번호(의사인 경우)

    @NotBlank(message = "이메일은 필수 항목 입니다.")
    @Email
    private String email;   // 이메일

    private List<String> roles; // 역할 목록

    @NotBlank(message = "비밀번호는 필수 항목 입니다.")
    private String password; // 비밀번호
}
