package com.neuroguard.consultationservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "medical-history-service")
public interface MedicalHistoryFeignClient {

    @GetMapping("/api/patient/medical-history/{patientId}")
    Object getPatientMedicalHistory(@PathVariable("patientId") Long patientId);

    @GetMapping("/api/provider/medical-history/{providerId}")
    Object getProviderMedicalHistory(@PathVariable("providerId") Long providerId);
}
