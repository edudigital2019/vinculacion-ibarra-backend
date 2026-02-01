package com.app.municipio.modules.business.dto.Responses;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessUpdateDto {
     private String commercialName;
    private String description;
    private String facebook;
    private String instagram;
    private String tiktok;
    private String website;
    private String phone;
    private String email;
    private Boolean acceptsWhatsappOrders;
    private String whatsappNumber;
    private String address;
    private String googleMapsCoordinates;
    private List<String> schedules;
}
