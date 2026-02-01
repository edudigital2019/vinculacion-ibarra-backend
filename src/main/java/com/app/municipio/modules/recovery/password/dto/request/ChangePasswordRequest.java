package com.app.municipio.modules.recovery.password.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank
        @NotNull
        @Size(min = 8, max = 20)
        String newPassword
        ) {

}
