package com.app.municipio.modules.recovery.password.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OtpRequest(
        @NotNull
        @Size(min = 6, max = 6)
        @Pattern(regexp = "^[a-zA-Z\\d]*$")
        String otp,
        @NotNull
        UUID uuid) {
}
