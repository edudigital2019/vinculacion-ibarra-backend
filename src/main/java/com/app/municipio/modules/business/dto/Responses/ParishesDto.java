package com.app.municipio.modules.business.dto.Responses;

import com.app.municipio.modules.business.models.enums.ParishType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParishesDto {
    private Long id;
    private String name;
    private ParishType type;
}
