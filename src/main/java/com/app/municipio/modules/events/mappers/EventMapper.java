package com.app.municipio.modules.events.mappers;

import com.app.municipio.modules.events.dto.Responses.EventResponseDto;
import com.app.municipio.modules.events.models.Event;

import java.util.Collections;
import java.util.List;

public class EventMapper {

    private EventMapper() {}

    public static EventResponseDto toDto(Event event) {
        if (event == null) return null;

        List<EventResponseDto.ContactDto> contacts = event.getContact() == null
                ? Collections.emptyList()
                : event.getContact().stream()
                .map(c -> EventResponseDto.ContactDto.builder()
                        .type(c.getType())
                        .description(c.getDescription())
                        .build())
                .toList();

        List<String> services = event.getServices() == null
                ? Collections.emptyList()
                : event.getServices().stream()
                .map(s -> s.getService())
                .toList();

        return EventResponseDto.builder()
                .id(event.getId())
                .name(event.getName())
                .type(event.getType())
                .dateStart(event.getDateStart())
                .dateEnd(event.getDateEnd())
                .description(event.getDescription())
                .direction(event.getDirection())
                .location(event.getLocation())
                .contact(contacts)
                .services(services)
                .link(event.getLink())
                .state(event.getState())
                .build();
    }
}
