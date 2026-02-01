package com.app.municipio.modules.events.services;

import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.modules.events.dto.Request.CreateEventDto;
import com.app.municipio.modules.events.dto.Request.UpdateEventDto;
import com.app.municipio.modules.events.models.Event;
import com.app.municipio.modules.events.models.EventContact;
import com.app.municipio.modules.events.models.EventServiceEntity;
import com.app.municipio.modules.events.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.municipio.modules.events.dto.Responses.EventResponseDto;
import com.app.municipio.modules.events.mappers.EventMapper;
import java.util.List;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<EventResponseDto> listAll() {
    return eventRepository.findAllWithRelations()
            .stream()
            .map(EventMapper::toDto)
            .toList();
}

private Event getEntityById(Long id) {
    return eventRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new ClientException("Event not found", HttpStatus.NOT_FOUND));
}

    public EventResponseDto getById(Long id) {
    var event = eventRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new ClientException("Event not found", HttpStatus.NOT_FOUND));
    return EventMapper.toDto(getEntityById(id));
}

    @Transactional
public EventResponseDto create(CreateEventDto dto) {
    validateDates(dto.getDateStart(), dto.getDateEnd());

    Event event = Event.builder()
            .name(dto.getName())
            .type(dto.getType())
            .dateStart(dto.getDateStart())
            .dateEnd(dto.getDateEnd())
            .description(dto.getDescription())
            .direction(dto.getDirection())
            .location(dto.getLocation())
            .link(dto.getLink())
            .state(dto.getState() != null ? dto.getState() : Boolean.TRUE)
            .build();

    // Contactos
    if (dto.getContact() != null) {
        for (CreateEventDto.ContactDto c : dto.getContact()) {
            EventContact contact = EventContact.builder()
                    .type(c.getType())
                    .description(c.getDescription())
                    .event(event)
                    .build();
            event.getContact().add(contact);
        }
    }

    // Servicios
    if (dto.getServices() != null) {
        for (String s : dto.getServices()) {
            EventServiceEntity service = EventServiceEntity.builder()
                    .service(s)
                    .event(event)
                    .build();
            event.getServices().add(service);
        }
    }

    Event saved = eventRepository.save(event);
    return EventMapper.toDto(saved);
}

@Transactional
public EventResponseDto update(Long id, UpdateEventDto dto) {
    Event event = getEntityById(id);

    if (dto.getName() != null) event.setName(dto.getName());
    if (dto.getType() != null) event.setType(dto.getType());
    if (dto.getDescription() != null) event.setDescription(dto.getDescription());
    if (dto.getDirection() != null) event.setDirection(dto.getDirection());
    if (dto.getLocation() != null) event.setLocation(dto.getLocation());
    if (dto.getLink() != null) event.setLink(dto.getLink());
    if (dto.getState() != null) event.setState(dto.getState());

    var finalStart = dto.getDateStart() != null ? dto.getDateStart() : event.getDateStart();
    var finalEnd = dto.getDateEnd() != null ? dto.getDateEnd() : event.getDateEnd();
    validateDates(finalStart, finalEnd);

    if (dto.getDateStart() != null) event.setDateStart(dto.getDateStart());
    if (dto.getDateEnd() != null) event.setDateEnd(dto.getDateEnd());

    // Reemplazar contactos si vienen
    if (dto.getContact() != null) {
        event.getContact().clear();
        for (CreateEventDto.ContactDto c : dto.getContact()) {
            EventContact contact = EventContact.builder()
                    .type(c.getType())
                    .description(c.getDescription())
                    .event(event)
                    .build();
            event.getContact().add(contact);
        }
    }

    // Reemplazar servicios si vienen
    if (dto.getServices() != null) {
        event.getServices().clear();
        for (String s : dto.getServices()) {
            EventServiceEntity service = EventServiceEntity.builder()
                    .service(s)
                    .event(event)
                    .build();
            event.getServices().add(service);
        }
    }

    Event saved = eventRepository.save(event);
    return EventMapper.toDto(saved);
}


        public void delete(Long id) {
        Event event = getEntityById(id);
        eventRepository.delete(event);
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ClientException("dateEnd cannot be before dateStart", HttpStatus.BAD_REQUEST);
        }
    }
}
