package kitae.spring.health.patient.controller;

import kitae.spring.health.enums.BloodGroup;
import kitae.spring.health.enums.Genotype;
import kitae.spring.health.patient.dto.PatientDTO;
import kitae.spring.health.patient.service.PatientService;
import kitae.spring.health.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    /**
     * 환자 프로필 가져오기
     * @return
     */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<Response<PatientDTO>> getPatientProfile() {
        return ResponseEntity.ok(patientService.getPatientProfile());
    }

    /**
     * 환자 프로필 업데이트
     * @param patientDTO
     * @return
     */
    @PutMapping("/me")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<Response<?>> updatePatientProfile(@RequestBody PatientDTO patientDTO) {
        return ResponseEntity.ok(patientService.updatePatientProfile(patientDTO));
    }

    /**
     * 환자 ID로 환자 정보 가져오기
     * @param patientId
     * @return
     */
    @GetMapping("/{patientId}")
    public ResponseEntity<Response<PatientDTO>> getPatientById(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getPatientById(patientId));
    }

    /**
     * 모든 혈액형 열거형 가져오기
     * @return
     */
    @GetMapping("/bloodgroup")
    public ResponseEntity<Response<List<BloodGroup>>> getAllBloodGroupEnums() {
        return ResponseEntity.ok(patientService.getAllBloodGroupEnums());
    }

    /**
     * 모든 유전자형 열거형 가져오기
     * @return
     */
    @GetMapping("/genotypes")
    public ResponseEntity<Response<List<Genotype>>> getAllGenotypeEnums() {
        return ResponseEntity.ok(patientService.getAllGenotypeEnums());
    }

}

