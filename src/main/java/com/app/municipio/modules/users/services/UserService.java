package com.app.municipio.modules.users.services;

import com.app.municipio.application.cloudinary.UploadFileInfo;
import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.application.validations.annotation.FileSize;
import com.app.municipio.application.validations.annotation.FileType;
import com.app.municipio.modules.users.dto.DashboardStats;
import com.app.municipio.modules.users.dto.UpdateUserDto;
import com.app.municipio.modules.users.dto.UserRegisterDto;
import com.app.municipio.modules.users.models.*;
import com.app.municipio.modules.users.models.enums.IdTypes;
import com.app.municipio.modules.users.models.enums.UserRoles;
import com.app.municipio.modules.users.repositories.*;
import com.app.municipio.utils.EmailMessages;
import com.app.municipio.utils.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.passay.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Set;

@Log4j2
@Validated
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthoritiesRepository authorityRepository;
    private final DocumentsService documentsService;
    private final UserDocumentsRepository userDocumentsRepository;
    private final CertificatesRepository certificatesRepository;
    private final EmailService emailService;
    private final SignedDocumentsRepository signedDocumentsRepository;
    private final PaymentReceiptRepository paymentReciptRepository;

    public void registerUser(
            @Valid UserRegisterDto dto,
            @FileType({ "pdf", "jpg", "jpeg","png" })
            @FileSize(max = 2, unit = FileSize.Unit.MB)
            MultipartFile identityDocument,

            @FileType({ "pdf", "jpg", "jpeg","png" })
            @FileSize(max = 2, unit = FileSize.Unit.MB)
            MultipartFile certificate,

            @FileType({ "pdf", "jpg", "jpeg","png" })
            @FileSize(max = 2, unit = FileSize.Unit.MB)
            MultipartFile signedDocument,

            @FileType({ "pdf", "jpg", "jpeg","png" })
            @FileSize(max = 2, unit = FileSize.Unit.MB)
            MultipartFile paymentReceipt
            ) {
        validateRegisterDto(dto);
        var userDocuments = documentsService.uploadUserDocuments(identityDocument, certificate, signedDocument, paymentReceipt);
        try {
            var user = buildUser(dto);
            user.setRoles(Set.of(getAuthority(UserRoles.USER)));
            user.setEnabled(false);
            userRepository.save(user);

            userDocumentsRepository.save(buildIdentityDocument(userDocuments.getFirst(), user));
            certificatesRepository.save(buildCertificate(userDocuments.get(1), user));
            signedDocumentsRepository.save(buildSignedDocument(userDocuments.get(2), user));
            paymentReciptRepository.save(buildPaymentReceipt(userDocuments.getLast(), user));


            emailService.sendEmail(
                    user.getEmail(),
                    EmailMessages.getRegistrationReceivedSubject(),
                    EmailMessages.getRegistrationReceivedMessage(user.getName()));

        } catch (Exception e) {
            documentsService.rollback(userDocuments);
            throw new ClientException("Error al registrar el usuario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void registerAdmin(@Valid UserRegisterDto dto) {
        var user = buildUser(dto);
        user.setRoles(Set.of(getAuthority(UserRoles.ADMIN)));
        user.setEnabled(true);
        userRepository.save(user);
    }

    private Authorities getAuthority(UserRoles role) {
        return authorityRepository.findByAuthority(role)
                .orElseThrow(() -> new ClientException("Rol no encontrado", HttpStatus.NOT_FOUND));
    }

    private void validateRegisterDto(@Valid UserRegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new ClientException("El nombre de usuario ya se encuentra registrado", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ClientException("Email ya se encuentra registrado", HttpStatus.BAD_REQUEST);
        }
        if (isPasswordInvalid(dto.getPassword())) {
            throw new ClientException(
                    "La contraseña debe ser entre 8 a 20 digitos y contener al menos una letra mayuscula, minuscula, un digito y un caracter especial",
                    HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByIdentification(dto.getIdentification())) {
            throw new ClientException("La identificación ya se encuentra registrada", HttpStatus.BAD_REQUEST);
        }
        if (dto.getIdType() != IdTypes.PASAPORTE && !validateEcuadorianId(dto.getIdentification())) {
            throw new ClientException("El número de cédula proporcionado no es válido", HttpStatus.BAD_REQUEST);
        }
    }

    private AppUser buildUser(@Valid UserRegisterDto dto) {
        return AppUser.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .address(dto.getAddress())
                .name(dto.getName())
                .lastname(dto.getLastname())
                .idType(dto.getIdType())
                .identification(dto.getIdentification())
                .phone(dto.getPhone())
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
    }

    public boolean isPasswordInvalid(String password) {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 20),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()));
        RuleResult result = validator.validate(new PasswordData(password));
        return !result.isValid();
    }

    private IdentityDocuments buildIdentityDocument(UploadFileInfo fileInfo, AppUser appUser) {
        return IdentityDocuments.builder()
                .personalIdUrl(fileInfo.getSecureUrl())
                .publicId(fileInfo.getPublicId())
                .fileType(fileInfo.getResourceType())
                .appUser(appUser)
                .build();
    }

    private Certificates buildCertificate(UploadFileInfo fileInfo, AppUser appUser) {
        return Certificates.builder()
                .certificatesUrl(fileInfo.getSecureUrl())
                .publicId(fileInfo.getPublicId())
                .fileType(fileInfo.getResourceType())
                .appUser(appUser)
                .build();
    }

    private static boolean validateEcuadorianId(String idNumber) {
        // Check if it's null or empty
        if (idNumber == null || idNumber.trim().isEmpty()) {
            return false;
        }

        // Remove spaces and hyphens if present
        idNumber = idNumber.replaceAll("[\\s-]", "");

        // Check if it has exactly 10 digits
        if (idNumber.length() != 10) {
            return false;
        }

        // Check if all characters are digits
        if (!idNumber.matches("\\d{10}")) {
            return false;
        }

        // Convert to integer array
        int[] digits = new int[10];
        for (int i = 0; i < 10; i++) {
            digits[i] = Character.getNumericValue(idNumber.charAt(i));
        }

        // Check if first two digits correspond to a valid province (01-24)
        int province = digits[0] * 10 + digits[1];
        if (province < 1 || province > 24) {
            return false;
        }

        // Check if third digit is less than 6 (for natural persons)
        if (digits[2] >= 6) {
            return false;
        }

        // Apply validation algorithm (modulo 10)
        int sum = 0;
        int[] coefficients = { 2, 1, 2, 1, 2, 1, 2, 1, 2 };

        for (int i = 0; i < 9; i++) {
            int value = digits[i] * coefficients[i];
            if (value >= 10) {
                value = value - 9; // If greater or equal to 10, subtract 9
            }
            sum += value;
        }

        // Calculate check digit
        int checkDigit = 10 - (sum % 10);
        if (checkDigit == 10) {
            checkDigit = 0;
        }

        // Compare with the last digit of the ID
        return checkDigit == digits[9];
    }

    public void updateUser(UpdateUserDto data, Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ClientException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        if (data.getEmail() != null && !data.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(data.getEmail())) {
                throw new ClientException("El email ya se encuentra registrado", HttpStatus.BAD_REQUEST);
            }
            user.setEmail(data.getEmail());
        }

        if (data.getPhone() != null) {
            user.setPhone(data.getPhone());
        }

        if (data.getAddress() != null) {
            user.setAddress(data.getAddress());
        }


        userRepository.save(user);

    }


    private SignedDocuments buildSignedDocument(UploadFileInfo fileInfo, AppUser appUser) {
        return SignedDocuments.builder()
                .signedUrl(fileInfo.getSecureUrl())
                .publicId(fileInfo.getPublicId())
                .fileType(fileInfo.getResourceType())
                .appUser(appUser)
                .build();
    }

    private PaymentReceipt buildPaymentReceipt(UploadFileInfo fileInfo, AppUser appUser) {
        return PaymentReceipt.builder()
                .paymentUrl(fileInfo.getSecureUrl())
                .publicId(fileInfo.getPublicId())
                .fileType(fileInfo.getResourceType())
                .appUser(appUser)
                .build();
    }


    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .totalUsers(userRepository.count())
                .pendingUsers(userRepository.countByEnabled(false))
                .approvedUsers(userRepository.countByEnabled(true))
                .build();
    }



    
}
