package com.neuroguard.medicalhistoryservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "careplan-service")
public interface CarePlanFeignClient {

    @GetMapping("/api/careplan/{carePlanId}/activities")
    Object getCarePlanActivities(@PathVariable("carePlanId") Long carePlanId);

    @PostMapping("/api/careplan/{carePlanId}/activities")
    Object addActivityToCarePlan(
            @PathVariable("carePlanId") Long carePlanId,
            @RequestBody Object activity
    );
}
