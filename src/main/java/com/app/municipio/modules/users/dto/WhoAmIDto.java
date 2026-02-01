package com.app.municipio.modules.users.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WhoAmIDto {
    private Long id;
    private String name;
    private String lastname;
    private String email;
    private String identification;
    private String phone;
    private String address;
    private String username;
    private boolean enabled;
    private List<String> roles;
}