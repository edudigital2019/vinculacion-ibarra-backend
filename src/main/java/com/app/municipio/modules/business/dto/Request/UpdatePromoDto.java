package com.app.municipio.modules.business.dto.Request;

import com.app.municipio.modules.business.models.enums.PromoType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
public class UpdatePromoDto {
    @Length(min = 1, max = 50)
    private String titlePromotion;
    @NotNull
    @Schema(description = "Tipo de promoción disponible",
            allowableValues = {"DESCUENTO_PORCENTAJE", "DESCUENTO_FIJO", "DOSXUNO", "COMBO"})
    private PromoType promoType;
    private String conditions;
    private LocalDate datePromoStart;
    private LocalDate datePromoEnd;
}
