package com.app.municipio.modules.recovery.password.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmailRequest(
        @NotNull
        @Email
        @Size(max = 50)
        String email) {

}
