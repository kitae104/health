package kitae.spring.health.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import kitae.spring.health.enums.NotificationType;
import kitae.spring.health.users.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;    // 아이디

    private String subject; // 제목

    @NotBlank(message = "Recipient is required")
    private String recipient;   // 수신자

    private String message; // 메시지 내용

    private NotificationType type; // 알림 유형

    private LocalDateTime createdAt; // 생성 일시

    private String templateName; // 템플릿 이름

    private Map<String , Object> templateVariables; // 템플릿 변수
}
