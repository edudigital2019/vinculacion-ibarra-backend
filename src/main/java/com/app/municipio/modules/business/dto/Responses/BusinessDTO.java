package com.app.municipio.modules.business.dto.Responses;

import com.app.municipio.modules.business.models.enums.DeliveryService;
import com.app.municipio.modules.business.models.enums.SalePlace;
import com.app.municipio.modules.business.models.enums.ValidationStatus;
import com.app.municipio.modules.photos.dto.Response.PhotoDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessDTO {
    private Long id;

    private String commercialName;

    private String phone;

    private String parishCommunitySector;

    private String facebook;

    private String instagram;

    private String tiktok;

    private String website;

    private String description;

    private Boolean acceptsWhatsappOrders;

    private DeliveryService deliveryService;

    private SalePlace salePlace;

    private Boolean receivedUdelSupport;

    private String udelSupportDetails;

    private LocalDate registrationDate;

    private ValidationStatus validationStatus;

    private UserBasicInfoDTO user;

    private CategoryBusinessDTO category;

    private String address;

    private String googleMapsCoordinates;

    private List<String> schedules;

    private String rejectionReason;

    private List<PhotoDTO> photos;

}

