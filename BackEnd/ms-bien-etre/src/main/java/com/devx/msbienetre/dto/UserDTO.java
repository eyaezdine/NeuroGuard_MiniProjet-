package com.devx.msbienetre.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id; // MongoDB ObjectId as String
    private String username; // will be mapped from email
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
