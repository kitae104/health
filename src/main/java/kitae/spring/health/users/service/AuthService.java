package kitae.spring.health.users.service;

import kitae.spring.health.doctor.repository.DoctorRepository;
import kitae.spring.health.notification.service.NotificationService;
import kitae.spring.health.patient.repository.PatientRepository;
import kitae.spring.health.role.repository.RoleRepository;
import kitae.spring.health.security.JwtService;
import kitae.spring.health.users.repository.PasswordResetCodeRepository;
import kitae.spring.health.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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



}
