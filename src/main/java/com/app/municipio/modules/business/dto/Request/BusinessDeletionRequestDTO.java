package com.app.municipio.modules.business.dto.Request;

import com.app.municipio.modules.business.models.enums.DeletionRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BusinessDeletionRequestDTO {
    private Long id;                  // ID de la solicitud
    private String businessName;      // Nombre del negocio
    private String motivo;            // Motivo de la solicitud
    private String justificacion;     // Justificación del solicitante
    private DeletionRequestStatus status; // Estado actual de la solicitud
    private String requestedBy;       // Email o nombre del usuario solicitante
}
