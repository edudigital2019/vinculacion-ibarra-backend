package com.app.municipio.modules.business.controllers;

import com.app.municipio.application.exceptions.ApResponse;
import com.app.municipio.modules.business.dto.Request.BusinessDeletionRequestDTO;
import com.app.municipio.modules.business.models.enums.DeletionRequestStatus;
import com.app.municipio.modules.business.services.BusinessDeletionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/business/deletion")
@RequiredArgsConstructor
public class BusinessDeletionController {

    private final BusinessDeletionService deletionService;

    /** User o Admin solicitan eliminación */
    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Solicitar eliminación de negocio (user o admin)")
    public ResponseEntity<ApResponse> requestDeletion(
            @PathVariable Long id,
            @RequestParam String motivo,
            @RequestParam String justificacion
    ) {
        deletionService.requestDeletion(id, motivo, justificacion);
        return ResponseEntity.ok(new ApResponse(true, "Solicitud enviada correctamente", null));
    }

    /**
     * Admin lista solicitudes (sin paginación)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar solicitudes de eliminación por estado")
    public ResponseEntity<List<BusinessDeletionRequestDTO>> listRequests(
            @RequestParam(defaultValue = "PENDING") DeletionRequestStatus status
    ) {
        return ResponseEntity.ok(deletionService.getRequestsByStatus(status));
    }

    /** Admin aprueba (id = id de la SOLICITUD) */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar solicitud de eliminación (id = solicitud)")
    public ResponseEntity<ApResponse> approve(
            @PathVariable Long id,
            @RequestParam(required = false) String justification
    ) {
        deletionService.decide(id, true, justification);
        return ResponseEntity.ok(new ApResponse(true, "Negocio eliminado correctamente", null));
    }

    /** Admin rechaza (id = id de la SOLICITUD) */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rechazar solicitud de eliminación (id = solicitud)")
    public ResponseEntity<ApResponse> reject(
            @PathVariable Long id,
            @RequestParam String justification
    ) {
        deletionService.decide(id, false, justification);
        return ResponseEntity.ok(new ApResponse(false, "Solicitud rechazada", null));
    }
}
