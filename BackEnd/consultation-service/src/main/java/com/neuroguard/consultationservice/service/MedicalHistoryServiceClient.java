package com.neuroguard.consultationservice.service;

import com.neuroguard.consultationservice.config.FeignClientConfig;
import com.neuroguard.consultationservice.dto.ClinicalSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "medical-history-service", configuration = FeignClientConfig.class)
public interface MedicalHistoryServiceClient {

    @GetMapping("/api/internal/medical-history/patients/{patientId}/clinical-summary")
    ClinicalSummaryDto getClinicalSummary(@PathVariable("patientId") Long patientId);
}
