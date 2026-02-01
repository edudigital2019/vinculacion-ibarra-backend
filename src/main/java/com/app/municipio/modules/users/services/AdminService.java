package com.app.municipio.modules.users.services;

import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.modules.users.dto.Request.UserApprovalRequest;
import com.app.municipio.modules.users.dto.Responses.PendingUserResponse;
import com.app.municipio.modules.users.dto.UserResponseDto;
import com.app.municipio.modules.users.dto.WhoAmIDto;
import com.app.municipio.modules.users.models.AppUser;
import com.app.municipio.modules.users.repositories.UsersRepository;
import com.app.municipio.utils.EmailMessages;
import com.app.municipio.utils.EmailService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Log4j2
@Validated
@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
    private final UsersRepository usersRepository;
    private final EmailService emailService;
    private final DocumentsService documentsService;

     public Page<PendingUserResponse> getPendingUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppUser> usersPage = usersRepository.findByEnabled(false, pageable);
        
        return usersPage.map(user -> new PendingUserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getIdentification()
        ));
    }
    public void processUserApproval(UserApprovalRequest request) {
        AppUser user = usersRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (user.isEnabled()) {
            throw new ClientException("Solo se pueden procesar usuarios pendientes", HttpStatus.BAD_REQUEST);
        }

        if (request.approve()) {
            user.setEnabled(true);
            usersRepository.save(user);

            emailService.sendEmail(
                    user.getEmail(),
                    EmailMessages.getStatusSubject("Aprobada"),
                    EmailMessages.getApprovalMessage(user.getName())
            );

        } else {
            if (request.rejectionReason() == null || request.rejectionReason().isBlank()) {
                throw new ClientException("La razón de rechazo es obligatoria", HttpStatus.BAD_REQUEST);
            }
            documentsService.rollbackFiles(user.getId());
            deleteUser(user);
            emailService.sendEmail(
                    user.getEmail(),
                    EmailMessages.getStatusSubject("Rechazada"),
                    EmailMessages.getRejectionMessage(user.getName(), request.rejectionReason())
            );
        }
    }

    private void deleteUser(AppUser user) {
         usersRepository.delete(user);
    }
    public WhoAmIDto getUserDetailsById(Long id) {
        AppUser user = usersRepository.findById(id)
                .orElseThrow(() -> new ClientException("Usuario no encontrado", HttpStatus.NOT_FOUND));
        return mapToDto(user);
    }
    //Metodos nuevos
    //---Encontrar por identificacion
    public Optional<UserResponseDto> findUserByIdentification(String identification) {
        return usersRepository.findByIdentification(identification)
                .map(this::mapToUserResponseDto);
    }
    //---Encontrar por nombre o identificacion
    public Page<UserResponseDto> searchUsersByNameOrIdentification(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AppUser> usersPage = usersRepository.findByNameOrIdentification(searchTerm, pageable);
        
        return usersPage.map(this::mapToUserResponseDto);
    }
    //---Obtener usuarios por estado 
    public Page<UserResponseDto> getUsersByStatus(boolean enabled, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<AppUser> usersPage = usersRepository.findByEnabled(enabled, pageable);
    
    return usersPage.map(this::mapToUserResponseDto);
    }
    //--Obtener todos los usuarios
    public Page<AppUser> getAllUsers(Pageable pageable) {
    return usersRepository.findAll(pageable);
    }
   //Mapeadores
    private WhoAmIDto mapToDto(AppUser user) {
        var roles = user.getRoles().stream().map(role -> role.getAuthority().name()).toList();
        return WhoAmIDto.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .username(user.getUsername())
                .identification(user.getIdentification())
                .enabled(user.isEnabled())
                .roles(roles)
                .build();
    }
     private UserResponseDto mapToUserResponseDto(AppUser user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .phone(user.getPhone())
                .address(user.getAddress())
                .username(user.getUsername())
                .email(user.getEmail())
                .idType(user.getIdType())
                .identification(user.getIdentification())
                .enabled(user.isEnabled())
                .build();
    }
}

