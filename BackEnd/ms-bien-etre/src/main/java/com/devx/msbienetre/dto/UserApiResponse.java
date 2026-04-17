package com.devx.msbienetre.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Enveloppe JSON renvoyée par le user-service Node (<code>{ "success", "data" }</code>). */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserApiResponse {
    private boolean success;
    private UserDTO data;
}
