package kitae.spring.health.notification.repository;

import kitae.spring.health.notification.dto.NotificationDTO;
import kitae.spring.health.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
