package com.app.municipio.modules.business.dto.Responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessCategorySelectDTO {
    private Long id;
    private String name;
}
