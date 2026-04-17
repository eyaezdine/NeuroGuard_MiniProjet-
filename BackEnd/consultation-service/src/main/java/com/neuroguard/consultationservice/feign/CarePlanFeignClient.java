package com.neuroguard.consultationservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "careplan-service")
public interface CarePlanFeignClient {

    @GetMapping("/api/careplan/{patientId}")
    Object getPatientCarePlan(@PathVariable("patientId") Long patientId);

    @PutMapping("/api/careplan/{carePlanId}/status")
    Object updateCarePlanStatus(
            @PathVariable("carePlanId") Long carePlanId,
            @RequestParam("status") String status
    );
}
