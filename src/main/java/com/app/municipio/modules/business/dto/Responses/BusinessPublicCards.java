package com.app.municipio.modules.business.dto.Responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessPublicCards {
    private Long id;

    private String commercialName;

    private String representativeName;

    private String address;

    private String description;

    private String phone;

    private String logoUrl;
}
