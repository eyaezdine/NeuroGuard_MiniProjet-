package com.devx.msbienetre.client;

import com.devx.msbienetre.dto.UserApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Doit correspondre au nom Eureka du user-service (voir gateway : {@code lb://user-service}).
 */
@FeignClient(name = "user-service", contextId = "userServiceClient")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserApiResponse getUserById(@PathVariable("id") String id);
}
