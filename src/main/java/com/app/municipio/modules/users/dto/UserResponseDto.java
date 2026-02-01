package com.app.municipio.modules.users.dto;

import com.app.municipio.modules.users.models.enums.IdTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String name;
    private String lastname;
    private String phone;
    private String address;
    private String username;
    private String email;
    private IdTypes idType;
    private String identification;
    private boolean enabled;
    private LocalDateTime registrationDate;
    
}