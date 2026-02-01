package com.app.municipio.modules.business.dto.Responses;

import com.app.municipio.modules.business.models.enums.PromoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessPromoDto {
    private Long idBusinessPromo;
    private Long businessId;
    private String businessName;
    private PromoType tipoPromocion;
    private String tituloPromocion;
    private LocalDate fechaPromoInicio;
    private LocalDate fechaPromoFin;
    private String businessImageUrl;
    private String condiciones;
}
