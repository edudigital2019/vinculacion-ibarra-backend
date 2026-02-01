package com.app.municipio.modules.recovery.password.controllers;

import com.app.municipio.modules.recovery.password.dto.request.ChangePasswordRequest;
import com.app.municipio.modules.recovery.password.dto.request.EmailRequest;
import com.app.municipio.modules.recovery.password.dto.request.OtpRequest;
import com.app.municipio.modules.recovery.password.dto.response.CodeRecoveryResponse;
import com.app.municipio.modules.recovery.password.dto.response.UserResponse;
import com.app.municipio.modules.recovery.password.services.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recovery")
@Tag(name = "Recovery", description = "Endpoints for recovery password")
public class PasswordController {

    private final PasswordService passwordService;

    @PreAuthorize("permitAll()")
    @PostMapping("/email/validation")
    @Operation(summary = "Validacion de correo", description = "Endpoint para validar correo en la recuperacion de claves")
    public ResponseEntity<CodeRecoveryResponse> emailValidator(@Valid @RequestBody EmailRequest emailRequest) {
        var codeRecovery = passwordService.validateEmail(emailRequest);
        return ResponseEntity.ok(codeRecovery);
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/otp/validation")
    @Operation(summary = "Validacion de otp", description = "Endpoint para validar otp ingresado")
    public ResponseEntity<UserResponse> otpValidator(@Valid @RequestBody OtpRequest otpRequest) {
        var otpValidation = passwordService.validateOtp(otpRequest);
        return ResponseEntity.ok(otpValidation);
    }

    @PreAuthorize("permitAll()")
    @PutMapping("/password/{userId}")
    @Operation(summary = "Cambio de clave", description = "Endpoint para cambio de clave una vez se verifique el otp valido")
    public ResponseEntity<Void> changePassword(@PathVariable Long userId, @Valid @RequestBody ChangePasswordRequest passwordReq) {
        passwordService.changePassword(userId, passwordReq);
        return ResponseEntity.noContent().build();
    }



}
