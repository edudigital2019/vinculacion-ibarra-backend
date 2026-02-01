package com.app.municipio.modules.business.dto.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessStatsDTO {
    private long totalBusinesses;
    private long pendingBusinesses;
    private long approvedBusinesses;
    private long rejectedBusinesses;
}
