package com.neuroguard.consultationservice.service;

import com.neuroguard.consultationservice.dto.UserDto;
import com.neuroguard.consultationservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "user-service",
        path = "/users",
        configuration = FeignClientConfig.class)
public interface UserServiceClient {

    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @GetMapping("/caregiver/{caregiverId}/patients")
    List<UserDto> getPatientsByCaregiver(@PathVariable("caregiverId") Long caregiverId);
}