package com.app.municipio.modules.business.dto.Responses;

import com.app.municipio.modules.business.models.enums.DeliveryService;
import com.app.municipio.modules.business.models.enums.SalePlace;
import com.app.municipio.modules.photos.dto.Response.PhotoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessPublicDTO {
    private Long id;

    private String commercialName;
    private String description;

    private String phone;
    private String whatsappNumber;

    private String facebook;
    private String instagram;
    private String tiktok;
    private String website;

    private String address;
    private String parishCommunitySector;
    private String googleMapsCoordinates;

    private String logoUrl;
    private List<PhotoDTO> photos;

    private List<String> schedules;

    private Boolean acceptsWhatsappOrders;
    private DeliveryService deliveryService;
    private SalePlace salePlace;

    private CategoryBusinessDTO category;
}
