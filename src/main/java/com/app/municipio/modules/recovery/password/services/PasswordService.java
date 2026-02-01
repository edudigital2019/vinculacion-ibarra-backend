package com.app.municipio.modules.recovery.password.services;

import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.modules.recovery.password.dto.request.ChangePasswordRequest;
import com.app.municipio.modules.recovery.password.dto.request.EmailRequest;
import com.app.municipio.modules.recovery.password.dto.request.OtpRequest;
import com.app.municipio.modules.recovery.password.dto.response.CodeRecoveryResponse;
import com.app.municipio.modules.recovery.password.dto.response.UserResponse;
import com.app.municipio.modules.recovery.password.models.OtpRecovery;
import com.app.municipio.modules.recovery.password.repositories.OtpRecoveryRepository;
import com.app.municipio.modules.users.models.AppUser;
import com.app.municipio.modules.users.repositories.UsersRepository;
import com.app.municipio.modules.users.services.UserService;
import com.app.municipio.utils.EmailMessages;
import com.app.municipio.utils.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UsersRepository usersRepository;
    private final OtpRecoveryRepository otpRecoveryRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserService  userService;

    public CodeRecoveryResponse validateEmail(EmailRequest emailRequest) {
        return usersRepository.findByUsernameOrEmail(emailRequest.email())
                .map(appUser -> {
                    otpRecoveryRepository.deleteAllByAppUser(appUser);

                    var otp = new DecimalFormat("000000").format(new SecureRandom().nextInt(999999));

                    emailService.sendEmail(appUser.getEmail(),
                            EmailMessages.getRecoveryPasswordSubject(),
                            EmailMessages.getRecoverOtpMessage(appUser.getUsername(), otp));


                    OtpRecovery otpRecovery = otpRecoveryRepository.save(OtpRecovery.builder()
                            .otp(otp)
                            .appUser(appUser)
                            .build());

                    return new CodeRecoveryResponse(otpRecovery.getUuid());
                })
                .orElseThrow(() -> new ClientException("Correo invalido", HttpStatus.NOT_FOUND));
    }

    public UserResponse validateOtp(OtpRequest otpRequest) {
        return otpRecoveryRepository.findById(otpRequest.uuid())
                .filter(this::isOtpNotYetValidated)
                .filter(otp -> isOtpCodeCorrect(otp, otpRequest.otp()))
                .map(this::processValidOtp)
                .map(otp -> new UserResponse(otp.getAppUser().getId()))
                .orElseThrow(() -> determineValidationError(otpRequest));
    }

    private boolean isOtpNotYetValidated(OtpRecovery otp) {
        return !otp.isOtpValidated();
    }

    private boolean isOtpCodeCorrect(OtpRecovery otp, String providedOtp) {
        return otp.getOtp().equals(providedOtp);
    }

    private OtpRecovery processValidOtp(OtpRecovery otp) {
        otp.setOtpValidated(true);
        return otpRecoveryRepository.save(otp);
    }

    private ClientException determineValidationError(OtpRequest otpRequest) {
        return otpRecoveryRepository.findById(otpRequest.uuid())
                .map(otp -> {
                    if (otp.isOtpValidated()) {
                        return new ClientException("El código de recuperación ya ha sido utilizado", HttpStatus.BAD_REQUEST);
                    } else {
                        return new ClientException("Código de recuperación incorrecto", HttpStatus.BAD_REQUEST);
                    }
                })
                .orElse(new ClientException("Otp Invalido", HttpStatus.BAD_REQUEST));
    }

    public void changePassword(Long userId, ChangePasswordRequest passwordReq) {
        var appUser = validateParams(userId, passwordReq);
        usersRepository.save(appUser);

        emailService.sendEmail(appUser.getEmail(),
                EmailMessages.getPasswordChangeSubject(),
                EmailMessages.getChangePasswordMessage(appUser.getUsername()));

    }

    /**
     * Valida los parametros de la clave, la existencia del usuario, que la nueva clave no sea igual a la que esta en base
     * Y retorna el objeto {@link AppUser} con la nueva clave codificada
     * @param userId PK del registro de usuario
     * @param changePasswordRequest Objeto con las nuevas claves
     * @return Registro de usuario con la nueva clave seteada
     */
    private AppUser validateParams(Long userId, ChangePasswordRequest changePasswordRequest) {
        boolean isPasswordInvalid = userService.isPasswordInvalid(changePasswordRequest.newPassword()  );

        if (isPasswordInvalid) {
            throw new ClientException("Claves inseguras", HttpStatus.BAD_REQUEST);
        }

        var appUser = usersRepository.findById(userId)
                .orElseThrow(() -> new ClientException("Usuario invalido", HttpStatus.NOT_FOUND));

        var otpRecovery = otpRecoveryRepository.findByAppUser(appUser)
                .orElseThrow(() -> new ClientException("Debe solicitar un código de recuperación antes de cambiar la clave", HttpStatus.BAD_REQUEST));

        if (!otpRecovery.isOtpValidated()) {
            throw new ClientException("Debe validar el codigo primero", HttpStatus.BAD_REQUEST);
        }


        var encodedNewPassword = passwordEncoder.encode(changePasswordRequest.newPassword());

        appUser.setPassword(encodedNewPassword);
        otpRecoveryRepository.deleteById(otpRecovery.getUuid());
        return appUser;
    }

}
