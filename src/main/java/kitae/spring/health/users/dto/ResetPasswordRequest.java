package kitae.spring.health.users.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResetPasswordRequest {

    private String email;   // 이메일

    private String code; // 인증 코드

    private String newPassword; // 새 비밀번호
}
