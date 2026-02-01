package com.app.municipio.modules.business.dto.Request;

import com.app.municipio.modules.business.models.enums.DeliveryService;
import com.app.municipio.modules.business.models.enums.SalePlace;
import com.app.municipio.modules.business.models.BusinessCategory;
import com.app.municipio.modules.photos.models.Photo;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({
    "commercialName",
    "description",
    "categoryId",
    "deliveryService",
    "salePlace",
    "phone",
    "acceptsWhatsappOrders",
    "whatsappNumber",
    "address",
    "googleMapsCoordinates",
    "schedules",
    "facebook",
    "instagram",
    "tiktok",
    "website",
    "logoFile",
    "carouselFiles"
})
public class BusinessFullUpdateDto {
    private String commercialName;
    private String description;
    private Long categoryId;
    private DeliveryService deliveryService;
    private SalePlace salePlace;
    private String phone;
    private Boolean acceptsWhatsappOrders;
    private String whatsappNumber;
    private String address;
    private String googleMapsCoordinates;
    private List<String> schedules;
    private String facebook;
    private String instagram;
    private String tiktok;
    private String website;

    @Schema(description = "Logo del negocio", type = "string", format = "binary")
    private MultipartFile logoFile;

    @ArraySchema(arraySchema = @Schema(description = "Fotos del carrusel"), schema = @Schema(type = "string", format = "binary"))
    private MultipartFile[] carouselFiles;
}
