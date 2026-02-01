package com.app.municipio.modules.events.controllers;

import com.app.municipio.application.exceptions.ApResponse;
import com.app.municipio.application.utils.Response;
import com.app.municipio.modules.events.dto.Request.CreateEventDto;
import com.app.municipio.modules.events.dto.Request.UpdateEventDto;
import com.app.municipio.modules.events.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Tag(name = "Events", description = "Endpoints para la gestión de eventos")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "Listar eventos")
    @GetMapping
    public ResponseEntity<ApResponse> listAll() {
        try {
            return Response.success("Events listed successfully", eventService.listAll());
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

    @Operation(summary = "Crear evento")
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

    @Operation(summary = "Actualizar evento")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateEventDto dto) {
        try {
            var updated = eventService.update(id, dto);
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
