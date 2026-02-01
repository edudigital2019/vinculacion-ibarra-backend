package com.app.municipio.modules.business.dto;


import com.app.municipio.modules.business.models.enums.ValidationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessPending {
    private String ownerName;
    private String identification;
    private String phone;
    private List<BusinessByOwner> business;

    @Builder
    @Data
    public static class BusinessByOwner {
        private Long id;
        private String businessName;
        private ValidationStatus status;
    }
}
