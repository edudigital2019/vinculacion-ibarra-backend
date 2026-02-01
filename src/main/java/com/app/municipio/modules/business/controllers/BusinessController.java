package com.app.municipio.modules.business.controllers;

import com.app.municipio.application.exceptions.ApResponse;
import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.application.utils.PagedResponse;
import com.app.municipio.application.utils.Response;
import com.app.municipio.modules.business.dto.Request.BusinessFullUpdateDto;
import com.app.municipio.modules.business.dto.Request.CreateNewBusinessDTO;
import com.app.municipio.modules.business.dto.Responses.BusinessDTO;
import com.app.municipio.modules.business.dto.Responses.BusinessPublicCards;
import com.app.municipio.modules.business.dto.Responses.BusinessPublicDTO;
import com.app.municipio.modules.business.dto.Responses.BusinessResponseDto;
import com.app.municipio.modules.business.dto.Responses.BusinessUpdateDto;
import com.app.municipio.modules.business.dto.Responses.ParishesDto;
import com.app.municipio.modules.business.models.enums.ParishType;
import com.app.municipio.modules.business.models.enums.ValidationStatus;
import com.app.municipio.modules.business.services.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/business")
@Tag(name = "Business", description = "Endpoints para la gestión de negocios")
public class BusinessController {

        private final BusinessService businessService;

        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Listar negocios de manera privada por categoría", description = "Obtiene una lista paginada de negocios, filtrando según permisos del usuario autenticado. Si es un usuario común, solo ve sus propios negocios.", tags = {
                        "Business" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Negocios listados exitosamente", content = @Content(mediaType = "application/json")),
                                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                                        @ApiResponse(responseCode = "403", description = "No autorizado")
                        })
        @GetMapping("/private-list-by-category")
        public ResponseEntity<ApResponse> listPrivateBusinessesByCategory(
                        @Parameter(description = "Nombre de la categoría (opcional)") @RequestParam @Nullable String category,
                        @Parameter(description = "Número de página (0-index)") @RequestParam int page,
                        @Parameter(description = "Tamaño de la página") @RequestParam int size) {
                return Response.success(
                                "Businesses listed successfully",
                                businessService.listPrivateBusinesses(category, PageRequest.of(page, size)));
        }

        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Crear un nuevo negocio", description = """
                            Permite a un usuario autenticado registrar un nuevo negocio.
                            El envío debe realizarse como `multipart/form-data` con:
                            - **business**: JSON con los datos del negocio (CreateNewBusinessDTO).
                            - **logoFile**: Imagen opcional del logo.
                            - **signatureFile**: Imagen opcional de la firma.
                            - **cedulaFile**: Imagen obligatoria de la cédula/RUC.
                            - **carrouselPhotos**: Imágenes opcionales para el carrusel (múltiples archivos).
                            ### Campos obligatorios en `business` (CreateNewBusinessDTO):
                            - `categoryId` (Long) → **NOT NULL**
                            - `commercialName` (String) → **NOT NULL**
                            - `representativeName` (String) → **NOT NULL**
                            - `phone` (String) → **NOT NULL**
                            - `email` (String) → **NOT NULL**
                            - `parishCommunitySector` (String) → **NOT NULL**
                            - `description` (String) → **NOT NULL**
                            - `productsServices` (List) → puede ser vacío pero **no nulo**
                            - `acceptsWhatsappOrders` (Boolean) → **NOT NULL**
                            - `deliveryService` (Enum: SI, NO) → **NOT NULL**
                            - `salePlace` (Enum: LOCAL_FIJO, AMBULANTE, OTRO) → **NOT NULL**
                            - `registrationDate` (LocalDate) → **NOT NULL**
                            - `address` (String) → **NOT NULL**
                            - `googleMapsCoordinates` (String) → **NOT NULL**
                            - `schedules` (List<String>) → puede ser vacío pero **no nulo**

                            ### Campos opcionales en `business`:
                            - `facebook`, `instagram`, `tiktok`, `website`
                            - `udelSupportDetails`, `signatureUrl`, `cedulaFileUrl`, `logoUrl`, `validationStatus`
                        """, tags = { "Business" }, responses = {
                        @ApiResponse(responseCode = "201", description = "Negocio creado exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Datos inválidos o error al subir archivos"),
                        @ApiResponse(responseCode = "401", description = "No autenticado")
        })
        @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApResponse> createBusiness(
                        @Parameter(description = "Datos del negocio en formato JSON", required = true, schema = @Schema(implementation = CreateNewBusinessDTO.class)) @RequestPart("business") @Valid CreateNewBusinessDTO dto,

                        @Parameter(description = "Logo del negocio (opcional)") @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,

                        @Parameter(description = "Fotos para el carrusel (opcional, múltiples fotos)") @RequestPart(value = "carrouselPhotos", required = false) MultipartFile[] carrouselPhotos) {
                try {
                        String message = businessService.createBusiness(dto, logoFile, carrouselPhotos);
                        return Response.success(message, HttpStatus.CREATED);
                } catch (Exception e) {
                        return Response.badRequest("Error creating business: " + e.getMessage());
                }
        }

        @Operation(summary = "Listar negocios públicos APROBADOS (opcional por categoría)", description = "Devuelve una lista paginada de negocios con estado APROBADO. Si se envía `category`, filtra por esa categoría.", tags = {
                        "Business" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Listado exitoso", content = @Content(mediaType = "application/json")),
                                        @ApiResponse(responseCode = "400", description = "Parámetros inválidos", content = @Content(mediaType = "application/json"))
                        })
        @GetMapping("/public/approved")
        public ResponseEntity<PagedResponse<BusinessPublicCards>> listPublicApproved(
                        @Parameter(description = "Nombre de la categoría (opcional)") @RequestParam(required = false) String category,
                        @Parameter(description = "Número de página (0-index)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamaño de la página") @RequestParam(defaultValue = "10") int size) {
                return ResponseEntity.ok(
                                businessService.listPublicApproved(category, page, size));
        }

        @Operation(summary = "Obtener negocio público por ID", description = "Devuelve los detalles de un negocio público por su ID.", tags = {
                        "Business" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Negocio encontrado", content = @Content(mediaType = "application/json")),
                                        @ApiResponse(responseCode = "404", description = "Negocio no encontrado", content = @Content(mediaType = "application/json"))
                        })
        @GetMapping("/public-details")
        public ResponseEntity<BusinessPublicDTO> getPublicBusinessById(
                        @Parameter(description = "ID del negocio", required = true) @RequestParam Long id) {
                return ResponseEntity.ok(
                                businessService.getPublicBusinessDetails(id));
        }

        // ===================== ACTUALIZAR NEGOCIO =====================
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Actualizar información de negocio rechazado", description = """
                            Permite a un usuario actualizar todos los campos de un negocio que ha sido rechazado,
                            incluyendo categoría, delivery, salePlace y fotos.
                            Se envía como `multipart/form-data`:
                            - Campos del DTO en `BusinessFullUpdateDto`
                            - `logoFile` opcional
                            - `carouselFiles` opcionales
                        """, tags = {
                        "Business" }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, examples = @ExampleObject(name = "Ejemplo actualización negocio", value = "{\n"
                                        +
                                        "  \"commercialName\":\"software_licencias\",\n" +
                                        "  \"description\":\"hjhhh\",\n" +
                                        "  \"categoryId\":7,\n" +
                                        "  \"deliveryService\":\"NO\",\n" +
                                        "  \"salePlace\":\"NO\",\n" +
                                        "  \"phone\":\"+593981700772\",\n" +
                                        "  \"acceptsWhatsappOrders\":true,\n" +
                                        "  \"whatsappNumber\":\"+593981700772\",\n" +
                                        "  \"address\":\"J\",\n" +
                                        "  \"googleMapsCoordinates\":\"JJJ\",\n" +
                                        "  \"facebook\":\"\",\n" +
                                        "  \"instagram\":\"\",\n" +
                                        "  \"tiktok\":\"\",\n" +
                                        "  \"website\":\"JJJ\",\n" +
                                        "  \"schedules\":[\"Lunes 08:00-12:00\",\"Martes 08:00-12:00\",\"Miércoles 08:00-12:00\"]\n"
                                        +
                                        "}"))), responses = {
                                                        @ApiResponse(responseCode = "200", description = "Negocio actualizado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusinessResponseDto.class))),
                                                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                                                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                                                        @ApiResponse(responseCode = "403", description = "No autorizado - No es el propietario del negocio"),
                                                        @ApiResponse(responseCode = "404", description = "Negocio no encontrado")
                                        })
        @PutMapping(value = "/update-rejected/{businessId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApResponse> updateRejectedBusiness(
                        @PathVariable Long businessId,
                        @RequestPart("business") @Valid BusinessFullUpdateDto updateDto,
                        @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
                        @RequestPart(value = "carouselFiles", required = false) MultipartFile[] carouselFiles)
                        throws IOException {
                updateDto.setLogoFile(logoFile);
                updateDto.setCarouselFiles(carouselFiles);
                var updatedBusiness = businessService.updateBusiness(businessId, updateDto);
                return ResponseEntity.ok(new ApResponse(true, "Negocio actualizado exitosamente", updatedBusiness));
        }

        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Actualizar información de negocio", description = "Permite a un usuario actualizar la información de su negocio. Dependiendo del estado (PENDING, APPROVED, REJECTED) solo se permiten ciertos campos.", tags = {
                        "Business" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Negocio actualizado exitosamente", content = @Content(mediaType = "application/json")),
                                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                                        @ApiResponse(responseCode = "403", description = "No autorizado - No es el propietario del negocio"),
                                        @ApiResponse(responseCode = "404", description = "Negocio no encontrado")
                        })
        @PutMapping("/{businessId}")
        public ResponseEntity<ApResponse> updateBusiness(
                        @PathVariable Long businessId,
                        @RequestBody BusinessUpdateDto updateDto) throws IOException {
                var updatedBusiness = businessService.updateBusiness(businessId, updateDto);
                return ResponseEntity.ok(new ApResponse(true, "Negocio actualizado exitosamente", updatedBusiness));
        }

        @PreAuthorize("permitAll()")
        @Operation(summary = "Listar parroquias por zona", description = "Devuelve una lista de parroquias filtradas por tipo de zona (URBANA o RURAL).", tags = {
                        "Business" }, responses = {
                                        @ApiResponse(responseCode = "200", description = "Listado exitoso", content = @Content(mediaType = "application/json")),
                                        @ApiResponse(responseCode = "400", description = "Parámetros inválidos", content = @Content(mediaType = "application/json"))
                        })
        @GetMapping("/list-parish")
        public ResponseEntity<List<ParishesDto>> listParishes(
                        @RequestParam ParishType type) {
                return ResponseEntity.ok(businessService.getParishesByZone(type));
        }

        // Busqueda dinamica
        @PreAuthorize("permitAll()")
        @Operation(summary = "Buscar negocios", description = """
                        Permite buscar negocios por término de búsqueda con filtrado opcional por estado.
                        La búsqueda se realiza en:
                        - Nombre comercial del negocio
                        - Nombre y apellido del propietario
                        - Identificación del propietario

                        ### Características:
                        - Búsqueda incremental (funciona letra por letra)
                        - No distingue mayúsculas/minúsculas
                        - Búsqueda parcial en múltiples campos
                        """, tags = { "Business" }, responses = {
                        @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "No autenticado"),
                        @ApiResponse(responseCode = "403", description = "No autorizado")
        })
        @GetMapping("/search")
        public ResponseEntity<PagedResponse<BusinessDTO>> searchBusinesses(
                        @Parameter(description = "Término de búsqueda (opcional) - Busca en nombre comercial, nombre del propietario e identificación") @RequestParam(required = false) String searchTerm,

                        @Parameter(description = "Número de página (0-index)") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Tamaño de la página") @RequestParam(defaultValue = "10") int size) {

                return ResponseEntity.ok(
                                businessService.searchBusinesses(searchTerm, PageRequest.of(page, size)));
        }
}
