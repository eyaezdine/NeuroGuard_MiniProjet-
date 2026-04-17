package com.neuroguard.medicalhistoryservice.controller;

import com.neuroguard.medicalhistoryservice.dto.ClinicalSummaryResponse;
import com.neuroguard.medicalhistoryservice.service.MedicalHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/medical-history")
@RequiredArgsConstructor
public class InternalMedicalHistoryController {

    private final MedicalHistoryService medicalHistoryService;

    @GetMapping("/patients/{patientId}/clinical-summary")
    public ResponseEntity<ClinicalSummaryResponse> getClinicalSummary(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalHistoryService.getClinicalSummary(patientId));
    }
}
