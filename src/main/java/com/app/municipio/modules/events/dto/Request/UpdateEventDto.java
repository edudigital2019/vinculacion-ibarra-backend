package com.app.municipio.modules.events.dto.Request;

import jakarta.validation.Valid;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateEventDto {

    private String name;
    private String type;
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private String description;
    private String direction;
    private String location;

    @Valid
    private List<CreateEventDto.ContactDto> contact;

    private List<String> services;

    private String link;
    private Boolean state;
}
