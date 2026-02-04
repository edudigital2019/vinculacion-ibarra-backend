package com.app.municipio.modules.events.controllers;

import com.app.municipio.application.exceptions.ApResponse;
import com.app.municipio.application.utils.Response;
import com.app.municipio.modules.events.dto.Request.CreateEventDto;
import com.app.municipio.modules.events.dto.Request.UpdateEventDto;
import com.app.municipio.modules.events.services.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Tag(name = "Events", description = "Endpoints para la gestión de eventos")
public class EventController {

    private final EventService eventService;
    private final ObjectMapper objectMapper;

@Operation(summary = "Listar eventos")
@GetMapping
public ResponseEntity<ApResponse> listAll() {
    try {
        var events = eventService.listAll();

        return Response.successWithTotal(
                "Events listed successfully",
                events,
                events.size()
        );

    } catch (Exception e) {
        return Response.badRequest("Error listing events: " + e.getMessage());
    }
}



    @Operation(summary = "Obtener evento por id")
    @GetMapping("/{id}")
    public ResponseEntity<ApResponse> getById(@PathVariable Long id) {
        try {
            return Response.success("Event found", eventService.getById(id));
        } catch (Exception e) {
            return Response.badRequest("Error getting event: " + e.getMessage());
        }
    }

    // ==========================
    // CREATE (JSON-only) - legacy
    // ==========================
    @Operation(summary = "Crear evento (JSON)")
    @PostMapping("/create")
    public ResponseEntity<ApResponse> create(@Valid @RequestBody CreateEventDto dto) {
        try {
            var created = eventService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Response.success("Event created successfully", created).getBody());
        } catch (Exception e) {
            return Response.badRequest("Error creating event: " + e.getMessage());
        }
    }

    // ==========================
    // CREATE (JSON + imágenes)
    // ==========================
    @Operation(summary = "Crear evento (JSON + varias imágenes)")
    
    
@PostMapping(value = "/create-with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ApResponse> createWithImages(
        @RequestParam("data") String data,
        @RequestParam(value = "images", required = false) List<MultipartFile> images,
        @RequestParam(value = "names", required = false) List<String> names
) {
    try {
        CreateEventDto dto = objectMapper.readValue(data, CreateEventDto.class);
        var created = eventService.create(dto, images, names);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success("Event created successfully", created).getBody());
    } catch (Exception e) {
        return Response.badRequest("Error creating event: " + e.getMessage());
    }
}

    // ==========================
    // UPDATE (JSON-only) - legacy
    // ==========================
    @Operation(summary = "Actualizar evento (JSON)")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateEventDto dto) {
        try {
            var updated = eventService.update(id, dto);
            return Response.success("Event updated successfully", updated);
        } catch (Exception e) {
            return Response.badRequest("Error updating event: " + e.getMessage());
        }
    }

    // ==========================
    // UPDATE (JSON + imágenes)
    // ==========================
    @Operation(summary = "Actualizar evento (JSON + imágenes) - agregar o reemplazar")
    
    @PutMapping(value = "/update-with-images/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ApResponse> updateWithImages(
        @PathVariable Long id,
        @RequestParam("data") String data,
        @RequestParam(value = "images", required = false) List<MultipartFile> images,
        @RequestParam(value = "names", required = false) List<String> names,
        @RequestParam(value = "replaceImages", defaultValue = "false") boolean replaceImages
) {
    try {
        UpdateEventDto dto = objectMapper.readValue(data, UpdateEventDto.class);

        var updated = eventService.update(id, dto, images, names, replaceImages);
        return Response.success("Event updated successfully", updated);
    } catch (Exception e) {
        return Response.badRequest("Error updating event: " + e.getMessage());
    }
}

    @Operation(summary = "Eliminar evento")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApResponse> delete(@PathVariable Long id) {
        try {
            eventService.delete(id);
            return Response.success("Event deleted successfully");
        } catch (Exception e) {
            return Response.badRequest("Error deleting event: " + e.getMessage());
        }
    }
}
