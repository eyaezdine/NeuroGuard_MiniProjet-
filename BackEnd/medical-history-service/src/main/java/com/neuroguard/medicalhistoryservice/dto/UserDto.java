package com.neuroguard.medicalhistoryservice.dto;

import lombok.Data;

@Data
public class UserDto {
    private String id;               // MongoDB ObjectId as String
    private String username;          // will be mapped from email
    private String email;
    private String firstName;
    private String lastName;
    private String role;              // e.g. "PATIENT", "PROVIDER"
}