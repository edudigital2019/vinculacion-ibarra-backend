package com.app.municipio.modules.business.services;

import com.app.municipio.application.cloudinary.CloudinaryService;
import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.application.utils.PagedResponse;
import com.app.municipio.modules.business.dto.Request.BusinessFullUpdateDto;
import com.app.municipio.modules.business.dto.Request.CreateNewBusinessDTO;
import com.app.municipio.modules.business.dto.Responses.*;
import com.app.municipio.modules.business.models.Business;
import com.app.municipio.modules.business.models.BusinessCategory;
import com.app.municipio.modules.business.models.enums.DeliveryService;
import com.app.municipio.modules.business.models.enums.ParishType;
import com.app.municipio.modules.business.models.enums.SalePlace;
import com.app.municipio.modules.business.models.enums.ValidationStatus;
import com.app.municipio.modules.business.repositories.BusinessCategoryRepository;
import com.app.municipio.modules.business.repositories.BusinessRepository;
import com.app.municipio.modules.business.repositories.ParishesRepository;
import com.app.municipio.modules.photos.dto.Response.PhotoDTO;
import com.app.municipio.modules.photos.enums.PhotoType;
import com.app.municipio.modules.photos.models.Photo;
import com.app.municipio.modules.users.models.AppUser;
import com.app.municipio.modules.users.models.enums.UserRoles;
import com.app.municipio.modules.users.repositories.UsersRepository;
import com.app.municipio.modules.users.services.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UsersRepository appUserRepository;
    private final BusinessCategoryRepository businessCategoryRepository;
    private final ParishesRepository parishesRepository;
    private final CloudinaryService cloudinaryService;
    private final AuthService authService;
    private static final String WHATSAPP_GLOBAL_REGEX = "^\\+[1-9]\\d{7,14}$";

    public PagedResponse<BusinessDTO> listPrivateBusinesses(@Nullable String categoryName, Pageable pageable) {
        AppUser user = appUserRepository.findByUsername(authService.getAuthenticatedUser().getUsername())
                .orElseThrow(() -> new ClientException("User not found", HttpStatus.NOT_FOUND));

        Page<Business> businesses;

        if (user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("ROLE_" + UserRoles.USER.name()))) {
            businesses = (categoryName != null)
                    ? businessRepository.findAllByCategoryAndUserId(categoryName, user.getId(), pageable)
                    : businessRepository.findAllByUserId(user.getId(), pageable);
        } else {
            businesses = (categoryName != null)
                    ? businessRepository.findAllByCategory(categoryName, pageable)
                    : businessRepository.findAll(pageable);
        }

        Page<BusinessDTO> dtoPage = businesses.map(this::mapToPrivateBusinessDTO);
        return new PagedResponse<>(dtoPage);
    }

    public String createBusiness(CreateNewBusinessDTO dto,
            @Nullable MultipartFile logoFile,
            @Nullable MultipartFile[] carrouselPhotos) {

        AppUser user = findAuthenticatedUser();
        BusinessCategory category = findCategoryById(dto.getCategoryId());
        List<Photo> photos = new ArrayList<>();
        List<String> dataLogo = null;
        List<List<String>> dataSlides = null;

        try {
            if (logoFile != null)
                dataLogo = uploadOptionalFile(logoFile);
            if (!(carrouselPhotos == null))
                dataSlides = uploadMultipleFiles(carrouselPhotos);
            if (dto.getWhatsappNumber() != null && !dto.getWhatsappNumber().isEmpty()) {
                validateWhatsapp(dto.getWhatsappNumber());
            }
            var parish = parishesRepository.findById(dto.getParishId())
                    .orElseThrow(() -> new ClientException("Parish not found", HttpStatus.NOT_FOUND));

            Business business = Business.builder()
                    .user(user)
                    .category(category)
                    .commercialName(dto.getCommercialName())
                    .phone(dto.getPhone())
                    .parishCommunitySector(dto.getParishCommunitySector())
                    .facebook(dto.getFacebook())
                    .instagram(dto.getInstagram())
                    .tiktok(dto.getTiktok())
                    .website(dto.getWebsite())
                    .description(dto.getDescription())
                    .acceptsWhatsappOrders(dto.getAcceptsWhatsappOrders())
                    .whatsappNumber(dto.getWhatsappNumber())
                    .deliveryService(DeliveryService.valueOf(dto.getDeliveryService().toUpperCase()))
                    .salePlace(SalePlace.valueOf(dto.getSalePlace().toUpperCase()))
                    .receivedUdelSupport(dto.getReceivedUdelSupport())
                    .registrationDate(dto.getRegistrationDate())
                    .address(dto.getAddress())
                    .googleMapsCoordinates(dto.getGoogleMapsCoordinates())
                    .schedules(dto.getSchedules())
                    .parish(parish)
                    .validationStatus(ValidationStatus.PENDING)
                    .build();
            var businessSaved = businessRepository.save(business);

            if (!(dataLogo == null)) {
                var logo = Photo.builder()
                        .url(dataLogo.get(0))
                        .publicId(dataLogo.get(1))
                        .fileType(dataLogo.get(2))
                        .business(businessSaved)
                        .photoType(PhotoType.LOGO)
                        .build();

                photos.add(logo);

            }

            if (!(dataSlides == null)) {
                for (List<String> slideData : dataSlides) {
                    Photo slide = Photo.builder()
                            .url(slideData.get(0))
                            .publicId(slideData.get(1))
                            .fileType(slideData.get(2))
                            .photoType(PhotoType.SLIDE)
                            .business(businessSaved)
                            .build();
                    photos.add(slide);

                }
            }

            businessSaved.setPhotos(photos);
            businessRepository.save(businessSaved);

            return "Negocio registrado exitosamente.";

        } catch (IOException e) {
            throw new ClientException("Error al subir archivos: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private BusinessDTO mapToPrivateBusinessDTO(Business business) {
        List<String> schedules = splitToList(business.getSchedules());

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
                .registrationDate(business.getRegistrationDate())
                // Nuevos campos añadidos
                .address(business.getAddress())
                .googleMapsCoordinates(business.getGoogleMapsCoordinates())
                .schedules(schedules)
                .photos(
                        business.getPhotos().stream()
                                .map(photo -> PhotoDTO.builder()
                                        .id(photo.getId())
                                        .url(photo.getUrl())
                                        .publicId(photo.getPublicId())
                                        .fileType(photo.getFileType())
                                        .photoType(photo.getPhotoType())
                                        .build())
                                .collect(Collectors.toList()))
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
                .build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<BusinessPublicCards> listPublicApproved(@Nullable String categoryName, int page, int size) {
        Page<Business> businesses = (categoryName != null && !categoryName.isBlank())
                ? businessRepository.findAllByCategoryAndValidationStatus(categoryName, ValidationStatus.VALIDATED,
                        PageRequest.of(page, size))
                : businessRepository.findByValidationStatus(ValidationStatus.VALIDATED, PageRequest.of(page, size));
        return new PagedResponse<>(businesses.map(this::mapToPublicBusinessCards));
    }

    private BusinessPublicCards mapToPublicBusinessCards(Business business) {
        var logoUrl = business.getPhotos() != null && !business.getPhotos().isEmpty()
                ? business.getPhotos().stream()
                        .filter(photo -> photo.getPhotoType() == PhotoType.LOGO)
                        .findFirst()
                        .map(Photo::getUrl)
                        .orElse(null)
                : null;
        return BusinessPublicCards.builder()
                .id(business.getId())
                .commercialName(business.getCommercialName())
                .description(business.getDescription())
                .address(business.getAddress())
                .phone(business.getPhone())
                .logoUrl(logoUrl)
                .build();
    }

    public BusinessPublicDTO getPublicBusinessDetails(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ClientException("Business not found", HttpStatus.NOT_FOUND));

        if (business.getValidationStatus() != ValidationStatus.VALIDATED) {
            throw new ClientException("Business is not approved for public view", HttpStatus.FORBIDDEN);
        }

        return mapToPublicBusinessView(business);
    }

    private BusinessPublicDTO mapToPublicBusinessView(Business b) {
        List<String> schedules = splitToList(b.getSchedules());

        // Campos que podrá ver el usuario
        return BusinessPublicDTO.builder()
                .id(b.getId())
                .commercialName(b.getCommercialName())
                .description(b.getDescription())
                .phone(b.getPhone())
                .whatsappNumber(b.getWhatsappNumber())
                .facebook(b.getFacebook())
                .instagram(b.getInstagram())
                .tiktok(b.getTiktok())
                .website(b.getWebsite())
                .address(b.getAddress())
                .parishCommunitySector(b.getParishCommunitySector())
                .googleMapsCoordinates(b.getGoogleMapsCoordinates())
                .photos(b.getPhotos().stream()
                        .map(photo -> PhotoDTO.builder()
                                .id(photo.getId())
                                .url(photo.getUrl())
                                .publicId(photo.getPublicId())
                                .fileType(photo.getFileType())
                                .photoType(photo.getPhotoType())
                                .build())
                        .toList())
                .schedules(schedules)
                .acceptsWhatsappOrders(b.getAcceptsWhatsappOrders())
                .deliveryService(b.getDeliveryService())
                .salePlace(b.getSalePlace())
                .category(CategoryBusinessDTO.builder()
                        .id(b.getCategory().getId())
                        .name(b.getCategory().getName())
                        .build())
                .build();
    }

    @Autowired
    private ObjectMapper mapper;

    /**
     * Actualiza un negocio existente según su estado
     */
    public BusinessResponseDto updateBusiness(Long businessId, Object updateDto) throws IOException {
        AppUser currentUser = findAuthenticatedUser();

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ClientException("Negocio no encontrado", HttpStatus.NOT_FOUND));

        if (!business.getUser().getId().equals(currentUser.getId())) {
            throw new ClientException("No tienes permisos para actualizar este negocio", HttpStatus.FORBIDDEN);
        }

        ValidationStatus status = business.getValidationStatus();

        switch (status) {
            case PENDING -> throw new ClientException(
                    "Su negocio está pendiente de revisión. Espere a que sea aceptado o rechazado.",
                    HttpStatus.BAD_REQUEST);

            case VALIDATED -> {
                if (!(updateDto instanceof BusinessUpdateDto dto))
                    throw new ClientException("DTO inválido para actualización", HttpStatus.BAD_REQUEST);
                updateAllowedFields(business, dto);
            }

            case REJECTED -> {
                if (!(updateDto instanceof BusinessFullUpdateDto dto))
                    throw new ClientException("DTO inválido para actualización completa", HttpStatus.BAD_REQUEST);
                updateAllFields(business, (BusinessFullUpdateDto) updateDto);
            }
        }

        Business updatedBusiness = businessRepository.save(business);
        return mapToBusinessResponse(updatedBusiness);
    }

    /**
     * Actualiza solo los campos permitidos para negocios VALIDADOS
     */
    private void updateAllowedFields(Business business, BusinessUpdateDto dto) throws JsonProcessingException {
        if (dto.getCommercialName() != null)
            business.setCommercialName(dto.getCommercialName());
        if (dto.getDescription() != null)
            business.setDescription(dto.getDescription());
        if (dto.getFacebook() != null)
            business.setFacebook(dto.getFacebook());
        if (dto.getInstagram() != null)
            business.setInstagram(dto.getInstagram());
        if (dto.getTiktok() != null)
            business.setTiktok(dto.getTiktok());
        if (dto.getWebsite() != null)
            business.setWebsite(dto.getWebsite());
        if (dto.getPhone() != null)
            business.setPhone(dto.getPhone());

        if (dto.getAcceptsWhatsappOrders() != null) {
            business.setAcceptsWhatsappOrders(dto.getAcceptsWhatsappOrders());
            if (dto.getAcceptsWhatsappOrders()
                    && (dto.getWhatsappNumber() == null || dto.getWhatsappNumber().isBlank())) {
                throw new ClientException("Se requiere número de WhatsApp cuando se aceptan pedidos por WhatsApp",
                        HttpStatus.BAD_REQUEST);
            }
        }

        if (dto.getWhatsappNumber() != null)
            business.setWhatsappNumber(dto.getWhatsappNumber());
        if (dto.getAddress() != null)
            business.setAddress(dto.getAddress());
        if (dto.getGoogleMapsCoordinates() != null)
            business.setGoogleMapsCoordinates(dto.getGoogleMapsCoordinates());

        // schedules: nunca nulo, siempre JSON array
        if (dto.getSchedules() != null) {
            business.setSchedules(mapper.writeValueAsString(dto.getSchedules()));
        } else {
            business.setSchedules("[]");
        }
    }

    /**
     * Actualiza todos los campos para negocios RECHAZADOS, incluyendo fotos
     */
    private void updateAllFields(Business business, BusinessFullUpdateDto dto) throws IOException {
        // Actualiza campos básicos
        updateAllowedFields(business, mapFullToPartial(dto));

        if (dto.getCategoryId() != null) {
            BusinessCategory category = findCategoryById(dto.getCategoryId());
            business.setCategory(category);
        }

        if (dto.getDeliveryService() != null)
            business.setDeliveryService(dto.getDeliveryService());
        if (dto.getSalePlace() != null)
            business.setSalePlace(dto.getSalePlace());

        List<Photo> newPhotos = new ArrayList<>();

        // Subir nuevo logo
        if (dto.getLogoFile() != null) {
            List<String> logoData = uploadOptionalFile(dto.getLogoFile());
            Photo logo = Photo.builder()
                    .url(logoData.get(0))
                    .publicId(logoData.get(1))
                    .fileType(logoData.get(2))
                    .business(business)
                    .photoType(PhotoType.LOGO)
                    .build();
            newPhotos.add(logo);
        }

        // Subir fotos del carrusel
        if (dto.getCarouselFiles() != null) {
            List<List<String>> carouselData = uploadMultipleFiles(dto.getCarouselFiles());
            for (List<String> slideData : carouselData) {
                Photo slide = Photo.builder()
                        .url(slideData.get(0))
                        .publicId(slideData.get(1))
                        .fileType(slideData.get(2))
                        .business(business)
                        .photoType(PhotoType.SLIDE)
                        .build();
                newPhotos.add(slide);
            }
        }

        business.getPhotos().clear();
        business.getPhotos().addAll(newPhotos);

        business.setValidationStatus(ValidationStatus.PENDING);

        businessRepository.save(business);
    }

    /**
     * Convierte un BusinessFullUpdateDto a BusinessUpdateDto para reutilizar
     * updateAllowedFields
     */
    private BusinessUpdateDto mapFullToPartial(BusinessFullUpdateDto fullDto) {
        return BusinessUpdateDto.builder()
                .commercialName(fullDto.getCommercialName())
                .description(fullDto.getDescription())
                .facebook(fullDto.getFacebook())
                .instagram(fullDto.getInstagram())
                .tiktok(fullDto.getTiktok())
                .website(fullDto.getWebsite())
                .phone(fullDto.getPhone())
                .acceptsWhatsappOrders(fullDto.getAcceptsWhatsappOrders())
                .whatsappNumber(fullDto.getWhatsappNumber())
                .address(fullDto.getAddress())
                .googleMapsCoordinates(fullDto.getGoogleMapsCoordinates())
                .schedules(fullDto.getSchedules() != null ? fullDto.getSchedules() : new ArrayList<>())
                .build();
    }

    /**
     * Mapea la entidad Business a DTO de respuesta con URLs de fotos
     */
    private BusinessResponseDto mapToBusinessResponse(Business business) {
        List<String> schedulesList = new ArrayList<>();
        if (business.getSchedules() != null && !business.getSchedules().isBlank()) {
            try {
                schedulesList = mapper.readValue(business.getSchedules(), new TypeReference<List<String>>() {
                });
            } catch (JsonProcessingException e) {
                schedulesList = new ArrayList<>();
            }
        }

        List<String> carouselUrls = business.getPhotos().stream()
                .filter(photo -> photo.getPhotoType() == PhotoType.SLIDE)
                .map(Photo::getUrl)
                .toList();

        String logoUrl = business.getPhotos().stream()
                .filter(photo -> photo.getPhotoType() == PhotoType.LOGO)
                .map(Photo::getUrl)
                .findFirst()
                .orElse(null);

        return BusinessResponseDto.builder()
                .commercialName(business.getCommercialName())
                .description(business.getDescription())
                .facebook(business.getFacebook())
                .instagram(business.getInstagram())
                .tiktok(business.getTiktok())
                .website(business.getWebsite())
                .phone(business.getPhone())
                .acceptsWhatsappOrders(business.getAcceptsWhatsappOrders())
                .whatsappNumber(business.getWhatsappNumber())
                .address(business.getAddress())
                .googleMapsCoordinates(business.getGoogleMapsCoordinates())
                .schedules(schedulesList) // siempre lista, nunca nula
                .logoUrl(logoUrl)
                .carouselUrls(carouselUrls)
                .build();
    }

    // Hasta aqui actualizar.

    private AppUser findAuthenticatedUser() {
        return appUserRepository.findByUsername(authService.getAuthenticatedUser().getUsername())
                .orElseThrow(() -> new ClientException("User not found", HttpStatus.NOT_FOUND));
    }

    private BusinessCategory findCategoryById(Long categoryId) {
        return businessCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ClientException("Category not found", HttpStatus.NOT_FOUND));
    }

    private List<String> uploadOptionalFile(@Nullable MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            return cloudinaryService.uploadFile(file, "business/logos");
        } else {
            throw new ClientException("Logo file cannot be null or empty", HttpStatus.BAD_REQUEST);
        }
    }

    private List<List<String>> uploadMultipleFiles(@Nullable MultipartFile[] files) throws IOException {
        if (files == null)
            throw new ClientException("Files array cannot be null", HttpStatus.BAD_REQUEST);
        return Arrays.stream(files)
                .filter(f -> f != null && !f.isEmpty())
                .map(f -> {
                    try {
                        return cloudinaryService.uploadFile(f, "business/carrousel");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    private List<String> splitToList(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split("\\+"));
    }

    public static void validateWhatsapp(String phone) {
        if (phone == null || phone.isEmpty())
            throw new ClientException("El número de teléfono no puede ser nulo o vacío", HttpStatus.BAD_REQUEST);
        if (!Pattern.matches(WHATSAPP_GLOBAL_REGEX, phone))
            throw new ClientException("El número de WhatsApp debe estar en formato internacional válido (+XXXXXXXX...)",
                    HttpStatus.BAD_REQUEST);
    }

    public List<ParishesDto> getParishesByZone(ParishType parishType) {
        return parishesRepository.findAllByParishType(parishType).stream()
                .map(parish -> ParishesDto.builder()
                        .id(parish.getId())
                        .name(parish.getName())
                        .type(parish.getParishType())
                        .build())
                .toList();
    }

    public PagedResponse<BusinessDTO> searchBusinesses(String searchTerm, Pageable pageable) {
        Page<Business> businesses = businessRepository.findBusinessesWithFilters(
                searchTerm,
                ValidationStatus.VALIDATED,
                pageable);

        Page<BusinessDTO> dtoPage = businesses.map(this::mapToPrivateBusinessDTO);
        return new PagedResponse<>(dtoPage);
    }

}
