package kitae.spring.health.users.service;

import kitae.spring.health.users.repository.PasswordResetCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class CodeGenerator {

    private final PasswordResetCodeRepository passwordResetCodeRepository;

    private static  final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 5;

    /**
     * 고유한 코드 생성
     * @return
     */
    public String generateUniqueCode() {
        String code;
        do {
            code = generateRandomCode();

        } while (passwordResetCodeRepository.findByCode(code).isPresent());

        return code;
    }

    /**
     * 랜덤 코드 생성
     * @return
     */
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(ALPHA_NUMERIC.length());
            sb.append(ALPHA_NUMERIC.charAt(index));
        }
        return sb.toString();
    }
}
