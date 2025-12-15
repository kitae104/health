package kitae.spring.health.notification.entity;

import jakarta.persistence.*;
import kitae.spring.health.audit.BaseTimeEntity;
import kitae.spring.health.enums.NotificationType;
import kitae.spring.health.users.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 아이디

    private String subject; // 제목

    private String recipient;   // 수신자

    private String message; // 메시지 내용

    @Enumerated(EnumType.STRING)
    private NotificationType type; // EMAIL, SMS, PUSH

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // 관련 사용자

}
