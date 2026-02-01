package com.app.municipio.modules.users.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"username", "message", "jwt", "status"})
public class AuthResponseDto {
    private String username;

    private String message;

    private String jwt;

    private boolean status;
}
