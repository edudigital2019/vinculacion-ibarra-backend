package com.app.municipio.modules.business.controllers;

import com.app.municipio.application.exceptions.ApResponse;
import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.application.utils.PagedResponse;
import com.app.municipio.application.utils.Response;
import com.app.municipio.modules.business.dto.BusinessPending;
import com.app.municipio.modules.business.dto.Request.BusinessApprovalRequest;
import com.app.municipio.modules.business.dto.Responses.BusinessDTO;
import com.app.municipio.modules.business.dto.Responses.BusinessStatsDTO;
import com.app.municipio.modules.business.dto.Responses.BusinessSummaryDTO;
import com.app.municipio.modules.business.models.enums.ValidationStatus;
import com.app.municipio.modules.business.services.AdminBusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/business")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "AdminBusiness", description = "Endpoints para gestión de negocios por admins")
public class AdminBusinessController {

    private final AdminBusinessService service;

    @Operation(summary = "Listar negocios pendientes", description = "Obtiene todos los negocios con estado 'PENDING' (paginado)")
    @GetMapping("/pending")
    public ResponseEntity<PagedResponse<BusinessPending>> getPendingBusinesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getPendingBusinesses(page, size));
    }

    @Operation(summary = "Obtener estadísticas de negocios",
            description = "Obtiene contador de negocios por estado: pendientes, aprobados, rechazados y total")
    @GetMapping("/stats")
    public ResponseEntity<BusinessStatsDTO> getBusinessStats() {
        return ResponseEntity.ok(service.getBusinessStats());
    }

    @Operation(summary = "Buscar y filtrar negocios",
            description = "Busca negocios por término (nombre, cédula, nombre comercial) y/o filtra por estado. Ambos parámetros son opcionales.")
    @GetMapping("/search-and-filter")
    public ResponseEntity<PagedResponse<BusinessSummaryDTO>> getBusinessesWithFilters(
            @Parameter(description = "Término de búsqueda (nombre, cédula, nombre comercial) - OPCIONAL")
            @RequestParam(required = false) String searchTerm,

            @Parameter(description = "Estado de validación (PENDING, VALIDATED, REJECTED) - OPCIONAL")
            @RequestParam(required = false) ValidationStatus status,

            @Parameter(description = "Número de página")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getBusinessesWithFilters(searchTerm, status, page, size));
    }

    @Operation(summary = "Aprobar un negocio", description = "Aprueba un negocio pendiente de validación")
    @PostMapping("/approve/{businessId}")
    public ResponseEntity<ApResponse> approveBusiness(@PathVariable Long businessId) {
        try {
            service.processBusinessApproval(new BusinessApprovalRequest(businessId, true, null));
            return Response.success("Negocio aprobado exitosamente");
        } catch (ClientException e) {
            return Response.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "Rechazar un negocio", description = "Rechaza un negocio pendiente de validación")
    @PutMapping("/reject/{businessId}")
    public ResponseEntity<ApResponse> rejectBusiness(@PathVariable Long businessId, @RequestParam String reason) {
        try {
            service.processBusinessApproval(new BusinessApprovalRequest(businessId, false, reason));
            return Response.success("Negocio rechazado exitosamente");
        } catch (ClientException e) {
            return Response.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "Obtener detalle de un negocio", description = "Obtiene toda la información de un negocio por ID")
    @GetMapping("/{id}")
    public ResponseEntity<BusinessDTO> getBusinessById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getBusinessDetails(id));
    }
}