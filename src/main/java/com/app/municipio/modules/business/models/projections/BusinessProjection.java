package com.app.municipio.modules.business.models.projections;

import com.app.municipio.modules.business.models.enums.ValidationStatus;

public interface BusinessProjection {
    Long getId();
    String getCommercialName();
    ValidationStatus getStatus();
    Long getUserId();
}
