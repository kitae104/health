package kitae.spring.health.doctor.service;

import kitae.spring.health.doctor.dto.DoctorDTO;
import kitae.spring.health.doctor.entity.Doctor;
import kitae.spring.health.doctor.repository.DoctorRepository;
import kitae.spring.health.enums.Specialization;
import kitae.spring.health.exceptions.NotFoundException;
import kitae.spring.health.response.Response;
import kitae.spring.health.users.entity.User;
import kitae.spring.health.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    /**
     * 의사 프로필 가져오기
     * @return
     */
    public Response<DoctorDTO> getDoctorProfile() {

        User user = userService.getCurrentUser();

        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("의사 프로필을 찾을 수 없습니다."));

        return Response.<DoctorDTO>builder()
                .statusCode(200)
                .message("의사 프로필을 성공적으로 가져왔습니다.")
                .data(modelMapper.map(doctor, DoctorDTO.class))
                .build();
    }

    /**
     * 의사 프로필 업데이트
     * @param doctorDTO
     * @return
     */
    public Response<?> updateDoctorProfile(DoctorDTO doctorDTO) {

        User user = userService.getCurrentUser();

        Doctor doctor = doctorRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("의사 프로필을 찾을 수 없습니다."));

        if(StringUtils.hasText(doctorDTO.getFirstName())) {
            doctor.setFirstName(doctorDTO.getFirstName());
        }
        if(StringUtils.hasText(doctorDTO.getLastName())) {
            doctor.setLastName(doctorDTO.getLastName());
        }

        Optional.ofNullable(doctorDTO.getSpecialization()).ifPresent(doctor::setSpecialization);

        doctorRepository.save(doctor);
        log.info("의사 프로필이 성공적으로 업데이트되었습니다");

        return Response.builder()
                .statusCode(200)
                .message("의사 프로필이 성공적으로 업데이트되었습니다.")
                .build();
    }

    /**
     * 모든 의사 정보 가져오기
     * @return
     */
    public Response<List<DoctorDTO>> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();

        List<DoctorDTO> doctorDTOs = doctors.stream()
                .map(doctor -> modelMapper.map(doctor, DoctorDTO.class))
                .toList();

        return Response.<List<DoctorDTO>>builder()
                .statusCode(200)
                .message("모든 의사 정보를 성공적으로 가져왔습니다.")
                .data(doctorDTOs)
                .build();
    }

    /**
     * 의사 ID로 의사 정보 가져오기
     * @param doctorId
     * @return
     */
    public Response<DoctorDTO> getDoctorById(Long doctorId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("의사를 찾을 수 없습니다. ID: " + doctorId));

        return Response.<DoctorDTO>builder()
                .statusCode(200)
                .message("의사 정보 조회를 성공했습니다.")
                .data(modelMapper.map(doctor, DoctorDTO.class))
                .build();
    }

    /**
     * 전문 분야로 의사 검색
     * @param specialization
     * @return
     */
    public Response<List<DoctorDTO>> searchDoctorsBySpecialization(Specialization specialization) {

        List<Doctor> doctors = doctorRepository.findBySpecialization(specialization);

        List<DoctorDTO> doctorDTOs = doctors.stream()
                .map(doctor -> modelMapper.map(doctor, DoctorDTO.class))
                .toList();

        String message = doctors.isEmpty() ?
                "해당 전문 분야의 의사를 찾을 수 없습니다: " + specialization.name() :
                "해당 전문 분야의 의사 검색에 성공했습니다: " + specialization.name();

        return Response.<List<DoctorDTO>>builder()
                .statusCode(200)
                .message(message)
                .data(doctorDTOs)
                .build();
    }

    /**
     * 모든 전문 분야 열거형 가져오기
     * @return
     */
    public Response<List<Specialization>> getAllSpecializationEnums() {

        List<Specialization> specializations = Arrays.asList(Specialization.values());

        return Response.<List<Specialization>>builder()
                .statusCode(200)
                .message("모든 전문 분야 열거형 조회 성공")
                .data(specializations)
                .build();
    }
}
