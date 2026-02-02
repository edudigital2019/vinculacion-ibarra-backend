package com.app.municipio.modules.events.services;

import com.app.municipio.application.cloudinary.CloudinaryService;
import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.modules.events.dto.Request.CreateEventDto;
import com.app.municipio.modules.events.dto.Request.UpdateEventDto;
import com.app.municipio.modules.events.dto.Responses.EventResponseDto;
import com.app.municipio.modules.events.mappers.EventMapper;
import com.app.municipio.modules.events.models.Event;
import com.app.municipio.modules.events.models.EventContact;
import com.app.municipio.modules.events.models.EventImage;
import com.app.municipio.modules.events.models.EventServiceEntity;
import com.app.municipio.modules.events.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final CloudinaryService cloudinaryService;

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
        return EventMapper.toDto(getEntityById(id));
    }

    // ==========================
    // CREATE (JSON-only)
    // ==========================
    @Transactional
    public EventResponseDto create(CreateEventDto dto) {
        validateDates(dto.getDateStart(), dto.getDateEnd());

        Event event = buildBaseEventFromCreate(dto);

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

    // ==========================
    // CREATE (JSON + imágenes)
    // ==========================
    @Transactional
    public EventResponseDto create(CreateEventDto dto, List<MultipartFile> images, List<String> names) {
        validateDates(dto.getDateStart(), dto.getDateEnd());

        Event event = buildBaseEventFromCreate(dto);

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

        // Guardar evento primero
        Event saved = eventRepository.save(event);

        // Subir imágenes a Cloudinary
        if (images != null && !images.isEmpty()) {
            String folder = "events/" + saved.getId(); // ordenado por evento

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file == null || file.isEmpty()) continue;

                String logicalName = resolveLogicalName(file, names, i);

                var upload = uploadToCloudinary(file, folder); // [secureUrl, publicId, resourceType]

                EventImage img = EventImage.builder()
                        .name(logicalName)
                        .url(upload.secureUrl())
                        .publicId(upload.publicId())
                        .resourceType(upload.resourceType())
                        .event(saved)
                        .build();

                saved.getImages().add(img);
            }

            saved = eventRepository.save(saved);
        }

        return EventMapper.toDto(saved);
    }

    // ==========================
    // UPDATE (JSON-only)
    // ==========================
    @Transactional
    public EventResponseDto update(Long id, UpdateEventDto dto) {
        Event event = getEntityById(id);

        applyUpdateFields(event, dto);

        Event saved = eventRepository.save(event);
        return EventMapper.toDto(saved);
    }

    // ==========================
    // UPDATE (JSON + imágenes) - add / replace
    // ==========================
    @Transactional
    public EventResponseDto update(Long id, UpdateEventDto dto, List<MultipartFile> images, List<String> names, boolean replaceImages) {
        Event event = getEntityById(id);

        applyUpdateFields(event, dto);

        // Si reemplaza, primero borrar en Cloudinary las anteriores (recomendado)
        if (replaceImages) {
            for (EventImage img : event.getImages()) {
                if (img.getPublicId() != null && !img.getPublicId().isBlank()) {
                    try {
                        cloudinaryService.deleteFile(img.getPublicId(), img.getResourceType());
                    } catch (IOException e) {
                        // Puedes decidir si esto debe bloquear o solo loguear
                        throw new ClientException("Error deleting old image in Cloudinary: " + e.getMessage(),
                                HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
            }
            event.getImages().clear(); // orphanRemoval = true elimina de BD
        }

        // Agregar nuevas imágenes si vienen
        if (images != null && !images.isEmpty()) {
            String folder = "events/" + event.getId();

            for (int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);
                if (file == null || file.isEmpty()) continue;

                String logicalName = resolveLogicalName(file, names, i);
                var upload = uploadToCloudinary(file, folder);

                EventImage img = EventImage.builder()
                        .name(logicalName)
                        .url(upload.secureUrl())
                        .publicId(upload.publicId())
                        .resourceType(upload.resourceType())
                        .event(event)
                        .build();

                event.getImages().add(img);
            }
        }

        Event saved = eventRepository.save(event);
        return EventMapper.toDto(saved);
    }

    public void delete(Long id) {
        Event event = getEntityById(id);

        // (opcional recomendado) borrar imágenes en Cloudinary al borrar evento
        for (EventImage img : event.getImages()) {
            if (img.getPublicId() != null && !img.getPublicId().isBlank()) {
                try {
                    cloudinaryService.deleteFile(img.getPublicId(), img.getResourceType());
                } catch (IOException e) {
                    throw new ClientException("Error deleting event images in Cloudinary: " + e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }

        eventRepository.delete(event);
    }

    // ==========================
    // Helpers
    // ==========================
    private Event buildBaseEventFromCreate(CreateEventDto dto) {
        return Event.builder()
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
    }

    private void applyUpdateFields(Event event, UpdateEventDto dto) {
        if (dto.getName() != null) event.setName(dto.getName());
        if (dto.getType() != null) event.setType(dto.getType());
        if (dto.getDescription() != null) event.setDescription(dto.getDescription());
        if (dto.getDirection() != null) event.setDirection(dto.getDirection());
        if (dto.getLocation() != null) event.setLocation(dto.getLocation());
        if (dto.getLink() != null) event.setLink(dto.getLink());
        if (dto.getState() != null) event.setState(dto.getState());

        LocalDate finalStart = dto.getDateStart() != null ? dto.getDateStart() : event.getDateStart();
        LocalDate finalEnd = dto.getDateEnd() != null ? dto.getDateEnd() : event.getDateEnd();
        validateDates(finalStart, finalEnd);

        if (dto.getDateStart() != null) event.setDateStart(dto.getDateStart());
        if (dto.getDateEnd() != null) event.setDateEnd(dto.getDateEnd());

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
    }

    private String resolveLogicalName(MultipartFile file, List<String> names, int index) {
        if (names != null && index < names.size() && names.get(index) != null && !names.get(index).isBlank()) {
            return names.get(index).trim();
        }
        String original = file.getOriginalFilename();
        return (original != null && !original.isBlank()) ? original : "image_" + (index + 1);
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ClientException("dateEnd cannot be before dateStart", HttpStatus.BAD_REQUEST);
        }
    }

    // ---- Cloudinary wrapper para no manejar List<String> por todo lado
    private Uploaded uploadToCloudinary(MultipartFile file, String folder) {
        try {
            List<String> res = cloudinaryService.uploadFile(file, folder);
            // res: [secureUrl, publicId, resourceType]
            return new Uploaded(res.get(0), res.get(1), res.get(2));
        } catch (IOException e) {
            throw new ClientException("Error uploading file to Cloudinary: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private record Uploaded(String secureUrl, String publicId, String resourceType) {}
}
