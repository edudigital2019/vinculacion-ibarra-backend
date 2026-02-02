package com.app.municipio.modules.events.dto.Responses;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventResponseDto {
    private Long id;
    private String name;
    private String type;
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private String description;
    private String direction;
    private String location;
    private List<ContactDto> contact;
    private List<String> services;
    private String link;
    private Boolean state;

    // NUEVO: imágenes relacionadas al evento
    private List<ImageDto> images;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ContactDto {
        private String type;
        private String description;
    }

    // NUEVO DTO para imágenes
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ImageDto {
        private String name; // ej: banner, galeria1
        private String url;  // URL de Cloudinary
    }
}
