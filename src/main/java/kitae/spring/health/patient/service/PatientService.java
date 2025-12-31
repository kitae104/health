package kitae.spring.health.patient.service;

import kitae.spring.health.enums.BloodGroup;
import kitae.spring.health.enums.Genotype;
import kitae.spring.health.exceptions.NotFoundException;
import kitae.spring.health.patient.dto.PatientDTO;
import kitae.spring.health.patient.entity.Patient;
import kitae.spring.health.patient.repository.PatientRepository;
import kitae.spring.health.response.Response;
import kitae.spring.health.users.entity.User;
import kitae.spring.health.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    /**
     * 환자 프로필 가져오기
     * @return
     */
    public Response<PatientDTO> getPatientProfile() {

        User user = userService.getCurrentUser();

        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("환자 프로필을 찾을 수 없습니다."));

        return Response.<PatientDTO>builder()
                .statusCode(200)
                .message("환자 프로필을 성공적으로 가져왔습니다.")
                .data(modelMapper.map(patient, PatientDTO.class))
                .build();
    }

    /**
     * 환자 프로필 업데이트
     * @param patientDTO
     * @return
     */
    public Response<?> updatePatientProfile(PatientDTO patientDTO) {

        User user = userService.getCurrentUser();

        Patient patient = patientRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("환자 프로필을 찾을 수 없습니다."));

        // 내용 존재 여부 확인하고 업데이트
        if(StringUtils.hasText(patientDTO.getFirstName())) {
            patient.setFirstName(patientDTO.getFirstName());
        }

        if(StringUtils.hasText(patientDTO.getLastName())) {
            patient.setLastName(patientDTO.getLastName());
        }

        if(StringUtils.hasText(patientDTO.getPhone())) {
            patient.setPhone(patientDTO.getPhone());
        }

        Optional.ofNullable(patientDTO.getDateOfBirth())
                .ifPresent(patient::setDateOfBirth);

        if(StringUtils.hasText(patientDTO.getKnownAllergies())){
            patient.setKnownAllergies(patientDTO.getKnownAllergies());
        }

        Optional.ofNullable(patientDTO.getBloodGroup()).ifPresent(patient::setBloodGroup);
        Optional.ofNullable(patientDTO.getGenotype()).ifPresent(patient::setGenotype);

        patientRepository.save(patient);

        return Response.builder()
                .statusCode(200)
                .message("환자 프로필이 성공적으로 업데이트되었습니다.")
                .build();
    }

    /**
     * 환자 ID로 환자 정보 가져오기
     * @param patientId
     * @return
     */
    public Response<PatientDTO> getPatientById(Long patientId) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("환자를 찾을 수 없습니다. ID: " + patientId));

        PatientDTO patientDTO = modelMapper.map(patient, PatientDTO.class);

        return Response.<PatientDTO>builder()
                .statusCode(200)
                .message("환자 정보 조회를 성공했습니다.")
                .data(patientDTO)
                .build();
    }

    /**
     * 모든 혈액형 열거형 가져오기
     * @return
     */
    public Response<List<BloodGroup>> getAllBloodGroupEnums() {

        List<BloodGroup> bloodGroups = Arrays.asList(BloodGroup.values()); // 모든 열거형 값을 리스트로 변환

        return Response.<List<BloodGroup>>builder()
                .statusCode(200)
                .message("모든 혈액형 열거형 조회 성공")
                .data(bloodGroups)
                .build();
    }

    /**
     * 모든 유전자형 열거형 가져오기
     * @return
     */
    public Response<List<Genotype>> getAllGenotypeEnums() {

        List<Genotype> genotypes = Arrays.asList(Genotype.values()); // 모든 열거형 값을 리스트로 변환

        return Response.<List<Genotype>>builder()
                .statusCode(200)
                .message("모든 유전자형 열거형 조회 성공")
                .data(genotypes)
                .build();
    }

}
