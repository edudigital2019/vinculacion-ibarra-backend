package com.app.municipio.modules.business.dto.Responses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessResponseDto {
    // Información básica
    private String commercialName;
    private String description;
    private String facebook;
    private String instagram;
    private String tiktok;
    private String website;
    private String phone;
    private Boolean acceptsWhatsappOrders;
    private String whatsappNumber;
    private String address;
    private String googleMapsCoordinates;
    private List<String> schedules;

    // Fotos
    private String logoUrl;            // URL del logo
    private List<String> carouselUrls; // URLs del carrusel
}