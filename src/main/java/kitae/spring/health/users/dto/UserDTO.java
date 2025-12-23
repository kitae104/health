package kitae.spring.health.users.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kitae.spring.health.role.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@JsonIgnoreProperties(ignoreUnknown = true) // JSON에 존재하지만 Java 클래스에는 없는 필드는 무시
@JsonInclude(JsonInclude.Include.NON_NULL) // 값이 null인 필드는 JSON으로 변환할 때 포함하지 않음
public class UserDTO {

    private Long id;

    private String name;

    private String email;

    private String profilePictureUrl;

    @JsonIgnore // 비밀번호는 JSON 직렬화/역직렬화에서 무시
    private String password;

    private List<Role> roles;
}
