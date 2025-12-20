package kitae.spring.health.notification.service;

import jakarta.persistence.EntityNotFoundException;
import kitae.spring.health.notification.dto.NotificationDTO;
import kitae.spring.health.notification.repository.NotificationRepository;
import kitae.spring.health.users.entity.User;
import kitae.spring.health.users.repository.UserRepository;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;


    @Test
    void sendEmail() {

        User user = userRepository.findByEmail("test@test.com")
                .orElseThrow(EntityNotFoundException::new);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient("aqua0405.kitae@gmail.com")
                .subject("Test Email")
                .message("This is a test email.")
                .build();

        notificationService.sendEmail(notificationDTO, user);
    }
}