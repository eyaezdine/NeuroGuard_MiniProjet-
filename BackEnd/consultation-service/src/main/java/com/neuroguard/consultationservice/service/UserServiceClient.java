package com.neuroguard.consultationservice.service;

import com.neuroguard.consultationservice.dto.ApiResponse;
import com.neuroguard.consultationservice.dto.UserDto;
import com.neuroguard.consultationservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service",
        url = "${user-service.url}",
        configuration = FeignClientConfig.class)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    ApiResponse<UserDto> getUserById(@PathVariable("id") String id);

    // Si vous avez besoin de récupérer des utilisateurs par rôle, ajoutez :
    // @GetMapping("/api/users/role/{role}")
    // ApiResponse<List<UserDto>> getUsersByRole(@PathVariable("role") String role);
}