package kitae.spring.health.users.service;

import kitae.spring.health.doctor.entity.Doctor;
import kitae.spring.health.doctor.repository.DoctorRepository;
import kitae.spring.health.exceptions.BadRequestException;
import kitae.spring.health.exceptions.NotFoundException;
import kitae.spring.health.notification.dto.NotificationDTO;
import kitae.spring.health.notification.entity.Notification;
import kitae.spring.health.notification.service.NotificationService;
import kitae.spring.health.patient.entity.Patient;
import kitae.spring.health.patient.repository.PatientRepository;
import kitae.spring.health.response.Response;
import kitae.spring.health.role.entity.Role;
import kitae.spring.health.role.repository.RoleRepository;
import kitae.spring.health.security.JwtService;
import kitae.spring.health.users.dto.LoginRequest;
import kitae.spring.health.users.dto.LoginResponse;
import kitae.spring.health.users.dto.RegistrationRequest;
import kitae.spring.health.users.dto.ResetPasswordRequest;
import kitae.spring.health.users.entity.PasswordResetCode;
import kitae.spring.health.users.entity.User;
import kitae.spring.health.users.repository.PasswordResetCodeRepository;
import kitae.spring.health.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final CodeGenerator codeGenerator;

    @Value("${password.reset.link}")
    private String resetLink;

    @Value("${login.link}")
    private String loginLink;


    /**
     * 사용자 등록 처리
     *
     * @param request
     * @return
     */
    public Response<String> register(RegistrationRequest request) {
        // 사용자가 이미 존재하는 지 확인
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("해당 이메일이 이미 존재합니다.");
        }

        // 역할 배정
        List<String> requestedRoleNames = (request.getRoles() != null && !request.getRoles().isEmpty())
            ? request.getRoles().stream().map(String::toUpperCase).toList()
            : List.of("PATIENT");

        boolean isDoctor = requestedRoleNames.contains("DOCTOR");

        if (isDoctor && (request.getLicenseNumber() == null || request.getLicenseNumber().isBlank())) {
            throw new BadRequestException("의사로 등록하려면 면허 번호가 필요합니다.");
        }

        // 2. 데이터베이스에서 역할을 불러오고 역할의 유효성을 검사
        List<Role> roles = requestedRoleNames.stream()
            .map(roleRepository::findByName)
            .flatMap(Optional::stream)
            .toList();

        if (roles.isEmpty()) {
            throw new NotFoundException("등록 실패: 요청하신 역할을 데이터베이스에서 찾을 수 없습니다.");
        }

        // 3. 새로운 사용자 생성 및 저장
        User newUser = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .roles(roles)
            .build();

        User savedUser = userRepository.save(newUser);

        log.info("새로운 사용자 등록 : {}, {} 개의 역할을 갖고 있습니다.", savedUser.getEmail(), roles.size());

        // 4 프로필 생성
        for (Role role : roles) {
            String roleName = role.getName();

            switch (roleName) {
                case "PATIENT":
                    createPatientProfile(savedUser);
                    log.info("환자 프로필이 생성되었습니다: {}", savedUser.getEmail());
                    break;

                case "DOCTOR":
                    createDoctorProfile(request, savedUser);
                    log.info("의사 프로필이 생성되었습니다: {}", savedUser.getEmail());
                    break;

                case "ADMIN":
                    log.info("관리자 역할이 사용자에게 배정되었습니다: {}", savedUser.getEmail());
                    break;

                default:
                    log.warn("할당된 역할 {}에는 해당하는 프로필 생성 로직이 없습니다.", roleName);
                    break;
            }
        }

        // 5. 환영 이메일 전송
        sendRegistrationEmail(request, savedUser);

        // 6. 응답 반환
        return Response.<String>builder()
            .statusCode(200)
            .message("등록이 완료되었습니다. 환영 이메일이 발송되었습니다.")
            .data(savedUser.getEmail())
            .build();
    }

    public Response<LoginResponse> login(LoginRequest loginRequest) {

        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("해당 이메일의 사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("비밀번호가 올바르지 않습니다.");
        }

        String token = jwtService.generateToken(user.getEmail());

        LoginResponse loginResponse = LoginResponse.builder()
            .roles(user.getRoles().stream().map(Role::getName).toList())
            .token(token)
            .build();

        return Response.<LoginResponse>builder()
            .statusCode(200)
            .message("로그인에 성공하였습니다.")
            .data(loginResponse)
            .build();
    }

    public Response<?> forgetPassword(String email) {

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("해당 이메일의 사용자를 찾을 수 없습니다."));
        
        passwordResetCodeRepository.deleteByUserId(user.getId()); // 기존 코드 삭제
        
        String code = codeGenerator.generateUniqueCode();   // 고유 코드 생성
        
        PasswordResetCode passwordResetCode = PasswordResetCode.builder()
            .user(user)
            .code(code)
            .expiryDate(calculateExpiryDate()) //
            .used(false)
            .build();

        passwordResetCodeRepository.save(passwordResetCode);

        // 비밀번호 재설정 이메일 전송
        NotificationDTO passwordResetEmail = NotificationDTO.builder()
            .recipient(user.getEmail())
            .subject("TeleMed 헬스케어에서 비밀번호 재설정 요청")
            .templateName("password-reset")
            .templateVariables(Map.of(
                "name", user.getName(),
                "resetLink", resetLink + code
            ))
            .build();

        notificationService.sendEmail(passwordResetEmail, user); // 이메일 전송

        return Response.builder()
            .statusCode(200)
            .message("비밀번호 재설정 이메일이 발송되었습니다.")
            .build();
    }

    public Response<?> updatePasswordViaResetCode(ResetPasswordRequest resetPasswordRequest) {

        String code = resetPasswordRequest.getCode();
        String newPassword = resetPasswordRequest.getNewPassword();

        log.info("코드 : " + code);
        log.info("새 비밀번호: " + newPassword);

        PasswordResetCode resetCode = passwordResetCodeRepository.findByCode(code)
            .orElseThrow(() -> new BadRequestException("유효한 비밀번호 재설정 코드를 찾을 수 없습니다."));

        // 코드 만료 확인
        if(resetCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetCodeRepository.delete(resetCode); // 만료된 코드 삭제
            throw new BadRequestException("비밀번호 재설정 코드가 만료되었습니다.");
        }

        User user = resetCode.getUser();
        user.setPassword(passwordEncoder.encode(newPassword)); // 새 비밀번호 암호화 설정
        userRepository.save(user);

        passwordResetCodeRepository.delete(resetCode); // 사용된 코드 삭제

        // 비밀번호 변경 알림 이메일 전송
        NotificationDTO passwordResetEmail = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("TeleMed 헬스케어에서 비밀번호가 성공적으로 변경되었습니다.")
                .templateName("password-update-confirmation")
                .templateVariables(Map.of(
                        "name", user.getName()
                ))
                .build();

        notificationService.sendEmail(passwordResetEmail, user);    // 이메일 전송

        return Response.builder()
            .statusCode(200)
            .message("비밀번호가 성공적으로 변경되었습니다.")
            .build();
    }

    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusHours(5);    // 5시간 후 만료
    }

    /**
     * 환자 프로필 생성
     *
     * @param user
     */
    private void createPatientProfile(User user) {
        Patient patient = Patient.builder()
            .user(user)
            .build();
        patientRepository.save(patient);
        log.info("환자 프로필이 생성되었습니다: {}", user.getEmail());
    }

    /**
     * 의사 프로필 생성
     *
     * @param request
     * @param user
     */
    private void createDoctorProfile(RegistrationRequest request, User user) {
        Doctor doctor = Doctor.builder()
            .specialization(request.getSpecialization())
            .licenseNumber(request.getLicenseNumber())
            .user(user)
            .build();
        doctorRepository.save(doctor);
        log.info("의사 프로필이 생성되었습니다: {}", user.getEmail());
    }

    /**
     * 환영 이메일 전송
     *
     * @param request
     * @param user
     */
    private void sendRegistrationEmail(RegistrationRequest request, User user) {
        NotificationDTO welcomeEmail = NotificationDTO.builder()
            .recipient(user.getEmail())
            .subject("환영합니다, TeleMed 헬스케어 입니다.")
            .templateName("welcome")
            .message("회원가입해주셔서 감사합니다. 계정이 생성되었습니다.")
            .templateVariables(Map.of(
                "name", request.getName(),
                "loginLink", loginLink
            ))
            .build();
        notificationService.sendEmail(welcomeEmail, user);
    }
}
