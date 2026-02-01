package com.app.municipio.modules.business.dto.Request;

import com.app.municipio.modules.business.models.enums.PromoType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBusinessPromoDto {

    @NotNull
    private Long businessId;

    @NotNull
    @Schema(description = "Tipo de promoción disponible",
            allowableValues = {"DESCUENTO_PORCENTAJE", "DESCUENTO_FIJO", "DOSXUNO", "COMBO"})
    private PromoType tipoPromocion;

    @NotNull
    @NotEmpty
    @Length(min = 1, max = 50)
    private String tituloPromocion;

    @NotNull
    private LocalDate fechaPromoInicio;

    @NotNull
    private LocalDate fechaPromoFin;

    @Length(min = 1, max = 50)
    private String condiciones;

}
