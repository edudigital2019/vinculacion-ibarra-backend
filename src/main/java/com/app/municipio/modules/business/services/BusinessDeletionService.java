package com.app.municipio.modules.business.services;

import com.app.municipio.application.cloudinary.CloudinaryService;
import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.modules.business.dto.Request.BusinessDeletionRequestDTO;
import com.app.municipio.modules.business.models.Business;
import com.app.municipio.modules.business.models.BusinessDeletionRequest;
import com.app.municipio.modules.business.models.enums.DeletionRequestStatus;
import com.app.municipio.modules.business.repositories.BusinessDeletionRequestRepository;
import com.app.municipio.modules.business.repositories.BusinessRepository;
import com.app.municipio.modules.photos.models.Photo;
import com.app.municipio.modules.photos.repository.PhotoRepository;
import com.app.municipio.modules.users.models.AppUser;
import com.app.municipio.modules.users.repositories.UsersRepository;
import com.app.municipio.modules.users.services.AuthService;
import com.app.municipio.utils.EmailService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class BusinessDeletionService {

    private final BusinessRepository businessRepository;
    private final BusinessDeletionRequestRepository deletionRequestRepository;
    private final UsersRepository usersRepository;
    private final AuthService authService;

    private final CloudinaryService cloudinaryService;
    private final PhotoRepository photoRepository;
    private final EmailService emailService;

    private final EntityManager em;

    public void requestDeletion(Long businessId, String motivo, String justificacion) {
        var who = authService.getAuthenticatedUser();
        String currentUsername = who.getUsername();
        boolean isAdmin = hasRole(who.getRoles(), "ADMIN");

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ClientException("Negocio no encontrado", HttpStatus.NOT_FOUND));

        if (!isAdmin && !business.getUser().getUsername().equalsIgnoreCase(currentUsername)) {
            throw new ClientException("No tienes permiso para eliminar este negocio", HttpStatus.FORBIDDEN);
        }

        if (deletionRequestRepository.existsByBusinessIdAndStatus(businessId, DeletionRequestStatus.PENDING)) {
            throw new ClientException("Ya existe una solicitud pendiente para este negocio", HttpStatus.CONFLICT);
        }

        AppUser requester = usersRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ClientException("Solicitante no encontrado", HttpStatus.NOT_FOUND));

        BusinessDeletionRequest req = BusinessDeletionRequest.builder()
                .business(business)
                .requester(requester)
                .motivo(motivo)
                .justificacion(justificacion)
                .status(DeletionRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        deletionRequestRepository.save(req);

        String destinatario = isAdmin ? business.getUser().getEmail() : requester.getEmail();
        log.info("Solicitud de eliminación creada: requestId={}, businessId={}, requester={}, admin?={}",
                req.getId(), businessId, requester.getUsername(), isAdmin);

        emailService.sendEmail(
                destinatario,
                "Solicitud de eliminación recibida",
                (isAdmin ? "Un administrador inició una solicitud" : "Tu solicitud ha sido registrada")
                        + " para el negocio \"" + business.getCommercialName() + "\".\n"
                        + "Motivo: " + motivo + "\n"
                        + "Justificación: " + justificacion
        );
    }

    @Transactional(readOnly = true)
    public List<BusinessDeletionRequestDTO> getRequestsByStatus(DeletionRequestStatus status) {
        return deletionRequestRepository.findAllByStatus(status).stream()
                .map(req -> new BusinessDeletionRequestDTO(
                        req.getId(),
                        req.getBusiness().getCommercialName(),
                        req.getMotivo(),
                        req.getJustificacion(),
                        req.getStatus(),
                        req.getRequester() != null ? req.getRequester().getEmail() : null
                ))
                .toList();
    }

    public void decide(Long requestId, boolean approve, String decisionJustification) {
        String adminUsername = authService.getAuthenticatedUser().getUsername();
        AppUser admin = usersRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ClientException("Administrador no encontrado", HttpStatus.NOT_FOUND));

        BusinessDeletionRequest req = deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ClientException("Solicitud no encontrada", HttpStatus.NOT_FOUND));

        if (req.getStatus() != DeletionRequestStatus.PENDING) {
            throw new ClientException("Esta solicitud ya fue procesada", HttpStatus.BAD_REQUEST);
        }

        Business business = req.getBusiness();

        String businessName = business.getCommercialName();
        String motivoOriginal = req.getMotivo();
        String justificacionOriginal = req.getJustificacion();
        String destinatario = (req.getRequester() != null) ? req.getRequester().getEmail() : null;

        req.setDecidedBy(admin);
        req.setDecidedAt(LocalDateTime.now());

        if (approve) {
            req.setStatus(DeletionRequestStatus.APPROVED);
            deletionRequestRepository.save(req);

            // 1) BORRAR ASSETS EN CLOUDINARY (con logs del resultado)
            deleteBusinessAssetsOrThrow(business);

            // 2) ELIMINAR TODAS LAS SOLICITUDES DEL NEGOCIO (libera FK)
            deletionRequestRepository.deleteAllByBusinessId(business.getId());
            em.flush(); // forzar ejecución inmediata del DELETE en DB

            // 3) ELIMINAR NEGOCIO
            businessRepository.delete(business);
            businessRepository.flush(); // fuerza commit inmediato
            log.info(">>> Verificando eliminación inmediata...");

            boolean exists = businessRepository.existsById(business.getId());
            log.info("¿Negocio aún existe en DB? {}", exists);

            em.flush(); // forzar ejecución inmediata del DELETE en DB

            log.info("Negocio eliminado OK: id={}, nombre='{}'", business.getId(), businessName);

            // 4) EMAIL APROBADA
            emailService.sendEmail(
                    destinatario,
                    "Eliminación de negocio aprobada",
                    "Se aprobó la eliminación del negocio \"" + businessName + "\".\n" +
                            "Motivo original: " + motivoOriginal + "\n" +
                            "Justificación original: " + justificacionOriginal + "\n" +
                            (decisionJustification != null ? "Observación del administrador: " + decisionJustification : "")
            );

        } else {
            req.setStatus(DeletionRequestStatus.REJECTED);
            deletionRequestRepository.save(req);

            log.info("Solicitud rechazada: requestId={}, businessId={}, decidedBy={}",
                    req.getId(), business.getId(), admin.getUsername());

            emailService.sendEmail(
                    destinatario,
                    "Solicitud de eliminación rechazada",
                    "Se rechazó la eliminación del negocio \"" + businessName + "\".\n" +
                            "Motivo original: " + motivoOriginal + "\n" +
                            "Justificación original: " + justificacionOriginal + "\n" +
                            (decisionJustification != null ? "Justificación del rechazo: " + decisionJustification : "")
            );
        }
    }

    /** Borra todas las fotos en Cloudinary según lo que hay en BD. Si alguna falla → lanza excepción → rollback. */
    private void deleteBusinessAssetsOrThrow(Business business) {
        var photos = photoRepository.findAllByBusiness_Id(business.getId());
        log.info("Fotos encontradas para businessId={}: {}", business.getId(), photos.size());

        boolean hadHardFailure = false;

        for (Photo p : photos) {
            // Derivar resourceType a partir del fileType guardado
            String resourceType = "image";
            String ft = p.getFileType();
            if (ft != null && ft.toLowerCase().contains("pdf")) resourceType = "raw";

            try {
                var res = cloudinaryService.deleteFile(p.getPublicId(), resourceType);
                Object result = (res != null ? res.get("result") : null);
                String resultStr = result != null ? result.toString() : "null";

                log.info("Cloudinary destroy -> publicId={} type={} response={}", p.getPublicId(), resourceType, res);

                // ÉXITO: ok o not_found (idempotente)
                if ("ok".equals(resultStr) || "not found".equals(resultStr) || "not_found".equals(resultStr)) {
                    continue;
                }

                // Otros estados: loguea pero NO bloquea
                log.warn("Respuesta no exitosa al eliminar publicId={} (result={})", p.getPublicId(), resultStr);

            } catch (Exception e) {
                // Excepción de red/SDK: la registramos y seguimos.
                // Si quieres bloquear ante errores de red, cambia hadHardFailure=true y maneja abajo.
                log.warn("Error eliminando en Cloudinary publicId={} : {}", p.getPublicId(), e.getMessage(), e);
                // hadHardFailure = true; // <- descomenta si deseas bloquear el borrado ante fallas de red
            }
        }

        // Borra SIEMPRE los registros de fotos de este negocio en BD.
        // Si quieres ser más estricto: bórralos solo si !hadHardFailure
        photoRepository.deleteAll(photos);
        log.info("Fotos eliminadas en BD para businessId={}: {}", business.getId(), photos.size());

        // Si decidieras bloquear por fallas duras (hadHardFailure=true), lanza aquí:
        if (hadHardFailure) {
            throw new ClientException("No se pudieron eliminar todas las imágenes del negocio", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private boolean hasRole(List<String> roles, String role) {
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role) || r.equalsIgnoreCase("ROLE_" + role));
    }
}
