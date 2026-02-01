package com.app.municipio.modules.business.dto.Request;

import io.github.luidmidev.jakarta.validations.PhoneNumber;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class CreateNewBusinessDTO {

    @NotNull
    private Long categoryId;

    @NotNull
    @NotEmpty
    @Length(min = 1, max = 50)
    private String commercialName;

    @PhoneNumber
    private String phone;

    @NotNull
    @NotEmpty
    @Length(min = 2, max = 100)
    private String parishCommunitySector;

    private String facebook;
    private String instagram;
    private String tiktok;
    private String website;

    @Length(min = 1, max = 200)
    private String description;

    @NotNull(message = "acceptsWhatsappOrders cannot be null")
    private Boolean acceptsWhatsappOrders;

    private String whatsappNumber;

    @NotNull
    @NotEmpty
    @Pattern(regexp = "NO|SI|BAJO_PEDIDO", message = "deliveryService debe ser NO, SI o BAJO_PEDIDO")
    private String deliveryService;

    @NotNull
    @NotEmpty
    @Pattern(regexp = "NO|FERIAS|LOCAL_FIJO", message = "salePlace debe ser NO, FERIAS o LOCAL_FIJO")
    private String salePlace;


    @NotNull(message = "receivedUdelSupport cannot be null")
    private Boolean receivedUdelSupport;

    @NotNull
    private LocalDate registrationDate;

    @NotNull
    @NotEmpty
    private String address;

    private String googleMapsCoordinates;

    private String schedules;

    @NotNull
    private Long parishId;
}

