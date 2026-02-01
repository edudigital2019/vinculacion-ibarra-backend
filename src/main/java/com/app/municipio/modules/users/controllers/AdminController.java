package com.app.municipio.modules.users.controllers;

import com.app.municipio.application.exceptions.ApResponse;
import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.application.utils.Response;
import com.app.municipio.application.utils.ResponseEntityUtils;
import com.app.municipio.modules.users.dto.DashboardStats;
import com.app.municipio.modules.users.dto.Request.UserApprovalRequest;
import com.app.municipio.modules.users.dto.Responses.PaymentReceiptDto;
import com.app.municipio.modules.users.dto.Responses.PendingUserResponse;
import com.app.municipio.modules.users.dto.UserResponseDto;
import com.app.municipio.modules.users.dto.WhoAmIDto;
import com.app.municipio.modules.users.services.AdminService;
import com.app.municipio.modules.users.services.DocumentsService;
import com.app.municipio.modules.users.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Endpoints for controlling admin operations")
public class AdminController {
        private final AdminService service;
        private final DocumentsService documentsService;
        private final UserService userService;

        @Operation(summary = "Listar usuarios pendientes", description = "Obtiene todos los usuarios con estado 'pendiente de aprobación' (paginado)", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Lista paginada de usuarios pendientes", content = @Content(mediaType = "application/json")),
                                        @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
                        })
        @GetMapping("/pending")
        public ResponseEntity<Page<PendingUserResponse>> getPendingUsers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return ResponseEntity.ok(service.getPendingUsers(page, size));
        }

        @Operation(summary = "Aprobar usuario", description = "Endpoint para aprobar usuarios pendientes (solo usuarios no habilitados)", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Usuario aprobado exitosamente"),
                                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                                        @ApiResponse(responseCode = "400", description = "El usuario ya está habilitado")
                        })
        @PostMapping("/approve/{userId}")
        public ResponseEntity<ApResponse> approveUser(@PathVariable Long userId) {
                try {
                        service.processUserApproval(new UserApprovalRequest(userId, true, null));
                        return Response.success("Usuario aprobado exitosamente");
                } catch (ClientException e) {
                        return Response.badRequest(e.getMessage());
                }
        }

        @Operation(summary = "Rechazar usuario", description = "Endpoint para rechazar usuarios pendientes (solo usuarios no habilitados)", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Usuario rechazado exitosamente"),
                                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                                        @ApiResponse(responseCode = "400", description = "El usuario ya está habilitado")
                        })
        @DeleteMapping("/reject/{userId}")
        public ResponseEntity<ApResponse> rejectUser(@PathVariable Long userId, @RequestParam String reason) {
                try {
                        service.processUserApproval(new UserApprovalRequest(userId, false, reason));
                        return Response.success("Usuario rechazado exitosamente");
                } catch (ClientException e) {
                        return Response.badRequest(e.getMessage());
                }
        }

        @GetMapping("/get-user-by-id/{id}")
        @Operation(summary = "Get User By ID", description = "Get user details by ID (admin only)", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "User found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WhoAmIDto.class))),
                                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json"))
                        })
        public ResponseEntity<WhoAmIDto> getUserById(@PathVariable Long id) {
                return ResponseEntity.ok(service.getUserDetailsById(id));
        }

        @GetMapping("/get-user-identity-document")
        @Operation(summary = "Get User Identity Document", description = "Retrieves the identity document file uploaded by a specific user (Admin only)", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Identity document retrieved successfully", content = @Content(mediaType = "application/octet-stream", schema = @Schema(type = "string", format = "binary"))),
                                        @ApiResponse(responseCode = "404", description = "User not found or document not available", content = @Content(mediaType = "application/json")),
                                        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required", content = @Content(mediaType = "application/json"))
                        })
        public ResponseEntity<ByteArrayResource> getIdentityDocument(
                        @RequestParam Long userId) {
                return ResponseEntityUtils.inlineResource(documentsService.getIdentityDocumentFile(userId));
        }

        @GetMapping("/get-user-certificate")
        @Operation(summary = "Get User Certificate", description = "Retrieves the certificate file uploaded by the authenticated user", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Certificate retrieved successfully", content = @Content(mediaType = "application/octet-stream", schema = @Schema(type = "string", format = "binary"))),
                                        @ApiResponse(responseCode = "404", description = "Certificate not found for the user", content = @Content(mediaType = "application/json")),
                                        @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(mediaType = "application/json"))
                        })

        public ResponseEntity<ByteArrayResource> getCertificate(
                        @RequestParam Long userId) {
                return ResponseEntityUtils.resource(documentsService.getCertificateFile(userId));
        }

        @GetMapping("/get-signed-document")
        @Operation(summary = "Ver documento firmado del usuario", description = "Permite al administrador visualizar o descargar el documento firmado de un usuario", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Documento firmado obtenido exitosamente"),
                                        @ApiResponse(responseCode = "404", description = "Documento no encontrado"),
                                        @ApiResponse(responseCode = "403", description = "Solo admins pueden acceder")
                        })
        public ResponseEntity<ByteArrayResource> reviewSignedDocument(@RequestParam Long userId) {
                var file = documentsService.getSignedDocumentFile(userId);
                return ResponseEntityUtils.resource(file);
        }

        @GetMapping("/get-dashboard-stats")
        @Operation(summary = "Obtener estadísticas del dashboard", description = "Obtiene las estadísticas del dashboard para el administrador", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
                                        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
                        })
        public ResponseEntity<DashboardStats> getDashboardStats() {
                return ResponseEntity.ok(userService.getDashboardStats());
        }

        // --Endpoint nuevos
        @Operation(summary = "Buscar usuario por identificación", description = "Obtiene un usuario por su número de identificación", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Usuario encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
                                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
                                        @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
                        })
        @GetMapping("/search/identification/{identification}")
        public ResponseEntity<?> getUserByIdentification(@PathVariable String identification) {
                Optional<UserResponseDto> user = service.findUserByIdentification(identification);
                if (user.isPresent()) {
                        return ResponseEntity.ok(user.get());
                }
                return Response.notFound("Usuario no encontrado con identificación: " + identification);
        }

        // Por nombre o identificacion
        @Operation(summary = "Buscar usuarios por nombre o identificación", description = "Busca usuarios por nombre (búsqueda parcial case-insensitive) o identificación exacta", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Lista paginada de usuarios encontrados", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
                                        @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
                        })
        @GetMapping("/search")
        public ResponseEntity<Page<UserResponseDto>> searchUsersByNameOrIdentification(
                        @RequestParam String searchTerm,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return ResponseEntity.ok(service.searchUsersByNameOrIdentification(searchTerm, page, size));
        }

        @Operation(summary = "Obtener usuarios por estado", description = "Obtiene usuarios filtrados por estado. El parámetro 'enabled' es requerido (true = aprobados, false = pendientes)", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Lista paginada de usuarios por estado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
                                        @ApiResponse(responseCode = "400", description = "El parámetro 'enabled' es requerido"),
                                        @ApiResponse(responseCode = "404", description = "No se encontraron usuarios con el estado especificado")
                        })
        @GetMapping("/users")
        public ResponseEntity<?> getUsersByStatus(
                        @RequestParam(required = false) Boolean enabled,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                if (enabled == null) {
                        return Response.badRequest(
                                        "El parámetro 'enabled' es requerido (true para aprobados, false para pendientes)");
                }

                Page<UserResponseDto> users = service.getUsersByStatus(enabled, page, size);

                if (users.isEmpty()) {
                        return ResponseEntity.notFound().build();
                }

                return ResponseEntity.ok(users);
        }

        @Operation (summary = "Obtener recibo de pago del usuario", description = "Permite al administrador obtener el recibo de pago subido por un usuario", tags = {
                        "Admin" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Recibo de pago obtenido exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentReceiptDto.class))),
                                        @ApiResponse(responseCode = "404", description = "Recibo de pago no encontrado"),
                                        @ApiResponse(responseCode = "403", description = "Solo admins pueden acceder")
                        })
        @GetMapping("/get-payment-receipt")
        public ResponseEntity<PaymentReceiptDto> getPaymentReceipt(
                        @RequestParam Long userId
        ) {
                return ResponseEntity.ok(documentsService.getPaymentReceipt(userId));
        }
}
