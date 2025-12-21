package kitae.spring.health.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "이메일은 필수 항목 입니다.")
    @Email
    private String email;

    @NotBlank(message = "비밀번호는 필수 항목 입니다.")
    private String password;
}