package kitae.spring.health.consultation.service;

import kitae.spring.health.appointment.entity.Appointment;
import kitae.spring.health.appointment.repository.AppointmentRepository;
import kitae.spring.health.consultation.dto.ConsultationDTO;
import kitae.spring.health.consultation.entity.Consultation;
import kitae.spring.health.consultation.repository.ConsultationRespository;
import kitae.spring.health.enums.AppointmentStatus;
import kitae.spring.health.exceptions.BadRequestException;
import kitae.spring.health.exceptions.NotFoundException;
import kitae.spring.health.patient.entity.Patient;
import kitae.spring.health.patient.repository.PatientRepository;
import kitae.spring.health.response.Response;
import kitae.spring.health.users.entity.User;
import kitae.spring.health.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationService {

    private final ConsultationRespository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final PatientRepository patientRepository;

    /**
     * 새로운 상담 기록을 생성
     * @param consultationDTO
     * @return
     */
    public Response<ConsultationDTO> createConsultation(ConsultationDTO consultationDTO) {

        User user = userService.getCurrentUser(); // 현재 인증된 사용자 가져오기
        Long appointmentId = consultationDTO.getAppointmentId();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다. ID: " + appointmentId));

        if(!appointment.getDoctor().getUser().getId().equals(user.getId())) {
            throw new BadRequestException("당신은 본 진료에 대한 메모를 작성할 권한이 없습니다..");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED); // 예약 상태를 COMPLETED로 업데이트
        appointmentRepository.save(appointment);    // 예약 상태 업데이트

        // 해당 예약에 대한 상담이 이미 진행 중인지 확인
        if(consultationRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new BadRequestException("이 예약에 대한 상담 기록이 이미 존재합니다. 예약 ID: " + appointmentId);
        }

        Consultation consultation = Consultation.builder()
                .consultationDate(LocalDateTime.now()) // 상담 날짜
                .subjectiveNotes(consultationDTO.getSubjectiveNotes()) // 주관적 소견
                .objectiveFindings(consultationDTO.getObjectiveFindings()) // 객관적 소견
                .assessment(consultationDTO.getAssessment()) // 평가
                .plan(consultationDTO.getPlan()) // 계획
                .appointment(appointment) // 예약 정보
                .build();

        consultationRepository.save(consultation);

        return Response.<ConsultationDTO>builder()
                .statusCode(200)
                .message("상담 기록이 성공적으로 생성되었습니다.")
                .data(modelMapper.map(consultation, ConsultationDTO.class))
                .build();
    }

    /**
     * 예약 ID로 상담 기록 조회
     * @param appointmentId
     * @return
     */
    public Response<ConsultationDTO> getConsultationByAppointmentId(Long appointmentId) {

        User user = userService.getCurrentUser();   // 현재 인증된 사용자 가져오기

        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new NotFoundException("상담 기록을 찾을 수 없습니다. 예약 ID: " + appointmentId));

        return Response.<ConsultationDTO>builder()
                .statusCode(200)
                .message("상담 기록이 성공적으로 조회되었습니다.")
                .data(modelMapper.map(consultation, ConsultationDTO.class))
                .build();
    }

    /**
     * 환자 ID로 상담 기록 목록 조회
     * @param patientId
     * @return
     */
    public Response<List<ConsultationDTO>> getConsultationHistoryForPatient(Long patientId) {

        User user = userService.getCurrentUser();  // 현재 인증된 사용자 가져오기

        // patientId가 null인 경우, 현재 인증된 환자의 ID를 가져옵니다.
        if(patientId == null){
            Patient currentPatient = patientRepository.findByUser(user)
                    .orElseThrow(() -> new BadRequestException("현재 사용자와 연결된 환자 정보를 찾을 수 없습니다."));
            patientId = currentPatient.getId(); // 현재 환자 ID 설정
        }

        // 환자가 실제로 존재하는지 확인
        patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("환자를 찾을 수 없습니다"));

        // 해당 환자의 상담 기록 목록 조회
        List<Consultation> history = consultationRepository
                .findByAppointmentPatientIdOrderByConsultationDateDesc(patientId);

        if(history.isEmpty()){
            return Response.<List<ConsultationDTO>>builder()
                    .statusCode(200)
                    .message("해당 환자의 상담 기록이 없습니다.")
                    .data(List.of())
                    .build();
        }

        // Consultation 엔티티를 ConsultationDTO로 매핑
        List<ConsultationDTO> historyDTOs = history.stream()
                .map(consultation -> modelMapper.map(consultation, ConsultationDTO.class))
                .toList();

        return Response.<List<ConsultationDTO>>builder()
                .statusCode(200)
                .message("상담 기록이 성공적으로 조회되었습니다.")
                .data(historyDTOs)
                .build();
    }
}