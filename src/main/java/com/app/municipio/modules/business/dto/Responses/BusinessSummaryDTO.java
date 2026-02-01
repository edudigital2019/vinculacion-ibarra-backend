package com.app.municipio.modules.business.dto.Responses;

import com.app.municipio.modules.business.models.enums.ValidationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessSummaryDTO {
    private Long id;
    private String commercialName;
    private String representativeName; // nombre del representante
    private String address;
    private String description;
    private String phone;
    private String logoUrl;
    private ValidationStatus validationStatus;
}
