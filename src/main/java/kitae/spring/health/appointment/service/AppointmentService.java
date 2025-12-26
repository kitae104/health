package kitae.spring.health.appointment.service;

import kitae.spring.health.appointment.dto.AppointmentDTO;
import kitae.spring.health.appointment.entity.Appointment;
import kitae.spring.health.appointment.repository.AppointmentRepository;
import kitae.spring.health.doctor.entity.Doctor;
import kitae.spring.health.doctor.repository.DoctorRepository;
import kitae.spring.health.enums.AppointmentStatus;
import kitae.spring.health.exceptions.BadRequestException;
import kitae.spring.health.exceptions.NotFoundException;
import kitae.spring.health.notification.dto.NotificationDTO;
import kitae.spring.health.notification.service.NotificationService;
import kitae.spring.health.patient.entity.Patient;
import kitae.spring.health.patient.repository.PatientRepository;
import kitae.spring.health.response.Response;
import kitae.spring.health.users.entity.User;
import kitae.spring.health.users.repository.UserRepository;
import kitae.spring.health.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ", Locale.KOREAN);
    private final UserService userService;

    /**
     * 예약 등록
     * @param appointmentDTO
     * @return
     */
    public Response<AppointmentDTO> bookAppointment(AppointmentDTO appointmentDTO) {

        User currentUser = userService.getCurrentUser();

        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new NotFoundException("해당 환자를 찾을 수 없습니다."));

        Doctor doctor = doctorRepository.findById(appointmentDTO.getDoctorId())
                .orElseThrow(() -> new NotFoundException("해당 의사를 찾을 수 없습니다."));

        LocalDateTime startTime = appointmentDTO.getStartTime(); // 예약 시작 시간
        LocalDateTime endTime = startTime.plusMinutes(60); // 예약 종료 시간 (1시간 후)

        // 예약 시간 유효성 검사
        if(startTime.isBefore(LocalDateTime.now().plusHours(1))){
            throw new BadRequestException("예약은 최소 1시간 이전에 해야 합니다.");
        }

        // 이 코드 스니펫은 새로운 예약 전에 의사에게 1시간의 휴식 시간(또는 버퍼)을 의무적으로 부여하는 로직입니다.
        LocalDateTime checkStart = startTime.minusMinutes(60); // 예약 시작 시간 1시간 전

        // 기존 예약 중 종료 시간이 제안된 시작 시간과 겹치거나, 시작 시간이 제안된 종료 시간과 겹치는 예약만 확인하면 됩니다.
        List<Appointment> conflicts = appointmentRepository.findConflictingAppointments(
                doctor.getId(), checkStart, endTime);

        if(!conflicts.isEmpty()){
            throw new BadRequestException("해당 시간대에 예약이 불가능합니다. 다른 시간을 선택해주세요.");
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String uniqueRoomName = "health-" + uuid.substring(0, 10);

        String meetingLink = "https://meet.jit.si/" + uniqueRoomName;
        log.info("미팅 링크 생성: " + meetingLink);

        Appointment appointment = Appointment.builder()
                .startTime(appointmentDTO.getStartTime())
                .endTime(appointmentDTO.getStartTime().plusMinutes(60))
                .meetingLink(meetingLink)
                .initialSymptoms(appointmentDTO.getInitialSymptoms())
                .purposeOfConsultation(appointmentDTO.getPurposeOfConsultation())
                .status(AppointmentStatus.SCHEDULED)
                .doctor(doctor)
                .patient(patient)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        sendAppointmentConfirmation(savedAppointment);  // 예약 확인 알림 전송

        return Response.<AppointmentDTO>builder()
                .statusCode(200)
//                .data(modelMapper.map(savedAppointment, AppointmentDTO.class))
                .message("예약이 성공적으로 등록되었습니다.")
                .build();
    }

    /**
     * 내 예약 조회
     * @return
     */
    public Response<List<AppointmentDTO>> getMyAppointments() {

        User user = userService.getCurrentUser();

        Long userId = user.getId();

        List<Appointment> appointments;

        // "DOCTOR" 권한이 있는지 확인
        boolean isDoctor = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("DOCTOR"));

        if(isDoctor){
            doctorRepository.findByUser(user)
                    .orElseThrow(() -> new NotFoundException("해당 의사를 찾을 수 없습니다."));

            appointments = appointmentRepository.findByDoctor_User_IdOrderByIdDesc(userId);
        } else {
            patientRepository.findByUser(user)
                    .orElseThrow(() -> new NotFoundException("해당 환자를 찾을 수 없습니다."));

            appointments = appointmentRepository.findByPatient_User_IdOrderByIdDesc(userId);
        }

        List<AppointmentDTO> appointmentDTOList = appointments.stream()
                .map(appointment -> modelMapper.map(appointment, AppointmentDTO.class))
                .toList();

        return Response.<List<AppointmentDTO>>builder()
                .statusCode(200)
                .data(appointmentDTOList)
                .message("내 예약 조회에 성공했습니다.")
                .build();

    }

    /**
     * 예약 취소
     * @param appointmentId
     * @return
     */
    public Response<AppointmentDTO> cancelAppointment(Long appointmentId) {

        User user = userService.getCurrentUser();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("해당 예약을 찾을 수 없습니다."));

        boolean isOwner = appointment.getPatient().getUser().getId().equals(user.getId()) ||
                appointment.getDoctor().getId().equals(user.getId());

        if(!isOwner){
            throw new BadRequestException("당신은 예약을 취소할 권한이 없습니다.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 참고: 알림은 상대방(환자/의사)에게 보내야 합니다.
        sendAppointmentCancellation(savedAppointment, user);  // 예약 취소 알림 전송

        return Response.<AppointmentDTO>builder()
                .statusCode(200)
//                .data(modelMapper.map(savedAppointment, AppointmentDTO.class))
                .message("예약이 성공적으로 취소되었습니다.")
                .build();
    }

    /**
     * 예약 완료
     * @param appointmentId
     * @return
     */
    public Response<?> completeAppointment(Long appointmentId) {

        User currentUser = userService.getCurrentUser();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("해당 예약을 찾을 수 없습니다."));

        if(!appointment.getDoctor().getUser().getId().equals(currentUser.getId())){
            throw new BadRequestException("당신은 이 예약을 완료할 권한이 없습니다.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setEndTime(LocalDateTime.now());

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        modelMapper.map(updatedAppointment, AppointmentDTO.class);

        return Response.builder()
                .statusCode(200)
                .message("진료 예약이 성공적으로 완료되었습니다. 이제 진료 기록을 작성하실 수 있습니다.")
                .build();
    }

    /**
     * 예약 취소 알림 전송
     * @param appointment
     * @param cancelingUser
     */
    private void sendAppointmentCancellation(Appointment appointment, User cancelingUser) {

        User patientUser = appointment.getPatient().getUser();  // 환자 사용자 정보
        User doctorUser = appointment.getDoctor().getUser(); // 의사 사용자 정보

        // 취소하는 사용자가 참여하고 있는지 확인하기 위한 안전 점검
        boolean isOwner = patientUser.getId().equals(cancelingUser.getId()) ||
                doctorUser.getId().equals(cancelingUser.getId());

        if(!isOwner){
            log.error("예약 취소 알림 전송 실패: 사용자가 예약의 당사자가 아닙니다. 사용자 ID: " + cancelingUser.getId());
            return;
        }

        String formattedTime = appointment.getStartTime().format(FORMATTER); // 예약 시간 포맷팅
        String cancellingPartyName = cancelingUser.getName(); // 취소하는 사람의 이름

        // 템플릿에서 공통으로 사용되는 변수
        Map<String, Object> baseVars = new HashMap<>();
        baseVars.put("cancelingPartyName", cancellingPartyName);
        baseVars.put("appointmentTime", formattedTime);
        baseVars.put("doctorName", appointment.getDoctor().getLastName());
        baseVars.put("patientFullName", patientUser.getName());

        // 의사에게 이메일 발송
        Map<String, Object> doctorVars = new HashMap<>(baseVars);
        doctorVars.put("recipientName", doctorUser.getName());

        NotificationDTO doctorNotification = NotificationDTO.builder()
                .recipient(doctorUser.getEmail())
                .subject("KITAE Health - 예약 취소 알림")
                .templateName("appointment-cancellation")
                .templateVariables(doctorVars)
                .build();

        notificationService.sendEmail(doctorNotification, doctorUser);
        log.info("의사에게 예약 취소 알림 전송 완료: " + doctorUser.getEmail());

        // 환자에게 이메일 발송
        Map<String, Object> patientVars = new HashMap<>(baseVars);
        patientVars.put("recipientName", patientUser.getName());

        NotificationDTO patientNotification = NotificationDTO.builder()
                .recipient(patientUser.getEmail())
                .subject("KITAE Health - 예약 취소 알림(ID : " + appointment.getId() + ")")
                .templateName("appointment-cancellation")
                .templateVariables(patientVars)
                .build();

        notificationService.sendEmail(patientNotification, patientUser);
        log.info("환자에게 예약 취소 알림 전송 완료: {}", patientUser.getEmail());
    }

    /**
     * 예약 확인 알림 전송
     * @param appointment
     */
    private void sendAppointmentConfirmation(Appointment appointment) {

        User patientUser = appointment.getPatient().getUser();  // 환자 사용자 정보
        String formattedTime = appointment.getStartTime().format(FORMATTER); // 예약 시간 포맷팅

        // 환자에게 이메일 발송
        Map<String, Object> patientVars = new HashMap<>();
        patientVars.put("patientName", patientUser.getName());
        patientVars.put("doctorName", appointment.getDoctor().getUser().getName());
        patientVars.put("appointmentTime", formattedTime);
        patientVars.put("isVirtual", true);
        patientVars.put("meetingLink", appointment.getMeetingLink());
        patientVars.put("purposeOfConsultation", appointment.getPurposeOfConsultation());

        NotificationDTO patientNotification = NotificationDTO.builder()
                .recipient(patientUser.getEmail())
                .subject("KITAE Health - 예약 확인 알림")
                .templateName("patient-appointment")
                .templateVariables(patientVars)
                .build();

        notificationService.sendEmail(patientNotification, patientUser);
        log.info("환자에게 예약 확인 알림 전송 완료: {}", patientUser.getEmail());

        // 의사에게 이메일 발송
        User doctorUser = appointment.getDoctor().getUser(); // 의사 사용자 정보

        Map<String, Object> doctorVars = new HashMap<>();
        doctorVars.put("doctorName", doctorUser.getName());
        doctorVars.put("patientFullName", patientUser.getName());
        doctorVars.put("appointmentTime", formattedTime);
        doctorVars.put("isVirtual", true);
        doctorVars.put("meetingLink", appointment.getMeetingLink());
        doctorVars.put("initialSymptoms", appointment.getInitialSymptoms());
        doctorVars.put("purposeOfConsultation", appointment.getPurposeOfConsultation());

        NotificationDTO doctorNotification = NotificationDTO.builder()
                .recipient(doctorUser.getEmail())
                .subject("KITAE Health - 새로운 예약 알림")
                .templateName("doctor-appointment")
                .templateVariables(doctorVars)
                .build();

        notificationService.sendEmail(doctorNotification, doctorUser);
        log.info("의사에게 새로운 예약 알림 전송 완료: " + doctorUser.getEmail());
    }

}
