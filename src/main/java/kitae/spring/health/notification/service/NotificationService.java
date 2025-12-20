package kitae.spring.health.notification.service;

import jakarta.mail.internet.MimeMessage;
import kitae.spring.health.enums.NotificationType;
import kitae.spring.health.notification.dto.NotificationDTO;
import kitae.spring.health.notification.entity.Notification;
import kitae.spring.health.notification.repository.NotificationRepository;
import kitae.spring.health.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendEmail(NotificationDTO notificationDTO, User user) {
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();   // 메일 메시지 객체 생성
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, // 메시지 도우미 생성
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, // 멀티파트 모드 설정
                    StandardCharsets.UTF_8.name()); // 문자 인코딩 설정

            helper.setTo(notificationDTO.getRecipient()); // 수신자 설정
            helper.setSubject(notificationDTO.getSubject()); // 제목 설정

            // 템플릿 처리
            if(notificationDTO.getTemplateName() != null){
                Context context = new Context(); // 템플릿 컨텍스트 생성(Thymeleaf)
                context.setVariables(notificationDTO.getTemplateVariables()); // 변수 설정
                String htmlContent = templateEngine.process(notificationDTO.getTemplateName(), context); // 템플릿 처리
                helper.setText(htmlContent, true); // HTML 콘텐츠 설정
            } else {
                helper.setText(notificationDTO.getMessage(), true); // 일반 메시지 설정
            }

            mailSender.send(mimeMessage);   // 메일 전송
            log.info("이메일을 {}에게 전송했습니다.", notificationDTO.getRecipient());

            // 전송 정보 DB 저장
            Notification notification = Notification.builder()
                    .subject(notificationDTO.getSubject())
                    .recipient(notificationDTO.getRecipient())
                    .message(notificationDTO.getMessage())
                    .type(NotificationType.EMAIL)
                    .user(user)
                    .build();

            notificationRepository.save(notification);

        } catch(Exception e){
            log.info(e.getMessage());
        }
    }
}
