package com.app.municipio.modules.users.dto;

import com.app.municipio.modules.users.models.enums.IdTypes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @NotNull
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank
    @NotNull
    @Size(min = 2, max = 50)
    private String lastname;


   private IdTypes idType;

    @NotBlank
    @NotNull
    @Size(min = 2, max = 15)
    private String identification;

    @NotBlank
    @NotNull
    private String phone;

    @NotBlank
    @NotNull
    private String address;


    @NotBlank
    @NotNull
    @Size(min = 5, max = 20)
    private String username;

    @NotBlank
    @NotNull
    @Size(min = 8, max = 20)
    private String password;

}
