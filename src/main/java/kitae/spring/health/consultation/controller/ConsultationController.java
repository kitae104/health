package kitae.spring.health.consultation.controller;

import jakarta.validation.Valid;
import kitae.spring.health.consultation.dto.ConsultationDTO;
import kitae.spring.health.consultation.entity.Consultation;
import kitae.spring.health.consultation.service.ConsultationService;
import kitae.spring.health.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<Response<ConsultationDTO>> createConsultation(@RequestBody @Valid ConsultationDTO consultationDTO) {
        return ResponseEntity.ok(consultationService.createConsultation(consultationDTO));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<Response<ConsultationDTO>> getConsultationByAppointmentId(@PathVariable("appointmentId") Long appointmentId) {
        return ResponseEntity.ok(consultationService.getConsultationByAppointmentId(appointmentId));
    }

    @GetMapping("/history")
    public ResponseEntity<Response<List<ConsultationDTO>>> getConsultationHistoryForPatient(
            @RequestParam(required = false) Long patientId ) {
        return ResponseEntity.ok(consultationService.getConsultationHistoryForPatient(patientId));
    }

}
