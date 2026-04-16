package com.devx.msbienetre.client;

import com.devx.msbienetre.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-users")
public interface UserClient {

    @GetMapping("/api/users/username/{username}")
    UserDTO getUserByUsername(@PathVariable("username") String username);

    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") String id);

    @GetMapping("/api/users/role/{role}")
    List<UserDTO> getUsersByRole(@PathVariable("role") String role);
}
