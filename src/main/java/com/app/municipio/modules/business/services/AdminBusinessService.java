package com.app.municipio.modules.business.services;

import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.application.utils.PagedResponse;
import com.app.municipio.modules.business.dto.BusinessPending;
import com.app.municipio.modules.business.dto.Request.BusinessApprovalRequest;
import com.app.municipio.modules.business.dto.Responses.*;
import com.app.municipio.modules.business.models.Business;
import com.app.municipio.modules.business.models.enums.ValidationStatus;
import com.app.municipio.modules.business.models.projections.BusinessOwnerProjection;
import com.app.municipio.modules.business.models.projections.BusinessProjection;
import com.app.municipio.modules.business.repositories.BusinessRepository;
import com.app.municipio.modules.photos.dto.Response.PhotoDTO;
import com.app.municipio.modules.photos.enums.PhotoType;
import com.app.municipio.modules.photos.models.Photo;
import com.app.municipio.modules.users.models.AppUser;
import com.app.municipio.utils.EmailMessages;
import com.app.municipio.utils.EmailService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class AdminBusinessService {

    private final BusinessRepository businessRepository;
    private final EmailService emailService;

    /**
     * Lista los negocios pendientes de aprobación agrupados por usuario
     * Usando query nativa optimizada y proyección
     */
    public PagedResponse<BusinessPending> getPendingBusinesses(int page, int size) {
        // Obtener datos agrupados directamente desde la BD
        Page<BusinessOwnerProjection> ownersPage = businessRepository.findPendingBusinessesGroupedByOwner(
                ValidationStatus.PENDING,
                PageRequest.of(page, size)
        );

        // Obtener todos los negocios para los owners de esta página
        List<Long> ownerIds = ownersPage.getContent()
                .stream()
                .map(BusinessOwnerProjection::getUserId)
                .toList();

        List<BusinessProjection> businessesForOwners = businessRepository.findBusinessesByOwnerIds(
                ValidationStatus.PENDING,
                ownerIds
        );

        // Agrupar por owner ID
        Map<Long, List<BusinessProjection>> businessesByOwner = businessesForOwners
                .stream()
                .collect(groupingBy(BusinessProjection::getUserId));

        // Mapear a DTOs
        List<BusinessPending> result = ownersPage.getContent()
                .stream()
                .map(owner -> BusinessPending.builder()
                        .ownerName(owner.getOwnerName())
                        .identification(owner.getIdentification())
                        .phone(owner.getPhoneNumber())
                        .business(businessesByOwner.get(owner.getUserId())
                                .stream()
                                .map(b -> BusinessPending.BusinessByOwner.builder()
                                        .id(b.getId())
                                        .businessName(b.getCommercialName())
                                        .status(b.getStatus())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return new PagedResponse<>(new PageImpl<>(result, ownersPage.getPageable(), ownersPage.getTotalElements()));
    }

    /**
     * Obtener estadísticas de negocios por estado
     */
    public BusinessStatsDTO getBusinessStats() {
        long total = businessRepository.count();
        long pending = businessRepository.countByValidationStatus(ValidationStatus.PENDING);
        long approved = businessRepository.countByValidationStatus(ValidationStatus.VALIDATED);
        long rejected = businessRepository.countByValidationStatus(ValidationStatus.REJECTED);

        return BusinessStatsDTO.builder()
                .totalBusinesses(total)
                .pendingBusinesses(pending)
                .approvedBusinesses(approved)
                .rejectedBusinesses(rejected)
                .build();
    }

    /**
     * Método unificado para búsqueda y filtrado de negocios
     * @param searchTerm término de búsqueda (opcional) - busca en nombre usuario, cédula, nombre comercial
     * @param status estado de validación (opcional) - filtra por estado específico
     * @param page número de página
     * @param size tamaño de página
     * @return lista paginada de negocios con formato simplificado
     */
    public PagedResponse<BusinessSummaryDTO> getBusinessesWithFilters(String searchTerm, ValidationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Limpiar searchTerm si viene vacío
        String cleanSearchTerm = (searchTerm != null && searchTerm.trim().isEmpty()) ? null : searchTerm;

        Page<Business> businessPage = businessRepository.findBusinessesWithFilters(
                cleanSearchTerm,
                status,
                pageable
        );

        List<BusinessSummaryDTO> businessSummaryList = businessPage.getContent()
                .stream()
                .map(this::mapToBusinessSummaryDTO)
                .toList();

        return new PagedResponse<>(new PageImpl<>(businessSummaryList, pageable, businessPage.getTotalElements()));
    }

    /**
     * Mapea Business a BusinessSummaryDTO
     */
    private BusinessSummaryDTO mapToBusinessSummaryDTO(Business business) {
        return BusinessSummaryDTO.builder()
                .id(business.getId())
                .commercialName(business.getCommercialName())
                .representativeName(buildRepresentativeName(business.getUser()))
                .address(business.getAddress())
                .description(business.getDescription())
                .phone(business.getPhone())
                .logoUrl(getLogoUrl(business))
                .validationStatus(business.getValidationStatus())
                .build();
    }
    /**
     * Construye el nombre completo del representante
     */
    private String buildRepresentativeName(AppUser user) {
        if (user.getLastname() != null && !user.getLastname().trim().isEmpty()) {
            return user.getName() + " " + user.getLastname();
        }
        return user.getName();
    }

    /**
     * Obtiene URL del logo
     */
    private String getLogoUrl(Business business) {
        return business.getPhotos().stream()
                .filter(photo -> PhotoType.LOGO.equals(photo.getPhotoType()))
                .findFirst()
                .map(Photo::getUrl)
                .orElse(null);
    }

    /**
     * Aprobar o rechazar un negocio
     */
    public void processBusinessApproval(BusinessApprovalRequest request) {
        Business business = businessRepository.findById(request.businessId())
                .orElseThrow(() -> new EntityNotFoundException("Negocio no encontrado"));

        if (business.getValidationStatus() != ValidationStatus.PENDING) {
            throw new ClientException("Solo se pueden procesar negocios pendientes", HttpStatus.BAD_REQUEST);
        }

        AppUser user = business.getUser();

        if (request.approve()) {
            business.setValidationStatus(ValidationStatus.VALIDATED); // aprobado
            business.setRejectionReason(null); // limpiar rechazo
            businessRepository.save(business);

            emailService.sendEmail(
                    user.getEmail(),
                    EmailMessages.getStatusSubject("Negocio Aprobado"),
                    EmailMessages.getApprovalMessage(user.getName())
            );

        } else {
            if (request.rejectionReason() == null || request.rejectionReason().isBlank()) {
                throw new ClientException("La razón de rechazo es obligatoria", HttpStatus.BAD_REQUEST);
            }

            business.setValidationStatus(ValidationStatus.REJECTED);
            business.setRejectionReason(request.rejectionReason());
            businessRepository.save(business);

            emailService.sendEmail(
                    user.getEmail(),
                    EmailMessages.getStatusSubject("Negocio Rechazado"),
                    EmailMessages.getRejectionMessage(user.getName(), request.rejectionReason())
            );
        }
    }

    /**
     * Obtener detalles de un negocio
     */
    public BusinessDTO getBusinessDetails(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ClientException("Negocio no encontrado", HttpStatus.NOT_FOUND));

        return mapToBusinessDTO(business);
    }

    /**
     * Mapea la entidad Business a DTO
     */
    private BusinessDTO mapToBusinessDTO(Business business) {
        return BusinessDTO.builder()
                .id(business.getId())
                .commercialName(business.getCommercialName())
                .phone(business.getPhone())
                .parishCommunitySector(business.getParishCommunitySector())
                .description(business.getDescription())
                .facebook(business.getFacebook())
                .instagram(business.getInstagram())
                .tiktok(business.getTiktok())
                .website(business.getWebsite())
                .deliveryService(business.getDeliveryService())
                .salePlace(business.getSalePlace())
                .acceptsWhatsappOrders(business.getAcceptsWhatsappOrders())
                .receivedUdelSupport(business.getReceivedUdelSupport())
                .validationStatus(business.getValidationStatus())
                .rejectionReason(business.getRejectionReason())
                .registrationDate(business.getRegistrationDate())
                .user(UserBasicInfoDTO.builder()
                        .id(business.getUser().getId())
                        .name(business.getUser().getName())
                        .email(business.getUser().getEmail())
                        .identification(business.getUser().getIdentification())
                        .build())
                .category(CategoryBusinessDTO.builder()
                        .id(business.getCategory().getId())
                        .name(business.getCategory().getName())
                        .build())
                .photos(business.getPhotos().stream()
                        .map(photo -> PhotoDTO.builder()
                                .id(photo.getId())
                                .url(photo.getUrl())
                                .photoType(photo.getPhotoType())
                                .build())
                        .toList())
                .build();
    }
}
