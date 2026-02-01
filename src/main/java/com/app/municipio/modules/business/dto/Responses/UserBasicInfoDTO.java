package com.app.municipio.modules.business.dto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserBasicInfoDTO {
    private Long id;
    private String name;
    private String email;
    private String identification;
}