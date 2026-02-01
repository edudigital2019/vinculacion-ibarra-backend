package com.app.municipio.modules.users.controllers;

import com.app.municipio.application.exceptions.ApResponse;
import com.app.municipio.application.utils.Response;
import com.app.municipio.application.utils.ResponseEntityUtils;
import com.app.municipio.modules.users.dto.Responses.PaymentReceiptDto;
import com.app.municipio.modules.users.dto.UpdateUserDto;
import com.app.municipio.modules.users.dto.UserRegisterDto;
import com.app.municipio.modules.users.models.AppUser;
import com.app.municipio.modules.users.services.DocumentsService;
import com.app.municipio.modules.users.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for controlling user operations")
public class UserController {
    private final UserService service;
    private final DocumentsService documentsService;


    @PreAuthorize("permitAll()")
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Register a new user",
            description = "Register user with identity document and certificate (PDF/JPG/PNG, max 2MB each)",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Validation error or invalid file"),
                    @ApiResponse(responseCode = "500", description = "Server error")
            }
    )
    public ResponseEntity<ApResponse> register(
            @Parameter(description = "User data")
            @RequestPart @Valid UserRegisterDto data,

            @Parameter(description = "Identity document (PDF/Image, max 2MB)")
            @RequestPart MultipartFile identityDocument,

            @Parameter(description = "Certificate (PDF/Image, max 2MB)")
            @RequestPart MultipartFile certificate,

            @Parameter (description = "Signed document (PDF/Image, max 2MB)")
            @RequestParam MultipartFile signedDocument,

            @Parameter (description = "Payment receipt (PDF/Image, max 2MB)")
            @RequestParam MultipartFile paymentReceipt
    ) {
        service.registerUser(data, identityDocument, certificate, signedDocument, paymentReceipt);
        return Response.success("Usuario creado exitosamente");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-new-admin")
    @Operation(
            summary = "Create New Admin",
            description = "Register a new admin user",
            tags = {"Users"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User data to register",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRegisterDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserRegisterDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Error in the dto fields, non-compliance with validations",
                            content = @Content(mediaType = "application/json")

                    )

            }
    )
    public ResponseEntity<ApResponse> createNewAdmin(
            @RequestBody UserRegisterDto data
    ) {
        service.registerAdmin(data);
        return Response.success("Nuevo administrador creado exitosamente");
    }



    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get-identity-document")
    @Operation(
            summary = "Get Identity Document",
            description = "Retrieve the identity document of a user by their ID",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Identity document retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ResponseEntity<ByteArrayResource> getIdentityDocument(@AuthenticationPrincipal AppUser user) {
        return ResponseEntityUtils.inlineResource(documentsService.getIdentityDocumentFile(user.getId()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get-certificate")
    @Operation(
            summary = "Get Certificate",
            description = "Retrieve the certificate of a user by their ID",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Certificate retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ResponseEntity<ByteArrayResource> getCertificate(@AuthenticationPrincipal AppUser user) {
        return ResponseEntityUtils.resource(documentsService.getCertificateFile(user.getId()));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update-user")
    @Operation(
            summary = "Update User",
            description = "Update user information",
            tags = {"Users"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User data to update",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateUserDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Validation error or invalid file"),
                    @ApiResponse(responseCode = "500", description = "Server error")
            }
    )

    public ResponseEntity<ApResponse> updateUser(
            @Parameter(description = "User data")
            @RequestBody @Valid UpdateUserDto data,
            @AuthenticationPrincipal AppUser user
    ) {
        service.updateUser(data, user.getId());
        return Response.success("Usuario actualizado exitosamente");
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping ("/get-signed-document")
    @Operation(
            summary = "Get signed document",
            description = "Retrieve the signed document of a user by their ID",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Signed Document retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Signed Document not found")
            }
    )
    public ResponseEntity<ByteArrayResource> getSignedDocument(@AuthenticationPrincipal AppUser user) {
        return ResponseEntityUtils.resource(documentsService.getSignedDocumentFile(user.getId()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping ("/get-payment-receipt")
    @Operation(
            summary = "Get payment receipt",
            description = "Retrieve the payment receipt of a user by their ID",
            tags = {"Users"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment receipt retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Payment receipt not found")
            }
    )
    public ResponseEntity<PaymentReceiptDto> getPaymentReceipt(@AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(documentsService.getPaymentReceipt(user.getId()));
    }



}
