package com.app.municipio.modules.business.dto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryBusinessDTO {
    private Long id;
    private String name;
    private String description;
}
