package com.app.municipio.modules.business.services;

import com.app.municipio.application.cloudinary.CloudinaryService;
import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.application.validations.annotation.FileSize;
import com.app.municipio.application.validations.annotation.FileType;
import com.app.municipio.modules.business.dto.Request.CreateBusinessPromoDto;
import com.app.municipio.modules.business.dto.Request.UpdatePromoDto;
import com.app.municipio.modules.business.dto.Responses.BusinessPromoDto;
import com.app.municipio.modules.business.models.BusinessPromo;
import com.app.municipio.modules.business.models.enums.PromoType;
import com.app.municipio.modules.business.repositories.BusinessPromoRepository;
import com.app.municipio.modules.business.repositories.BusinessRepository;
import com.app.municipio.modules.photos.enums.PhotoType;
import com.app.municipio.modules.photos.models.Photo;
import com.app.municipio.modules.photos.repository.PhotoRepository;
import io.micrometer.common.lang.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
@Log4j2
public class BusinessPromotionService {

    private final BusinessPromoRepository businessPromoRep;
    private final CloudinaryService cloudinaryService;
    private final PhotoRepository photoRepository;
    private final BusinessRepository businessRepository;

    @Transactional
    public String createBusinessPromo(
            @Valid CreateBusinessPromoDto dto,
            @FileType({"jpg", "jpeg", "png"}) @FileSize(max = 2, unit = FileSize.Unit.MB) MultipartFile photo,
            Long userId) {
        if (dto.getFechaPromoFin().isBefore(dto.getFechaPromoInicio())) {
            throw new ClientException("Fecha inicio debe ser mayor a la fecha fin", HttpStatus.BAD_REQUEST);
        }
        var business = businessRepository.findByIdAndUserId(dto.getBusinessId(), userId)
                .orElseThrow(() -> new ClientException("Negocio no encontrado o no pertenece al usuario", HttpStatus.NOT_FOUND));

        BusinessPromo businessPromo = BusinessPromo.builder()
                .business(business)
                .tipoPromocion(dto.getTipoPromocion())
                .tituloPromocion(dto.getTituloPromocion())
                .fechaPromoInicio(dto.getFechaPromoInicio())
                .fechaPromoFin(dto.getFechaPromoFin())
                .condiciones(dto.getCondiciones())
                .build();
        // Guardar primero la promoción para evitar TransientObjectException al asociar
        // la foto
        businessPromo = businessPromoRep.save(businessPromo);
        try {
            if (photo != null && !photo.isEmpty()) {
                var uploadData = cloudinaryService.uploadFile(photo, "promociones");
                photoRepository.save(com.app.municipio.modules.photos.models.Photo.builder()
                        .publicId(uploadData.get(1))
                        .url(uploadData.get(0))
                        .fileType(uploadData.get(2))
                        .photoType(PhotoType.PROMOTION)
                        .businessPromo(businessPromo)
                        .business(business)
                        .build());
            }
        } catch (Exception e) {
            throw new ClientException("Error al subir la foto de la promoción: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return "Promoción de negocio registrado exitosamente.";
    }

    public List<BusinessPromoDto> listPromXBusinessId(Long id) {

        if (id == null) {
            throw new ClientException("Por favor enviar el negocio", HttpStatus.BAD_REQUEST);
        }
        List<BusinessPromo> promotions = businessPromoRep.findAllActivePromosByBusinessId(LocalDate.now(), id);
        return promotions
                .stream()
                .map(entity -> BusinessPromoDto.builder()
                        .idBusinessPromo(entity.getId())
                        .fechaPromoInicio(entity.getFechaPromoInicio())
                        .fechaPromoFin(entity.getFechaPromoFin())
                        .condiciones(entity.getCondiciones())
                        .tituloPromocion(entity.getTituloPromocion())
                        .tipoPromocion(entity.getTipoPromocion())
                        .businessId(entity.getBusiness().getId())
                        .businessImageUrl(entity.getPhotos().stream()
                                .findFirst()
                                .map(Photo::getUrl)
                                .orElse(null))
                        .businessName(entity.getBusiness().getCommercialName())
                        .build())
                .toList();
    }

    public List<BusinessPromoDto> listPublicBusinessesProms(@Nullable PromoType type) {
        List<BusinessPromo> promotions;
        if (type == null) {
            promotions = businessPromoRep.findAllActivePromosPublic(LocalDate.now());
        } else {
            promotions = businessPromoRep.findAllActivePromosPublicFiltered(LocalDate.now(), type);
        }
        return promotions
                .stream()
                .map(entity -> BusinessPromoDto.builder()
                        .idBusinessPromo(entity.getId())
                        .fechaPromoInicio(entity.getFechaPromoInicio())
                        .fechaPromoFin(entity.getFechaPromoFin())
                        .condiciones(entity.getCondiciones())
                        .tituloPromocion(entity.getTituloPromocion())
                        .tipoPromocion(entity.getTipoPromocion())
                        .businessId(entity.getBusiness() != null ? entity.getBusiness().getId() : null)
                        .businessImageUrl(entity.getPhotos().stream()
                                .findFirst()
                                .map(Photo::getUrl)
                                .orElse(null))
                        .businessName(entity.getBusiness().getCommercialName())
                        .build())
                .toList();
    }

    public List<BusinessPromoDto> searchPromotions(@Nullable PromoType type, @Nullable Long categoryId) {
        List<BusinessPromo> promotions = businessPromoRep.searchPromos(LocalDate.now(), type, categoryId);

        return promotions
                .stream()
                .map(entity -> BusinessPromoDto.builder()
                        .idBusinessPromo(entity.getId())
                        .fechaPromoInicio(entity.getFechaPromoInicio())
                        .fechaPromoFin(entity.getFechaPromoFin())
                        .condiciones(entity.getCondiciones())
                        .tituloPromocion(entity.getTituloPromocion())
                        .tipoPromocion(entity.getTipoPromocion())
                        .businessId(entity.getBusiness().getId())
                        .businessImageUrl(
                                entity.getPhotos() != null && !entity.getPhotos().isEmpty()
                                        ? entity.getPhotos().getFirst().getUrl()
                                        : null)
                        .businessName(entity.getBusiness().getCommercialName())
                        .build())
                .toList();
    }

    public void deletePromotion(Long promoId, Long userId) {
        var promo = businessPromoRep.findById(promoId)
                .orElseThrow(() -> new ClientException("Promoción no encontrada", HttpStatus.NOT_FOUND));

        if (!promo.getBusiness().getUser().getId().equals(userId)) {
            throw new ClientException("No tienes permiso para eliminar esta promoción", HttpStatus.UNAUTHORIZED);
        }

        var photos = promo.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            for (Photo photo : photos) {
                try {
                    cloudinaryService.deleteFile(photo.getPublicId());
                } catch (Exception e) {
                    // Loguear el error pero continuar con la eliminación
                    log.error("Error al eliminar la foto de Cloudinary: {}", e.getMessage());
                }
                photoRepository.delete(photo);
            }
        }

        businessPromoRep.delete(promo);
    }

    public void updatePromotion(Long promoId, Long userId, MultipartFile image, @Valid UpdatePromoDto dto) throws IOException {
        var promo = businessPromoRep.findById(promoId)
                .orElseThrow(() -> new ClientException("Promoción no encontrada", HttpStatus.NOT_FOUND));
        if (!promo.getBusiness().getUser().getId().equals(userId)) {
            throw new ClientException("No tienes permiso para actualizar esta promoción", HttpStatus.UNAUTHORIZED);
        }

        if (image != null && !image.isEmpty()) {
            validateImageFile(image);
            replacePhoto(promo, image);
        }
        if (dto.getTitlePromotion() != null) {
            promo.setTituloPromocion(dto.getTitlePromotion());
        }
        if (dto.getPromoType() != null) {
            promo.setTipoPromocion(dto.getPromoType());
        }
        if (dto.getConditions() != null) {
            promo.setCondiciones(dto.getConditions());
        }
        validateAndUpdatePromoDates(dto, promo);
        businessPromoRep.save(promo);
    }

    private void validateAndUpdatePromoDates(UpdatePromoDto dto, BusinessPromo promo) {
        LocalDate newStartDate = dto.getDatePromoStart();
        LocalDate newEndDate = dto.getDatePromoEnd();
        LocalDate currentStartDate = promo.getFechaPromoInicio();
        LocalDate currentEndDate = promo.getFechaPromoFin();

        // Determinar las fechas finales que se van a usar
        LocalDate finalStartDate = newStartDate != null ? newStartDate : currentStartDate;
        LocalDate finalEndDate = newEndDate != null ? newEndDate : currentEndDate;

        // Validar que la fecha de inicio no sea posterior a la fecha final
        if (finalStartDate.isAfter(finalEndDate)) {
            throw new ClientException("La fecha de inicio no puede ser mayor a la fecha final", HttpStatus.BAD_REQUEST);
        }

        // Actualizar las fechas si pasaron la validación
        if (newStartDate != null) {
            promo.setFechaPromoInicio(newStartDate);
        }
        if (newEndDate != null) {
            promo.setFechaPromoFin(newEndDate);
        }
    }


    private void replacePhoto(BusinessPromo promo, MultipartFile image) throws IOException {
        var existingPhotos = promo.getPhotos();
        if (existingPhotos != null && !existingPhotos.isEmpty()) {
            for (Photo photo : existingPhotos) {
                try {
                    cloudinaryService.deleteFile(photo.getPublicId());
                } catch (Exception e) {
                    log.error("Error al eliminar la foto de Cloudinary al actualizar negocio: {}", e.getMessage());
                }
                photoRepository.delete(photo);
            }
        }

        // Subir la nueva imagen y crear la nueva foto
        var uploadData = cloudinaryService.uploadFile(image, "promociones");
        Photo newPhoto = Photo.builder()
                .publicId(uploadData.get(1))
                .url(uploadData.get(0))
                .fileType(uploadData.get(2))
                .photoType(PhotoType.PROMOTION)
                .businessPromo(promo)
                .business(promo.getBusiness())
                .build();

        photoRepository.save(newPhoto);

        // Actualizar la colección de fotos en la promoción
        promo.getPhotos().clear();
        promo.getPhotos().add(newPhoto);
    }

    private void validateImageFile(MultipartFile image) {
        if (image.getSize() > 2 * 1024 * 1024) { // 2MB
            throw new ClientException("El archivo no debe superar los 2MB", HttpStatus.BAD_REQUEST);
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.matches("image/(jpeg|jpg|png)")) {
            throw new ClientException("Solo se permiten archivos JPG, JPEG y PNG", HttpStatus.BAD_REQUEST);
        }
    }
}
