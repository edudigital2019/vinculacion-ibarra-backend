package com.app.municipio.modules.events.dto.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateEventDto {

    @NotBlank
    @Schema(example = "Nombre del evento")
    private String name;

    @NotBlank
    @Schema(example = "Tipo del evento")
    private String type;

    @NotNull
    @Schema(example = "2026-02-15")
    private LocalDate dateStart;

    @NotNull
    @Schema(example = "2026-02-20")
    private LocalDate dateEnd;

    @Schema(example = "Descripción del evento.")
    private String description;

    @Schema(example = "Dirección del evento")
    private String direction;

    @Schema(example = "Ciudad o localización")
    private String location;

    @Valid
    private List<ContactDto> contact;

    private List<String> services;

    @Schema(example = "https://ejemplo.com/evento")
    private String link;

    @NotNull
    private Boolean state;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ContactDto {
        @NotBlank
        private String type;

        @NotBlank
        private String description;
    }
}
