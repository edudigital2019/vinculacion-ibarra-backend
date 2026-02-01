package com.app.municipio.modules.users.dto;

import lombok.Data;

@Data
public class UpdateUserDto {
    private String phone;
    private String email;
    private String address;
    private String username;
}
