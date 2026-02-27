package com.neuroguard.consultationservice.dto;

public class UserDto {
    private String id;              // ← passé de Long à String
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    // Optionnel : conserver username pour compatibilité, mais on l'ignore
    private String username;

    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}