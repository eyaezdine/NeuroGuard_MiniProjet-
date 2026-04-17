package com.esprit.microservice.careplanservice.feign;

import com.esprit.microservice.careplanservice.dto.ClinicalSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "medical-history-service")
public interface MedicalHistoryServiceClient {

    @GetMapping("/api/internal/medical-history/patients/{patientId}/clinical-summary")
    ClinicalSummaryDto getClinicalSummary(@PathVariable("patientId") Long patientId);
}
