package com.app.municipio.modules.business.controllers;

import com.app.municipio.application.exceptions.ApResponse;
import com.app.municipio.application.utils.Response;
import com.app.municipio.application.validations.annotation.FileSize;
import com.app.municipio.application.validations.annotation.FileType;
import com.app.municipio.modules.business.dto.Request.CreateBusinessPromoDto;
import com.app.municipio.modules.business.dto.Request.UpdatePromoDto;
import com.app.municipio.modules.business.models.enums.PromoType;
import com.app.municipio.modules.business.services.BusinessPromotionService;
import com.app.municipio.modules.users.models.AppUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/promotions/business")
@Tag(name = "Promotions", description = "Endpoints para la gestión de promociones de los negocios")
public class BusinessPromotionController {

    private final BusinessPromotionService businessPromotionService;

    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Listar promociones activas por id de negocio (privado)",
            description = "Obtiene una lista de todas las promociones activas del negocio ",
            tags = {"Promotions"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promociones listadas exitosamente",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "No autorizado")
            }
    )
    @GetMapping("/private")
    public ResponseEntity<ApResponse> listPromXBusinessId(
            @RequestParam(required = false) Long businessId
    ) {
        try{
        return Response.success(
                "Promociones listadas exitosamente",
                businessPromotionService.listPromXBusinessId(businessId)
        );
        } catch (Exception e) {
            return Response.badRequest("Error al consultar promoción de negocio: " + e.getMessage());
        }
    }



    @PreAuthorize("permitAll()")
    @Operation(
            summary = "Listar promociones activas con o sin filtro (publico)",
            description = "Obtiene una lista de todas las promociones activas de los negocios, si no envian el tipo de promoción trae todo",
            tags = {"Promotions"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promociones listadas exitosamente",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "No autorizado")
            }
    )
    @GetMapping("/public")
    public ResponseEntity<ApResponse> listPublicBusinessProm(
            @RequestParam(required = false) PromoType promotionType
    ) {
        try{
        return Response.success(
                "Promociones listadas exitosamente",
                businessPromotionService.listPublicBusinessesProms(promotionType)
        );
        } catch (Exception e) {
            return Response.badRequest("Error al consultar promoción de negocio: " + e.getMessage());
        }
    }


    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Agregar promociones al negocio",
            description = """
                        Permite a un usuario autenticado registrar una nueva promo a su negocio.
                        - **businessId**: id del negocio
                        - **tipoPromocion**: tipo de promocion diponible
                        - `tituloPromocion` (String) → **NOT NULL**
                        - `fechaPromoInicio` (Date) → **NOT NULL**
                        - `fechaPromoFin` (Date) → **NOT NULL**
                        - `condiciones` (String) → **NOT NULL**
                     """,
            tags = {"Promotions"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Negocios listados exitosamente",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "No autorizado")
            }
    )
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApResponse> createBusinessPromo(
            @Parameter(description = "Datos de la promo en formato JSON", required = true, schema = @Schema(implementation = CreateBusinessPromoDto.class))
            @RequestPart CreateBusinessPromoDto dto,

            @Parameter(description = "Foto de la promoción (JPG/PNG, max 2MB)", required = true)
            @RequestPart MultipartFile photo,

            @AuthenticationPrincipal AppUser user
    ) {
        try {
            String message = businessPromotionService.createBusinessPromo(dto, photo, user.getId());
            return Response.success(message, HttpStatus.CREATED);
        } catch (Exception e) {
            return Response.badRequest("Error al crear promoción de negocio: " + e.getMessage());
        }
    }
    @PreAuthorize("permitAll()")
    @Operation(
            summary = "Buscar promociones activas con filtros dinámicos",
            description = """
                        Permite obtener promociones activas aplicando filtros opcionales:
                        - **promotionType**: filtra por tipo de promoción (2x1, DESCUENTO_FIJO, COMBO, etc.)
                        - **categoryId**: filtra por categoría de negocio (ejemplo: salud, mecánicas, textil, etc.)
                        - Si no se envían filtros, retorna todas las promociones activas.
                     """,
            tags = {"Promotions"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promociones listadas exitosamente",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "No autorizado")
            }
    )
    @GetMapping("/public/search")
    public ResponseEntity<ApResponse> searchPromotions(
            @Parameter(description = "Tipo de promoción (ej: DESCUENTO_FIJO, COMBO, 2X1)") 
            @RequestParam(required = false) PromoType promotionType,

            @Parameter(description = "ID de la categoría de negocio (ej: salud, mecánicas, textil)") 
            @RequestParam(required = false) Long categoryId
    ) {
        try {
            return Response.success(
                    "Promociones filtradas exitosamente",
                    businessPromotionService.searchPromotions(promotionType, categoryId)
            );
        } catch (Exception e) {
            return Response.badRequest("Error al buscar promociones: " + e.getMessage());
        }
    }

    @PutMapping("/update/{promoId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Actualizar una promoción de negocio",
            description = """
                        Permite a un usuario autenticado actualizar una promoción existente de su negocio.
                        - **promoId**: id de la promoción a actualizar
                        - `titlePromotion` (String) → título de la promoción (opcional)
                        - `promoType` (Enum) → tipo de promoción (opcional)
                        - `conditions` (String) → condiciones de la promoción (opcional)
                        - `datePromoStart` (Date) → fecha de inicio de la promoción (opcional)
                        - `datePromoEnd` (Date) → fecha de fin de la promoción (opcional)
                        - `photo` (MultipartFile) → nueva foto para la promoción (opcional)
                     """,
            tags = {"Promotions"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promoción actualizada exitosamente",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "No autorizado"),
                    @ApiResponse(responseCode = "404", description = "Promoción no encontrada")
            }
    )
    public ResponseEntity<ApResponse> updateBusinessPromo(
            @PathVariable Long promoId,

            @Parameter(description = "Foto de la promoción (JPG/PNG, max 2MB)", required = true)
            @RequestPart(required = false) MultipartFile photo,

            @Parameter(description = "Datos de la promoción en formato JSON", required = true, schema = @Schema(implementation = UpdatePromoDto.class))
            @Valid @RequestPart UpdatePromoDto dto,

            @AuthenticationPrincipal AppUser user
    ) throws IOException {
       businessPromotionService.updatePromotion(promoId, user.getId(), photo, dto);
       return Response.success("Promoción actualizada exitosamente");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete")
    @Operation(
            summary = "Eliminar una promoción de negocio",
            description = """
                        Permite a un usuario autenticado eliminar una promoción existente de su negocio.
                        - **promoId**: id de la promoción a eliminar
                     """,
            tags = {"Promotions"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Promoción eliminada exitosamente",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                    @ApiResponse(responseCode = "403", description = "No autorizado"),
                    @ApiResponse(responseCode = "404", description = "Promoción no encontrada")
            }
    )
    public ResponseEntity<ApResponse> deleteBusinessPromo(@RequestParam Long promoId, @AuthenticationPrincipal AppUser user) {
        businessPromotionService.deletePromotion(promoId, user.getId());
        return Response.success("Promoción eliminada exitosamente");
    }

}
